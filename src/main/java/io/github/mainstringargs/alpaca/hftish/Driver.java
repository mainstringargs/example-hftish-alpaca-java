package io.github.mainstringargs.alpaca.hftish;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class Driver {

  public static void main(String[] args) {

    CommandLine commandLine = new CommandLine(new AlgoConfig());
    ParseResult parsed = commandLine.parseArgs(args);
    AlgoConfig algoConfig = (AlgoConfig) parsed.commandSpec().userObject();
    System.out.println(algoConfig);

  }

}
