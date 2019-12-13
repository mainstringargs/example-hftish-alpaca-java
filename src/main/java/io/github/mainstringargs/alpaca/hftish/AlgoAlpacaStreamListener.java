package io.github.mainstringargs.alpaca.hftish;

import io.github.mainstringargs.alpaca.websocket.listener.AlpacaStreamListenerAdapter;
import io.github.mainstringargs.alpaca.websocket.message.AlpacaStreamMessageType;
import io.github.mainstringargs.domain.alpaca.websocket.AlpacaStreamMessage;
import io.github.mainstringargs.domain.alpaca.websocket.trade.TradeUpdateMessage;

/**
 * The listener interface for receiving algoAlpacaStream events. The class that is interested in
 * processing a algoAlpacaStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoAlpacaStreamListener</code> method. When the algoAlpacaStream event occurs, that
 * object's appropriate method is invoked.
 *
 * @see UpdateMessage
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
        super(AlpacaStreamMessageType.TRADE_UPDATES);
        this.algorithm = algorithm;
    }



    @Override
    public void onStreamUpdate(AlpacaStreamMessageType streamMessageType,
                    AlpacaStreamMessage streamMessage) {
        switch (streamMessageType) {
            case TRADE_UPDATES:

                algorithm.onTradeUpdates((TradeUpdateMessage) streamMessage);
                break;
        }
    }

}
