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

    PolygonAPI polygonApi = new PolygonAPI();
    polygonApi.addPolygonStreamListener(new AlgoPolygonStreamListener(this));

    AlpacaAPI alpacaApi = new AlpacaAPI();
    alpacaApi.addAlpacaStreamListener(new AlgoAlpacaStreamListener(this));

  }

  /**
   * On quote.
   *
   * @param message the message
   */
  public void onQuote(QuotesMessage message) {
    LOGGER.info("onQuote " + message);

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


  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {

  }

}
