# Example HFT-ish Algorithm for Alpaca Trading API for Java

<a href="https://travis-ci.org/mainstringargs/example-hftish-alpaca-java"><img src="https://travis-ci.org/mainstringargs/example-hftish-alpaca-java.svg?branch=master" alt="Build Status"></a>

This is designed to mimic the [python version](https://github.com/alpacahq/example-hftish), using the [alpaca-java](https://github.com/mainstringargs/alpaca-java) library.

The Main Class is:

```
io.github.mainstringargs.alpaca.hftish.Driver
```

Arguments are as follows:

* -s / --symbol: the stock to trade (defaults to "SNAP")
* -q / --quantity: the maximum number of shares to hold at once. Note that this does not account for any existing position; the algorithm only tracks what is bought as part of its execution. (Default 500, minimum 100.)
