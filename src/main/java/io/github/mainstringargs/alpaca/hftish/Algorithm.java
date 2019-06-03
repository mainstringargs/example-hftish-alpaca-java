package io.github.mainstringargs.alpaca.hftish;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.mainstringargs.alpaca.AlpacaAPI;
import io.github.mainstringargs.alpaca.domain.Order;
import io.github.mainstringargs.alpaca.enums.OrderEvent;
import io.github.mainstringargs.alpaca.enums.OrderSide;
import io.github.mainstringargs.alpaca.enums.OrderTimeInForce;
import io.github.mainstringargs.alpaca.enums.OrderType;
import io.github.mainstringargs.alpaca.rest.exceptions.AlpacaAPIException;
import io.github.mainstringargs.alpaca.websocket.message.OrderUpdateMessage;
import io.github.mainstringargs.polygon.PolygonAPI;
import io.github.mainstringargs.polygon.nats.message.QuotesMessage;
import io.github.mainstringargs.polygon.nats.message.TradesMessage;

/**
 * The Class Algorithm.
 */
public class Algorithm implements Runnable {


  /** The logger. */
  private static Logger LOGGER = LogManager.getLogger(Algorithm.class);

  /** The algo config. */
  private AlgoConfig algoConfig;

  /** The polygon api. */
  private final PolygonAPI polygonApi;

  /** The alpaca api. */
  private final AlpacaAPI alpacaApi;

  /** The quote. */
  private Quote quote;

  /** The position. */
  private Position position;

  /**
   * Gets the algo config.
   *
   * @return the algo config
   */
  public AlgoConfig getAlgoConfig() {
    return algoConfig;
  }

  /**
   * Instantiates a new algorithm.
   *
   * @param algoConfig the algo config
   */
  public Algorithm(AlgoConfig algoConfig) {
    this.algoConfig = algoConfig;

    quote = new Quote();
    position = new Position();

    polygonApi = new PolygonAPI();
    alpacaApi = new AlpacaAPI();

    startStreamListeners();
  }

  /**
   * Start stream listeners.
   */
  private void startStreamListeners() {

    polygonApi.addPolygonStreamListener(new AlgoPolygonStreamListener(this));
    alpacaApi.addAlpacaStreamListener(new AlgoAlpacaStreamListener(this));
  }

  /**
   * On quote.
   *
   * @param message the message
   */
  public void onQuote(QuotesMessage message) {
    LOGGER.debug("onQuote " + message);
    quote.update(message.getStockQuote());
    LOGGER.debug("updatedQuote " + quote);
  }


  /**
   * On trade.
   *
   * @param message the message
   */
  public void onTrade(TradesMessage message) {
    LOGGER.debug("onTrade " + message);

    if (quote.isTraded()) {
      return;
    }

    // We've received a trade and might be ready to follow it
    if (message.getStockTrade().getT() <= (quote.getTime() + 50)) {
      // The trade came too close to the quote update
      // and may have been for the previous level
      return;
    }

    if (message.getStockTrade().getS() >= 100) {
      // The trade was large enough to follow, so we check to see if
      // we're ready to trade. We also check to see that the
      // bid vs ask quantities (order book imbalance) indicate
      // a movement in that direction. We also want to be sure that
      // we're not buying or selling more than we should.

      if (Double.compare(message.getStockTrade().getP(), quote.getAsk()) == 0
          && Double.compare(quote.getBidSize(), quote.getAskSize() * 1.8) > 0
          && ((position.getTotalShares() + position.getPendingBuyShares()) < algoConfig
              .getQuantity() - 100)) {

        try {
          Order order = alpacaApi.requestNewOrder(algoConfig.getSymbol(), 100, OrderSide.BUY,
              OrderType.LIMIT, OrderTimeInForce.DAY, quote.getAsk(), null, null);
          // Approximate an IOC order by immediately cancelling
          alpacaApi.cancelOrder(order.getId());

          position.updatePendingBuyShares(100);
          position.setOrdersFilledAmount(order.getId(), 0);

          LOGGER.info("Buy at " + quote.getAsk());

          quote.setTraded(true);

        } catch (AlpacaAPIException e) {
          e.printStackTrace();
        }

      } else if (Double.compare(message.getStockTrade().getP(), quote.getBid()) == 0
          && Double.compare(quote.getAskSize(), quote.getBidSize() * 1.8) > 0
          && ((position.getTotalShares() - position.getPendingSellShares()) >= 100)) {

        try {
          Order order = alpacaApi.requestNewOrder(algoConfig.getSymbol(), 100, OrderSide.SELL,
              OrderType.LIMIT, OrderTimeInForce.DAY, quote.getBid(), null, null);
          // Approximate an IOC order by immediately cancelling
          alpacaApi.cancelOrder(order.getId());

          position.updatePendingSellShares(100);
          position.setOrdersFilledAmount(order.getId(), 0);

          LOGGER.info("Sell at " + quote.getAsk());

          quote.setTraded(true);

        } catch (AlpacaAPIException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * On trade updates.
   *
   * @param message the message
   */
  public void onTradeUpdates(OrderUpdateMessage message) {
    LOGGER.debug("onTradeUpdates " + message);

    if (message.getEvent() == OrderEvent.FILL) {

      if (message.getOrder().getSide().trim().equalsIgnoreCase("Buy")) {
        position.updateTotalShares(Integer.parseInt(message.getOrder().getFilledQty()));
      } else {
        position.updateTotalShares(-1 * Integer.parseInt(message.getOrder().getFilledQty()));
      }

      position.removePendingOrder(message.getOrder().getId(), message.getOrder().getSide());
    } else if (message.getEvent() == OrderEvent.PARTIALLY_FILLED) {
      position.updateFilledAmount(message.getOrder().getId(),
          Integer.parseInt(message.getOrder().getFilledQty()), message.getOrder().getSide());
    } else if (message.getEvent() == OrderEvent.CANCELED
        && message.getEvent() == OrderEvent.REJECTED) {
      position.removePendingOrder(message.getOrder().getId(), message.getOrder().getSide());
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {

  }

}
