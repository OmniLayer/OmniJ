package foundation.omni.tx

import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.spongycastle.util.encoders.Hex
import spock.lang.Specification


/**
 * WIP
 */
class EncodeMultisigSpec extends Specification {

    def "encode" () {
        given:
        // the address of the sender, used as seed for hashing:
        def senderAddr = "mvayzbj425X55kRLLPQiuCXWUED6LMP65C";
        // public key of the sender, used to redeem dust later:
        def senderPubKey = ECKey.fromPublicOnly(hex("0347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca3"));
        // payload (it's a "create property" transaction):
        def payload = hex("00000032010001000000000000426172654d756c7469736967546f6b656e73006275696c6465722e62697477617463682e636f000000000000000f4240")

        when:
        Transaction tx = EncodeMultisig.encodeObfuscated(senderPubKey, payload, senderAddr);

        then:
        tx != null
        tx.inputs.size() == 0       // No inputs yet
        tx.outputs.size() == 1
        tx.outputs[0].value.value == 100_000
        tx.outputs[0].scriptBytes != null
        // TODO: Check to make sure generated script is correct
    }

    byte[] hex(String string) {
        return string.decodeHex()
    }

}