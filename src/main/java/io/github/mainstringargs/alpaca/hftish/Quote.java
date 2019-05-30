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
  private boolean traded = false;

  /** The level ct. */
  private long levelCt = 1;

  /** The time. */
  private long time = 0;

  /**
   * Called when a level change happens.
   */
  public void reset() {
    traded = false;
    levelCt += 1;
  }

  /**
   * Round value to specified number of places
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
   * Update the Quote
   *
   * @param quote the quote
   */
  public void update(StockQuote quote) {
    // Update bid and ask sizes and timestamp
    this.bidSize = quote.getBs();
    this.askSize = quote.getAs();

    // Check if there has been a level change
    if (Double.compare(bid, quote.getBp()) != 0 && Double.compare(ask, quote.getAp()) != 0
        && Double.compare(round(quote.getAp() - quote.getBp(), 2), .01) == 0) {
      // Update bids and asks and time of level change
      this.prevBid = this.bid;
      this.prevAsk = this.ask;
      this.bid = quote.getBp();
      this.ask = quote.getAp();
      this.time = quote.getT();

      // Update spreads
      this.prevSpread = round(this.prevAsk - this.prevBid, 3);
      this.spread = round(this.ask - this.bid, 3);

      LOGGER.info("Level Change: " + this.prevBid + " " + this.prevAsk + " " + this.prevSpread + " "
          + this.bid + " " + this.ask + " " + this.spread);

      // If change is from one penny spread level to a different penny
      // spread level, then initialize for new level (reset stale vars)

      if (Double.compare(prevSpread, 0.01) == 0) {
        this.reset();
      }


    }
  }

  public double getPrevBid() {
    return prevBid;
  }

  public void setPrevBid(double prevBid) {
    this.prevBid = prevBid;
  }

  public double getPrevAsk() {
    return prevAsk;
  }

  public void setPrevAsk(double prevAsk) {
    this.prevAsk = prevAsk;
  }

  public double getPrevSpread() {
    return prevSpread;
  }

  public void setPrevSpread(double prevSpread) {
    this.prevSpread = prevSpread;
  }

  public double getBid() {
    return bid;
  }

  public void setBid(double bid) {
    this.bid = bid;
  }

  public double getAsk() {
    return ask;
  }

  public void setAsk(double ask) {
    this.ask = ask;
  }

  public long getBidSize() {
    return bidSize;
  }

  public void setBidSize(long bidSize) {
    this.bidSize = bidSize;
  }

  public long getAskSize() {
    return askSize;
  }

  public void setAskSize(long askSize) {
    this.askSize = askSize;
  }

  public double getSpread() {
    return spread;
  }

  public void setSpread(double spread) {
    this.spread = spread;
  }

  public boolean isTraded() {
    return traded;
  }

  public void setTraded(boolean traded) {
    this.traded = traded;
  }

  public long getLevelCt() {
    return levelCt;
  }

  public void setLevelCt(long levelCt) {
    this.levelCt = levelCt;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  @Override
  public String toString() {
    return "Quote [prevBid=" + prevBid + ", prevAsk=" + prevAsk + ", prevSpread=" + prevSpread
        + ", bid=" + bid + ", ask=" + ask + ", bidSize=" + bidSize + ", askSize=" + askSize
        + ", spread=" + spread + ", traded=" + traded + ", levelCt=" + levelCt + ", time=" + time
        + "]";
  }

}
