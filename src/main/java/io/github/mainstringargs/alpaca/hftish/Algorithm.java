package io.github.mainstringargs.alpaca.hftish;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.mainstringargs.alpaca.AlpacaAPI;
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

  private Quote quote;

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
    polygonApi = new PolygonAPI();
    alpacaApi = new AlpacaAPI();

    startStreamListeners();
  }

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
    LOGGER.info("onQuote " + message);
    quote.update(message.getStockQuote());
    LOGGER.info("updatedQuote " + quote);
  }


  /**
   * On trade.
   *
   * @param message the message
   */
  public void onTrade(TradesMessage message) {
    LOGGER.info("onTrade " + message);

  }

  /**
   * On trade updates.
   *
   * @param message the message
   */
  public void onTradeUpdates(OrderUpdateMessage message) {
    LOGGER.info("onTradeUpdates " + message);

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
