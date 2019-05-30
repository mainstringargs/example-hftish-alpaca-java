package io.github.mainstringargs.alpaca.hftish;

import io.github.mainstringargs.alpaca.enums.MessageType;
import io.github.mainstringargs.alpaca.websocket.AlpacaStreamListenerAdapter;
import io.github.mainstringargs.alpaca.websocket.message.OrderUpdateMessage;
import io.github.mainstringargs.alpaca.websocket.message.UpdateMessage;


/**
 * The listener interface for receiving algoAlpacaStream events. The class that is interested in
 * processing a algoAlpacaStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoAlpacaStreamListener</code> method. When the algoAlpacaStream event occurs, that
 * object's appropriate method is invoked.
 */
public class AlgoAlpacaStreamListener extends AlpacaStreamListenerAdapter {

  /** The algorithm. */
  private Algorithm algorithm;

  /**
   * Instantiates a new algo alpaca stream listener.
   *
   * @param algorithm the algorithm
   */
  public AlgoAlpacaStreamListener(Algorithm algorithm) {
    super(MessageType.ORDER_UPDATES);
    this.algorithm = algorithm;
  }



  /* (non-Javadoc)
   * @see io.github.mainstringargs.alpaca.websocket.AlpacaStreamListenerAdapter#streamUpdate(io.github.mainstringargs.alpaca.enums.MessageType, io.github.mainstringargs.alpaca.websocket.message.UpdateMessage)
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
