package io.github.mainstringargs.alpaca.hftish;

import io.github.mainstringargs.polygon.enums.ChannelType;
import io.github.mainstringargs.polygon.nats.PolygonStreamListenerAdapter;
import io.github.mainstringargs.polygon.nats.message.ChannelMessage;
import io.github.mainstringargs.polygon.nats.message.QuotesMessage;
import io.github.mainstringargs.polygon.nats.message.TradesMessage;


/**
 * The listener interface for receiving algoPolygonStream events. The class that is interested in
 * processing a algoPolygonStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoPolygonStreamListener</code> method. When the algoPolygonStream event occurs, that
 * object's appropriate method is invoked.
 *
 * @see ChannelMessage
 */
public class AlgoPolygonStreamListener extends PolygonStreamListenerAdapter {

  /** The algorithm. */
  private Algorithm algorithm;

  /**
   * Instantiates a new algo polygon stream listener.
   *
   * @param algorithm the algorithm
   */
  public AlgoPolygonStreamListener(Algorithm algorithm) {
    super(algorithm.getAlgoConfig().getSymbol(), ChannelType.QUOTES, ChannelType.TRADES);
    this.algorithm = algorithm;
  }



  /*
   * (non-Javadoc)
   * 
   * @see io.github.mainstringargs.polygon.nats.PolygonStreamListenerAdapter#streamUpdate(java.lang.
   * String, io.github.mainstringargs.polygon.enums.ChannelType,
   * io.github.mainstringargs.polygon.nats.message.ChannelMessage)
   */
  @Override
  public void streamUpdate(String ticker, ChannelType channelType, ChannelMessage message) {
    switch (channelType) {
      case QUOTES:
        algorithm.onQuote((QuotesMessage) message);
        break;
      case TRADES:
        algorithm.onTrade((TradesMessage) message);
        break;
    }

  }

}
