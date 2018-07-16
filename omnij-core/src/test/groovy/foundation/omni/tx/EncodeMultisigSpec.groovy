package foundation.omni.tx

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.params.RegTestParams
import spock.lang.Shared

/**
 * WIP
 */
class EncodeMultisigSpec extends BaseTxSpec {
    // the address of the sender, used as seed for hashing:
    @Shared
    def sendingAddr = "mvayzbj425X55kRLLPQiuCXWUED6LMP65C";
    // public key of the sender, used to redeem dust later:
    @Shared
    def senderPubKey = ECKey.fromPublicOnly(hex("0347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca3"));
    // payload (it's a "create property" transaction):
    @Shared
    def payload     = hex("00000032010001000000000000426172654d756c7469736967546f6b656e73006275696c6465722e62697477617463682e636f000000000000000f4240")
    @Shared
    def expSeqPayload   = "0100000032010001000000000000426172654d756c7469736967546f6b656e0273006275696c6465722e62697477617463682e636f000000000000000f420340"

    def "sequencing payload produces expected result" () {
        when:
        byte[] sequenced = SequenceNumbers.add(payload);

        then:
        sequenced.encodeHex().toString() == expSeqPayload
    }

    def "obfuscating payload produces expected result" () {
        when:
        byte[] obf = Obfuscation.obfuscate(hex(expSeqPayload), sendingAddr);

        then:
        // TODO: The expected result here is not necessarily valid
        obf.encodeHex().toString() == "e2e98198f331c436644f88b5a6bc5c65df64d53457d624ed05e78dba40dd5efac1e512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087efabf"
    }

    def "encode" () {
        given:
        EncodeMultisig encoder = new EncodeMultisig(RegTestParams.get())

        when:
        Transaction tx = encoder.encodeObfuscated(senderPubKey, payload, sendingAddr);

        then:
        tx != null
        tx.inputs.size() == 0       // No inputs yet
        tx.outputs.size() == 2
        tx.outputs[0].value == 786.satoshi
        tx.outputs[0].scriptBytes.encodeHex().toString() == "51210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32103e2e98198f331c436644f88b5a6bc5c65df64d53457d624ed05e78dba40dd5e012103fac1e512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087e0653ae"
        tx.outputs[1].value == 684.satoshi
// TODO: The expected result here is not necessarily valid
        tx.outputs[1].scriptBytes.encodeHex().toString() == "51210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32103fabfe512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087e0052ae"
// TODO: The expected result here is not necessarily valid
        tx.bitcoinSerialize().encodeHex().toString() == "01000000000212030000000000006951210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32103e2e98198f331c436644f88b5a6bc5c65df64d53457d624ed05e78dba40dd5e012103fac1e512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087e0653aeac020000000000004751210347d08029b5cbc934f6079b650c50718eab5a56d51cf6b742ec9f865a41fcfca32103fabfe512bac2575554a5dee8a345fc773615af68a09d0291473316fe39087e0052ae00000000"
    }
}