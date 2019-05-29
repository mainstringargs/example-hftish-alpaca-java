package io.github.mainstringargs.alpaca.hftish;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import io.github.mainstringargs.polygon.enums.ChannelType;
import io.github.mainstringargs.polygon.nats.PolygonStreamListener;
import io.github.mainstringargs.polygon.nats.message.ChannelMessage;
import io.github.mainstringargs.polygon.nats.message.QuotesMessage;
import io.github.mainstringargs.polygon.nats.message.TradesMessage;

/**
 * The listener interface for receiving algoPolygonStream events. The class that is interested in
 * processing a algoPolygonStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoPolygonStreamListener<code> method. When the algoPolygonStream event occurs, that
 * object's appropriate method is invoked.
 *
 * @see AlgoPolygonStreamEvent
 */
public class AlgoPolygonStreamListener implements PolygonStreamListener {

  /** The algorithm. */
  private Algorithm algorithm;

  /**
   * Instantiates a new algo polygon stream listener.
   *
   * @param algorithm the algorithm
   */
  public AlgoPolygonStreamListener(Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.github.mainstringargs.polygon.nats.PolygonStreamListener#getStockChannelTypes()
   */
  @Override
  public Map<String, Set<ChannelType>> getStockChannelTypes() {

    HashMap<String, Set<ChannelType>> stockChannelTypes = new HashMap<>();

    stockChannelTypes.put(algorithm.getAlgoConfig().getSymbol(),
        new HashSet<ChannelType>(Arrays.asList(ChannelType.QUOTES, ChannelType.TRADES)));

    return stockChannelTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.github.mainstringargs.polygon.nats.PolygonStreamListener#streamUpdate(java.lang.String,
   * io.github.mainstringargs.polygon.enums.ChannelType,
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
