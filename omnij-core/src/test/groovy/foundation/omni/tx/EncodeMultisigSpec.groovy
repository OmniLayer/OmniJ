package foundation.omni.tx

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
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
        tx.outputs.size() == 2
        tx.outputs[0].value.value == 100_000
        tx.outputs[0].scriptBytes.encodeHex().toString() == "51210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32102e2e98198f331c436644f88b5a6bc5c65df64d53457d624ed05e78dba40dd5e012102fac1e512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087e0653ae"
        tx.outputs[1].value.value == 100_000
// TODO: Make this match
//        tx.outputs[1].scriptBytes.encodeHex().toString() == "51210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32102fabf5862d9719cf2fc07b1c1a2204cb4d74ea3e72f358f31f32f90c4629c210052ae"
    }

    byte[] hex(String string) {
        return string.decodeHex()
    }

}