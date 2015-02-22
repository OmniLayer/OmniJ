package com.msgilligan.bitcoin.cli

import spock.lang.Specification


/**
 * Test Spec for the bitcoinj-cli tool
 */
class BitcoinJCliSpec extends Specification {

    def "help option outputs help"() {
        when: "we run the tool"
        InputStream is = new ByteArrayInputStream(new byte[0])
        String[] args = ["--help"]
        BitcoinJCli cli = new BitcoinJCli(args)
        def status = cli.run(is, System.out, System.err)

        then: "Status is 1"
        status == 1
        // We could output to stringbuffers and check the output here, I suppose.
    }

    def "getblockcount"() {
        when: "we run the tool"
        InputStream is = new ByteArrayInputStream(new byte[0])
        String[] args = ["getblockcount", "-regtest"]
        BitcoinJCli cli = new BitcoinJCli(args)
        def status = cli.run(is, System.out, System.err)

        then: "Status is 0"
        status == 0
        // We could output to stringbuffers and check the output here, I suppose.
    }

    def "generate a block"() {
        when: "we run the tool"
        InputStream is = new ByteArrayInputStream(new byte[0])
        String[] args = ["setgenerate", "1", "-regtest"]
        BitcoinJCli cli = new BitcoinJCli(args)
        def status = cli.run(is, System.out, System.err)

        then: "Status is 0"
        status == 0
        // We could output to stringbuffers and check the output here, I suppose.
    }

}