package foundation.omni.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.consensusj.bitcoin.cli.CliOptions;

/**
 * Option definitions for Omni Consensus Tool
 */
public class OmniConsensusToolOptions extends CliOptions {
    public OmniConsensusToolOptions() {
        super();
        this.addOption(Option.builder("o").longOpt("output")
                .desc("Output filename")
                .hasArg()
                .argName("filename")
                .build())
                .addOption(Option.builder("p").longOpt("property")
                        .desc("Omni property/currency id (numeric)")
                        .hasArg()
                        .argName("id")
                        .build())
                .addOption(Option.builder("x").longOpt("compare")
                        .desc("Compare properties from two URLs")
                        .build())
                .addOptionGroup(new OptionGroup()
                        .addOption(Option.builder("core").longOpt("omnicore-url")
                                .desc("Use Omni Core API via URL")
                                .hasArg()
                                .argName("url")
                                .build())
                        .addOption(Option.builder("wallet").longOpt("omniwallet-url")
                                .desc("Use Omniwallet API via URL")
                                .hasArg()
                                .argName("url")
                                .build())
                        .addOption(Option.builder("chest").longOpt("omnichest-url")
                                .desc("Use Omnichest API via URL")
                                .hasArg()
                                .argName("url")
                                .build()));
    }

}
