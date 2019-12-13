package io.github.mainstringargs.alpaca.hftish;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

/**
 * The Class Driver.
 */
public class Driver {


    /** The logger. */
    private static Logger LOGGER = LogManager.getLogger(Driver.class);

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        CommandLine commandLine = new CommandLine(new AlgoConfig());
        ParseResult parsed = commandLine.parseArgs(args);
        AlgoConfig algoConfig = (AlgoConfig) parsed.commandSpec().userObject();
        LOGGER.info("Arguments: " + algoConfig);

        Algorithm algorithm = new Algorithm(algoConfig);

    }

}
