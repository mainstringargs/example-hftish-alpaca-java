package io.github.mainstringargs.alpaca.hftish;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import io.github.mainstringargs.alpaca.enums.MessageType;
import io.github.mainstringargs.alpaca.websocket.AlpacaStreamListener;
import io.github.mainstringargs.alpaca.websocket.message.OrderUpdateMessage;
import io.github.mainstringargs.alpaca.websocket.message.UpdateMessage;

/**
 * The listener interface for receiving algoAlpacaStream events. The class that is interested in
 * processing a algoAlpacaStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoAlpacaStreamListener<code> method. When the algoAlpacaStream event occurs, that
 * object's appropriate method is invoked.
 *
 * @see AlgoAlpacaStreamEvent
 */
public class AlgoAlpacaStreamListener implements AlpacaStreamListener {

  /** The algorithm. */
  private Algorithm algorithm;

  /**
   * Instantiates a new algo alpaca stream listener.
   *
   * @param algorithm the algorithm
   */
  public AlgoAlpacaStreamListener(Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  /* (non-Javadoc)
   * @see io.github.mainstringargs.alpaca.websocket.AlpacaStreamListener#getMessageTypes()
   */
  @Override
  public Set<MessageType> getMessageTypes() {
    return new HashSet<MessageType>(Arrays.asList(MessageType.ORDER_UPDATES));
  }

  /* (non-Javadoc)
   * @see io.github.mainstringargs.alpaca.websocket.AlpacaStreamListener#streamUpdate(io.github.mainstringargs.alpaca.enums.MessageType, io.github.mainstringargs.alpaca.websocket.message.UpdateMessage)
   */
  @Override
  public void streamUpdate(MessageType messageType, UpdateMessage message) {
    switch (messageType) {
      case ORDER_UPDATES:

        algorithm.onTradeUpdates((OrderUpdateMessage) message);
        break;
    }

  }

}
