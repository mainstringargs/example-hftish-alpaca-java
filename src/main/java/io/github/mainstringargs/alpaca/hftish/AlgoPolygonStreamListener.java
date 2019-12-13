package io.github.mainstringargs.alpaca.hftish;

import io.github.mainstringargs.domain.polygon.websocket.PolygonStreamMessage;
import io.github.mainstringargs.domain.polygon.websocket.quote.QuoteMessage;
import io.github.mainstringargs.domain.polygon.websocket.trade.TradeMessage;
import io.github.mainstringargs.polygon.websocket.listener.PolygonStreamListenerAdapter;
import io.github.mainstringargs.polygon.websocket.message.PolygonStreamMessageType;

/**
 * The listener interface for receiving algoPolygonStream events. The class that is interested in
 * processing a algoPolygonStream event implements this interface, and the object created with that
 * class is registered with a component using the component's
 * <code>addAlgoPolygonStreamListener</code> method. When the algoPolygonStream event occurs, that
 * object's appropriate method is invoked.
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
        super(algorithm.getAlgoConfig().getSymbol(), PolygonStreamMessageType.QUOTE,
                        PolygonStreamMessageType.TRADE);
        this.algorithm = algorithm;
    }



    /**
     * On stream update.
     *
     * @param streamMessageType the stream message type
     * @param streamMessage the stream message
     */
    @Override
    public void onStreamUpdate(PolygonStreamMessageType streamMessageType,
                    PolygonStreamMessage streamMessage) {
        switch (streamMessageType) {
            case QUOTE:
                algorithm.onQuote((QuoteMessage) streamMessage);
                break;
            case TRADE:
                algorithm.onTrade((TradeMessage) streamMessage);
                break;
        }

    }

}
