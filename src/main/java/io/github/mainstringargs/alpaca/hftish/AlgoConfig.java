package io.github.mainstringargs.alpaca.hftish;

import picocli.CommandLine.Option;

/**
 * The Class AlgoConfig.
 */
class AlgoConfig {



  /** The symbol. */
  @Option(names = {"-s", "--symbol"}, defaultValue = "SNAP",
      description = "the stock to trade (defaults to \"SNAP\"")
  private String symbol;

  /** The quantity. */
  @Option(names = {"-q", "--quantity"}, defaultValue = "500",
      description = "the maximum number of shares to hold at once. Note that this does not account for any existing position; the algorithm only tracks what is bought as part of its execution. (Default 500, minimum 100.)")
  private int quantity;

  /**
   * Gets the symbol.
   *
   * @return the symbol
   */
  public String getSymbol() {
    return symbol;
  }

  /**
   * Gets the quantity.
   *
   * @return the quantity
   */
  public int getQuantity() {
    return quantity;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "AlgoConfig [symbol=" + symbol + ", quantity=" + quantity + "]";
  }


}
