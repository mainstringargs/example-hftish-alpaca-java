package io.github.mainstringargs.alpaca.hftish;

import java.util.HashMap;
import java.util.Map;

/**
 * The position object is used to track how many shares we have. We need to keep track of this so
 * our position size doesn't inflate beyond the level we're willing to trade with. Because orders
 * may sometimes be partially filled, we need to keep track of how many shares are "pending" a buy
 * or sell as well as how many have been filled into our account.
 */
public class Position {

    /** The orders filled amount. */
    private Map<String, Long> ordersFilledAmount = new HashMap<String, Long>();

    /** The pending buy shares. */
    private long pendingBuyShares = 0;

    /** The pending sell shares. */
    private long pendingSellShares = 0;

    /** The total shares. */
    private long totalShares = 0;


    /**
     * Reset.
     */
    public synchronized void reset() {
        ordersFilledAmount.clear();
        pendingBuyShares = 0;
        pendingSellShares = 0;
        totalShares = 0;
    }


    /**
     * Update pending buy shares.
     *
     * @param quantity the quantity
     */
    public synchronized void updatePendingBuyShares(long quantity) {
        pendingBuyShares += quantity;
    }

    /**
     * Update pending sell shares.
     *
     * @param quantity the quantity
     */
    public synchronized void updatePendingSellShares(long quantity) {
        pendingSellShares += quantity;
    }

    /**
     * Update filled amount.
     *
     * @param orderId the order id
     * @param newAmount the new amount
     * @param side the side
     */
    public synchronized void updateFilledAmount(String orderId, long newAmount, String side) {
        Long oldAmount = this.ordersFilledAmount.get(orderId.trim());

        if (oldAmount == null) {
            oldAmount = 0L;
        }

        if (newAmount > oldAmount) {
            if (side.trim().equalsIgnoreCase("Buy")) {
                updatePendingBuyShares(oldAmount - newAmount);
                updateTotalShares(newAmount - oldAmount);
            } else {
                updatePendingSellShares(oldAmount - newAmount);
                updateTotalShares(oldAmount - newAmount);
            }
            this.ordersFilledAmount.put(orderId.trim(), newAmount);
        }
    }

    /**
     * Removes the pending order.
     *
     * @param orderId the order id
     * @param qty the qty
     * @param side the side
     */
    public synchronized void removePendingOrder(String orderId, int qty, String side) {
        Long oldAmount = this.ordersFilledAmount.get(orderId.trim());

        if (oldAmount == null) {
            oldAmount = 0L;
        }

        if (side.trim().equalsIgnoreCase("Buy")) {
            updatePendingBuyShares(oldAmount - qty);
        } else {
            updatePendingSellShares(oldAmount - qty);
        }


        this.ordersFilledAmount.remove(orderId.trim());

    }

    /**
     * Update total shares.
     *
     * @param quantity the quantity
     */
    public synchronized void updateTotalShares(long quantity) {
        this.totalShares += quantity;

    }

    /**
     * Gets the pending buy shares.
     *
     * @return the pending buy shares
     */
    public synchronized long getPendingBuyShares() {
        return pendingBuyShares;
    }

    /**
     * Sets the pending buy shares.
     *
     * @param pendingBuyShares the new pending buy shares
     */
    public synchronized void setPendingBuyShares(long pendingBuyShares) {
        this.pendingBuyShares = pendingBuyShares;
    }

    /**
     * Gets the pending sell shares.
     *
     * @return the pending sell shares
     */
    public synchronized long getPendingSellShares() {
        return pendingSellShares;
    }

    /**
     * Sets the pending sell shares.
     *
     * @param pendingSellShares the new pending sell shares
     */
    public synchronized void setPendingSellShares(long pendingSellShares) {
        this.pendingSellShares = pendingSellShares;
    }

    /**
     * Gets the total shares.
     *
     * @return the total shares
     */
    public synchronized long getTotalShares() {
        return totalShares;
    }

    /**
     * Sets the total shares.
     *
     * @param totalShares the new total shares
     */
    public synchronized void setTotalShares(long totalShares) {
        this.totalShares = totalShares;
    }

    /**
     * Sets the orders filled amount.
     *
     * @param orderId the order id
     * @param filledAmount the filled amount
     */
    public synchronized void setOrdersFilledAmount(String orderId, long filledAmount) {
        ordersFilledAmount.put(orderId, filledAmount);
    }

    /**
     * To string.
     *
     * @return the string
     */
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Position [ordersFilledAmount=" + ordersFilledAmount + ", pendingBuyShares="
                        + pendingBuyShares + ", pendingSellShares=" + pendingSellShares
                        + ", totalShares=" + totalShares + "]";
    }



}
