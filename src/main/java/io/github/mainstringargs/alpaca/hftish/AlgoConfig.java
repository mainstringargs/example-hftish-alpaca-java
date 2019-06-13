package io.github.mainstringargs.alpaca.hftish;

import picocli.CommandLine.Option;

/**
 * The Class AlgoConfig.
 */
class AlgoConfig {

  /** The Constant PLACEHOLDER_DEFAULT. */
  public static final String PLACEHOLDER_DEFAULT = "<PLACEHOLDER>";

  /** The symbol. */
  @Option(names = {"-s", "--symbol"}, defaultValue = "SNAP",
      description = "the stock to trade (defaults to \"SNAP\"")
  private String symbol;

  /** The quantity. */
  @Option(names = {"-q", "--quantity"}, defaultValue = "500",
      description = "the maximum number of shares to hold at once. Note that this does not account for any existing position; the algorithm only tracks what is bought as part of its execution. (Default 500, minimum 100.)")
  private int quantity;

  /** The key. */
  @Option(names = {"-key", "--alpaca-key"}, defaultValue = PLACEHOLDER_DEFAULT,
      description = "The key used for Alpaca authentication")
  private String key;

  /** The secret. */
  @Option(names = {"-secret", "--alpaca-secret"}, defaultValue = PLACEHOLDER_DEFAULT,
      description = "The secret used for Alpaca authentication")
  private String secret;

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

  /**
   * Gets the key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets the secret.
   *
   * @return the secret
   */
  public String getSecret() {
    return secret;
  }

  @Override
  public String toString() {
    return "AlgoConfig [symbol=" + symbol + ", quantity=" + quantity + ", key=" + key + ", secret="
        + secret + "]";
  }


}
