package io.github.mainstringargs.alpaca.hftish;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.mainstringargs.alpaca.AlpacaAPI;
import io.github.mainstringargs.alpaca.enums.Direction;
import io.github.mainstringargs.alpaca.enums.OrderSide;
import io.github.mainstringargs.alpaca.enums.OrderStatus;
import io.github.mainstringargs.alpaca.enums.OrderTimeInForce;
import io.github.mainstringargs.alpaca.enums.OrderType;
import io.github.mainstringargs.alpaca.properties.AlpacaProperties;
import io.github.mainstringargs.alpaca.rest.exception.AlpacaAPIRequestException;
import io.github.mainstringargs.domain.alpaca.clock.Clock;
import io.github.mainstringargs.domain.alpaca.order.Order;
import io.github.mainstringargs.domain.alpaca.websocket.trade.TradeUpdateMessage;
import io.github.mainstringargs.domain.polygon.websocket.quote.QuoteMessage;
import io.github.mainstringargs.domain.polygon.websocket.trade.TradeMessage;
import io.github.mainstringargs.polygon.PolygonAPI;
import io.github.mainstringargs.util.concurrency.ExecutorTracer;

/**
 * The Class Algorithm.
 */
public class Algorithm {


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

    /** The polygon stream listener. */
    private AlgoPolygonStreamListener polygonStreamListener;

    /** The alpaca stream listener. */
    private AlgoAlpacaStreamListener alpacaStreamListener;

    /** The Constant DOUBLE_THRESHOLD. */
    public static final double DOUBLE_THRESHOLD = .0001;

    /** The Constant scheduledService. */
    private static final ScheduledExecutorService scheduledService =
                    ExecutorTracer.newScheduledThreadPool(1);

    /** The curr format. */
    DecimalFormat currFormat = new DecimalFormat("'$'0.00");


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

        String key = algoConfig.getKey();
        String secret = algoConfig.getSecret();

        if (key.equals(AlgoConfig.PLACEHOLDER_DEFAULT)) {
            key = AlpacaProperties.KEY_ID_VALUE;
        }
        if (secret.equals(AlgoConfig.PLACEHOLDER_DEFAULT)) {
            secret = AlpacaProperties.SECRET_VALUE;
        }

        alpacaApi = new AlpacaAPI(AlpacaProperties.API_VERSION_VALUE, key, secret,
                        AlpacaProperties.BASE_API_URL_VALUE);
        polygonApi = new PolygonAPI(key);


        Clock marketClock = null;
        try {
            marketClock = alpacaApi.getClock();
        } catch (AlpacaAPIRequestException e1) {
            e1.printStackTrace();
        }

