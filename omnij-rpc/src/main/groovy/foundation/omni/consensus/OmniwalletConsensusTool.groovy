package foundation.omni.consensus

/**
 * Command-line tool and class for fetching OmniWallet consensus data
 */
class OmniwalletConsensusTool extends OmniwalletConsensusFetcher implements ConsensusTool   {

    OmniwalletConsensusTool() {
        super(OmniwalletConsensusFetcher.OmniHost_Live)
    }

    OmniwalletConsensusTool(URI hostURI) {
        super(hostURI)
    }

    public static void main(String[] args) {
        OmniwalletConsensusTool tool = new OmniwalletConsensusTool()
        tool.run(args.toList())
    }

}
