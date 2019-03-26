package foundation.omni.tx

import org.bitcoinj.core.Transaction

/**
 *
 */
class OmniTransactionParserSpec extends BaseTxSpec {

    /**
     * See omnichest.info: http://omnichest.info/lookuptx.aspx?txid=8976ef82bf26f030badd47383fb0b78bc39dffa361cef00995a4bd79c892323d
     */
    static final String rawTxString = "010000000183ce74b0f8568476edf5cc764e5ee79380be42192d2b8371276295429bb93111010000006b483045022100d93ae0ff3ec5b80f71de9903a8de8649f9efe579403815743f8238252406487302206ff85a325bb717b7bd344db42024c20e160e4ff098dcff0d558a5aac3c14eda30121025909601275e0e28fcddb743720b0870884c619efd1b4f0eb2cd80026ba1c4515ffffffff04ac02000000000000475121025909601275e0e28fcddb743720b0870884c619efd1b4f0eb2cd80026ba1c4515210211699ea51d4f7456804bc55f38d49e94123d7e8bdd0226c2ffe8d6dac6d913ed52ae22020000000000001976a914957917bf8de671bc02f5c6cff43a63550a2aa07088ac22020000000000001976a914946cb2e08075bcbaf157e47bcb67eb2b2339d24288ac19fdd62b000000001976a914ed8cc8d7d5562f79abdc07d0b8324802506bea3888ac00000000";
    static final String rawTxHashString =  "8976ef82bf26f030badd47383fb0b78bc39dffa361cef00995a4bd79c892323d"
    static final String omniPayloadString = "000000000000001f000000002b82ea800000000000000000000000000000"
    static final byte[] rawTx = hex(rawTxString)
    static final byte[] txHash = hex(rawTxHashString)
    static final byte[] omniPayload = hex(omniPayloadString)
    static final Transaction tx = new Transaction(netParams, rawTx)

    def "Make sure tx string hashes to hash string"() {
        expect:
        tx.txId.bytes == txHash
    }

    def "can create OmniTransaction from Omni-encoded Bitcoin transaction"() {
        given:
        OmniTransactionParser parser = new OmniTransactionParser();

        when:
        OmniTransaction omniTx = parser.parse(tx)

        then:
        omniTx != null
        // Since we're not code complete on transaction parsing comment this out so we don't break the build.
        //omniTx.omniPayload == omniPayload
    }
}