        if (marketClock != null)
            if (marketClock.getIsOpen()) {
                init();
            } else {
                scheduleNextOpen(marketClock);
            }

    }

    /**
     * Inits the.
     */
    private void init() {
        Clock marketClock = null;
        try {
            marketClock = alpacaApi.getClock();
        } catch (AlpacaAPIRequestException e1) {
            e1.printStackTrace();
        }

        LOGGER.info("Market is now open. Current Clock " + marketClock);

        scheduleNextClose(marketClock);

        cancelPendingOrders();
        position.reset();
        updateInitialStates();
        startStreamListeners();
    }


    /**
     * Schedule next open.
     *
     * @param marketClock the market clock
     */
    private void scheduleNextOpen(Clock marketClock) {

        long delay = ChronoUnit.MILLIS.between(ZonedDateTime.now(), (marketClock.getNextOpen()));

        if (delay < 0) {

            try {
                marketClock = alpacaApi.getClock();
            } catch (AlpacaAPIRequestException e1) {
                e1.printStackTrace();
            }

            LOGGER.info("Clock has not yet rolled over after market close, sleeping for 15 minutes");

            try {
                Thread.sleep(TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            scheduleNextOpen(marketClock);

            return;
        }

        LOGGER.info("Market is closed. Will Open in "
                        + TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS) + " minutes");

        scheduledService.schedule(new Runnable() {

            @Override
            public void run() {
                init();

            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule next close.
     *
     * @param marketClock the market clock
     */
    private void scheduleNextClose(Clock marketClock) {

        long delay = ChronoUnit.MILLIS.between(ZonedDateTime.now(), (marketClock.getNextClose()));

        LOGGER.info("Market will Close in " + TimeUnit.MINUTES.convert(delay, TimeUnit.MILLISECONDS)
                        + " minutes");

        scheduledService.schedule(new Runnable() {

            @Override
            public void run() {
                closeStreamListeners();
                cancelPendingOrders();
                position.reset();

                Clock marketClock = null;
                try {
                    marketClock = alpacaApi.getClock();
                } catch (AlpacaAPIRequestException e1) {
                    e1.printStackTrace();
                }

                LOGGER.info("Market is now closed. Current Clock " + marketClock);

                scheduleNextOpen(marketClock);

            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel pending orders.
     */
    private void cancelPendingOrders() {
        List<Order> orders = null;
        try {
            orders = alpacaApi.getOrders(OrderStatus.OPEN, 50, null, ZonedDateTime.now(),
                            Direction.ASCENDING, false);
        } catch (AlpacaAPIRequestException e) {
            e.printStackTrace();
        }

        if (orders != null) {
            for (Order order : orders) {

                if (order.getSymbol().trim().equalsIgnoreCase(algoConfig.getSymbol().trim())) {
                    try {
                        boolean cancelledOrder = alpacaApi.cancelOrder(order.getId());

                        LOGGER.info("Cancelling " + order.getId() + " " + cancelledOrder);
                    } catch (AlpacaAPIRequestException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Update initial states.
     */
    private void updateInitialStates() {
        String ticker = algoConfig.getSymbol();

        io.github.mainstringargs.domain.alpaca.position.Position currentPosition = null;

        try {
            currentPosition = alpacaApi.getOpenPositionBySymbol(ticker.trim());
        } catch (AlpacaAPIRequestException e) {
            e.printStackTrace();
        }

        if (currentPosition != null) {
            String qtyForTicker = currentPosition.getQty();

            if (qtyForTicker != null && !qtyForTicker.isEmpty()) {
                Integer qtyInt = Integer.parseInt(qtyForTicker);
                LOGGER.info("Currently own " + qtyInt + " of " + ticker);
                position.setTotalShares(qtyInt);
            }
        }

    }

    /**
     * Start stream listeners.
     */
    private void startStreamListeners() {

        polygonStreamListener = new AlgoPolygonStreamListener(this);
        alpacaStreamListener = new AlgoAlpacaStreamListener(this);

        polygonApi.addPolygonStreamListener(polygonStreamListener);
        alpacaApi.addAlpacaStreamListener(alpacaStreamListener);
    }


    /**
     * Close stream listeners.
     */
    protected void closeStreamListeners() {
        if (polygonStreamListener != null) {
            polygonApi.removePolygonStreamListener(polygonStreamListener);
        }
        if (alpacaStreamListener != null) {
            alpacaApi.removeAlpacaStreamListener(alpacaStreamListener);
        }
    }


    /**
     * On quote.
     *
     * @param message the message
     */
    public synchronized void onQuote(QuoteMessage message) {
        LOGGER.debug("onQuote " + message);
        quote.update(message);
        LOGGER.debug("updatedQuote " + quote);
    }


    /**
     * On trade.
     *
     * @param streamMessage the message
     */
    public synchronized void onTrade(TradeMessage streamMessage) {
        LOGGER.debug("onTrade " + streamMessage);

        if (quote.isTraded()) {
            return;
        }

        // System.out.println(new Date(message.getStockTrade().getT()) + " "+new
        // Date(quote.getTime()));

        // We've received a trade and might be ready to follow it
        if (streamMessage.getT() <= (quote.getTime() + 50)) {
            // The trade came too close to the quote update
            // and may have been for the previous level
            return;
        }

        if (streamMessage.getS() >= 100) {


            // The trade was large enough to follow, so we check to see if
            // we're ready to trade. We also check to see that the
            // bid vs ask quantities (order book imbalance) indicate
            // a movement in that direction. We also want to be sure that
            // we're not buying or selling more than we should.

            if (Math.abs(streamMessage.getP() - quote.getAsk()) < DOUBLE_THRESHOLD
                            && Double.compare(quote.getBidSize(),
                                            (double) quote.getAskSize() * 1.8) > 0.0
                            && ((position.getTotalShares()
                                            + position.getPendingBuyShares()) < algoConfig
                                                            .getQuantity() - 100)) {



                try {

                    double buyingPower =
                                    Double.parseDouble(alpacaApi.getAccount().getBuyingPower());

                    if (buyingPower > (quote.getAsk() * 100)) {

                        LOGGER.info("Buy " + 100 + " of " + algoConfig.getSymbol() + " at "
                                        + currFormat.format(quote.getAsk()) + "; Current shares: "
                                        + position.getTotalShares());

                        Order order = alpacaApi.requestNewOrder(algoConfig.getSymbol(), 100,
                                        OrderSide.BUY, OrderType.LIMIT, OrderTimeInForce.DAY,
                                        quote.getAsk(), null, false, null, null, null, null, null);
                        // Approximate an IOC order by immediately cancelling
                        alpacaApi.cancelOrder(order.getId().trim());

                        position.updatePendingBuyShares(100);
                        position.setOrdersFilledAmount(order.getId(), 0);


                        quote.setTraded(true);
                    } else {
                        LOGGER.info("Ignoring buy; Not enough buying power: "
                                        + currFormat.format(buyingPower));
                    }

                } catch (AlpacaAPIRequestException e) {
                    e.printStackTrace();
                }

            } else if (Math.abs(streamMessage.getP() - quote.getBid()) < DOUBLE_THRESHOLD
                            && Double.compare(quote.getAskSize(),
                                            (double) quote.getBidSize() * 1.8) > 0.0
                            && ((position.getTotalShares()
                                            - position.getPendingSellShares()) >= 100)
                            && position.getTotalShares() > 0) {

                long numberToSell = 100;

                if (numberToSell > position.getTotalShares()) {
                    numberToSell = position.getTotalShares();
                }

                try {

                    LOGGER.info("Sell " + numberToSell + " of " + algoConfig.getSymbol() + " at "
                                    + currFormat.format(quote.getAsk()) + "; Current shares: "
                                    + position.getTotalShares());

                    Order order = alpacaApi.requestNewOrder(algoConfig.getSymbol(),
                                    (int) numberToSell, OrderSide.SELL, OrderType.LIMIT,
                                    OrderTimeInForce.DAY, quote.getBid(), null, false, null, null,
                                    null, null, null);
                    // Approximate an IOC order by immediately cancelling
                    alpacaApi.cancelOrder(order.getId().trim());

                    position.updatePendingSellShares(numberToSell);
                    position.setOrdersFilledAmount(order.getId().trim(), 0);


                    quote.setTraded(true);

                } catch (AlpacaAPIRequestException e) {
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
    public synchronized void onTradeUpdates(TradeUpdateMessage message) {
        LOGGER.debug("onTradeUpdates " + message);

        if (message.getData().getEvent().equalsIgnoreCase("fill")) {

            if (message.getData().getOrder().getType().trim().equalsIgnoreCase("Buy")) {
                // position.updateFilledAmount(message.getOrder().getId(),
                // Integer.parseInt(message.getOrder().getFilledQty()),
                // message.getOrder().getSide());
                position.updateTotalShares(
                                Integer.parseInt(message.getData().getOrder().getFilledQty()));
            } else {
                // position.updateFilledAmount(message.getOrder().getId(),
                // Integer.parseInt(message.getOrder().getFilledQty()),
                // message.getOrder().getSide());
                position.updateTotalShares(
                                -1 * Integer.parseInt(message.getData().getOrder().getFilledQty()));
            }

            position.removePendingOrder(message.getData().getOrder().getId().trim(),
                            Integer.parseInt(message.getData().getOrder().getFilledQty()),
                            message.getData().getOrder().getSide());
        } else if (message.getData().getEvent().equalsIgnoreCase("partial_fill")) {
            position.updateFilledAmount(message.getData().getOrder().getId().trim(),
                            Integer.parseInt(message.getData().getOrder().getFilledQty()),
                            message.getData().getOrder().getSide());
        } else if (message.getData().getEvent().equalsIgnoreCase("cancelled")
                        || message.getData().getEvent().equalsIgnoreCase("rejected")) {
            position.removePendingOrder(message.getData().getOrder().getId().trim(),
                            Integer.parseInt(message.getData().getOrder().getQty()),
                            message.getData().getOrder().getSide());
        }
    }

}
