package io.github.mainstringargs.alpaca.hftish;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.mainstringargs.polygon.domain.StockQuote;

/**
 * 
 * We use Quote objects to represent the bid/ask spread. When we encounter a 'level change', a move
 * of exactly 1 penny, we may attempt to make one trade. Whether or not the trade is successfully
 * filled, we do not submit another trade until we see another level change. Note: Only moves of 1
 * penny are considered eligible because larger moves could potentially indicate some newsworthy
 * event for the stock, which this algorithm is not tuned to trade.
 */
public class Quote {

  /** The logger. */
  private static Logger LOGGER = LogManager.getLogger(Quote.class);

  /** The prev bid. */
  private double prevBid = 0.0;

  /** The prev ask. */
  private double prevAsk = 0.0;

  /** The prev spread. */
  private double prevSpread = 0.0;

  /** The bid. */
  private double bid = 0.0;

  /** The ask. */
  private double ask = 0.0;

  /** The bid size. */
  private long bidSize = 0;

  /** The ask size. */
  private long askSize = 0;

  /** The spread. */
  private double spread = 0.0;

  /** The traded. */
  private boolean traded = true;

  /** The level ct. */
  private long levelCt = 1;

  /** The time. */
  private long time = 0;
  
  /**
   * Called when a level change happens.
   */
  public synchronized void reset() {
    traded = false;
    levelCt += 1;
  }

  /**
   * Round value to specified number of places.
   *
   * @param value the value
   * @param places the places
   * @return the double
   */
  public static double round(double value, int places) {
    double scale = Math.pow(10, places);
    return Math.round(value * scale) / scale;
  }

  /**
   * Update the Quote.
   *
   * @param quote the quote
   */
  public synchronized void update(StockQuote quote) {
    // Update bid and ask sizes and timestamp
    this.bidSize = quote.getBs();
    this.askSize = quote.getAs();

    // Check if there has been a level change
    if (Double.compare(bid, quote.getBp()) != 0.0 && Double.compare(ask, quote.getAp()) != 0.0d
        && (Math.abs(round(quote.getAp() - quote.getBp(), 2) - .01) < Algorithm.DOUBLE_THRESHOLD)) {
      // Update bids and asks and time of level change
      this.prevBid = this.bid;
      this.prevAsk = this.ask;
      this.bid = quote.getBp();
      this.ask = quote.getAp();
      this.time = quote.getT();

      // Update spreads
      this.prevSpread = round(this.prevAsk - this.prevBid, 3);
      this.spread = round(this.ask - this.bid, 3);

      LOGGER.debug("Level Change: " + this.prevBid + " " + this.prevAsk + " " + this.prevSpread
          + " " + this.bid + " " + this.ask + " " + this.spread);

      // If change is from one penny spread level to a different penny
      // spread level, then initialize for new level (reset stale vars)

      if (Math.abs(prevSpread - 0.01) < Algorithm.DOUBLE_THRESHOLD) {
        this.reset();
      }


    }
  }

  /**
   * Gets the prev bid.
   *
   * @return the prev bid
   */
  public synchronized double getPrevBid() {
    return prevBid;
  }

  /**
   * Sets the prev bid.
   *
   * @param prevBid the new prev bid
   */
  public synchronized void setPrevBid(double prevBid) {
    this.prevBid = prevBid;
  }

  /**
   * Gets the prev ask.
   *
   * @return the prev ask
   */
  public synchronized double getPrevAsk() {
    return prevAsk;
  }

  /**
   * Sets the prev ask.
   *
   * @param prevAsk the new prev ask
   */
  public synchronized void setPrevAsk(double prevAsk) {
    this.prevAsk = prevAsk;
  }

  /**
   * Gets the prev spread.
   *
   * @return the prev spread
   */
  public synchronized double getPrevSpread() {
    return prevSpread;
  }

  /**
   * Sets the prev spread.
   *
   * @param prevSpread the new prev spread
   */
  public synchronized void setPrevSpread(double prevSpread) {
    this.prevSpread = prevSpread;
  }

  /**
   * Gets the bid.
   *
   * @return the bid
   */
  public synchronized double getBid() {
    return bid;
  }

  /**
   * Sets the bid.
   *
   * @param bid the new bid
   */
  public synchronized void setBid(double bid) {
    this.bid = bid;
  }

  /**
   * Gets the ask.
   *
   * @return the ask
   */
  public synchronized double getAsk() {
    return ask;
  }

  /**
   * Sets the ask.
   *
   * @param ask the new ask
   */
  public synchronized void setAsk(double ask) {
    this.ask = ask;
  }

  /**
   * Gets the bid size.
   *
   * @return the bid size
   */
  public synchronized long getBidSize() {
    return bidSize;
  }

  /**
   * Sets the bid size.
   *
   * @param bidSize the new bid size
   */
  public synchronized void setBidSize(long bidSize) {
    this.bidSize = bidSize;
  }

  /**
   * Gets the ask size.
   *
   * @return the ask size
   */
  public synchronized long getAskSize() {
    return askSize;
  }

  /**
   * Sets the ask size.
   *
   * @param askSize the new ask size
   */
  public synchronized void setAskSize(long askSize) {
    this.askSize = askSize;
  }

  /**
   * Gets the spread.
   *
   * @return the spread
   */
  public synchronized double getSpread() {
    return spread;
  }

  /**
   * Sets the spread.
   *
   * @param spread the new spread
   */
  public synchronized void setSpread(double spread) {
    this.spread = spread;
  }

  /**
   * Checks if is traded.
   *
   * @return true, if is traded
   */
  public synchronized boolean isTraded() {
    return traded;
  }

  /**
   * Sets the traded.
   *
   * @param traded the new traded
   */
  public synchronized void setTraded(boolean traded) {
    this.traded = traded;
  }

  /**
   * Gets the level ct.
   *
   * @return the level ct
   */
  public synchronized long getLevelCt() {
    return levelCt;
  }

  /**
   * Sets the level ct.
   *
   * @param levelCt the new level ct
   */
  public synchronized void setLevelCt(long levelCt) {
    this.levelCt = levelCt;
  }

  /**
   * Gets the time.
   *
   * @return the time
   */
  public synchronized long getTime() {
    return time;
  }

  /**
   * Sets the time.
   *
   * @param time the new time
   */
  public synchronized void setTime(long time) {
    this.time = time;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Quote [prevBid=" + prevBid + ", prevAsk=" + prevAsk + ", prevSpread=" + prevSpread
        + ", bid=" + bid + ", ask=" + ask + ", bidSize=" + bidSize + ", askSize=" + askSize
        + ", spread=" + spread + ", traded=" + traded + ", levelCt=" + levelCt + ", time=" + time
        + "]";
  }


}
