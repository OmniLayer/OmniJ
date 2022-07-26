package foundation.omni.netapi.analytics.test

import foundation.omni.CurrencyID
import foundation.omni.OmniValue
import foundation.omni.netapi.ConsensusService
import foundation.omni.netapi.analytics.OmniLayerRichListService
import foundation.omni.netapi.omnicore.OmniCoreClient
import io.reactivex.rxjava3.core.Observable
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.params.MainNetParams
import org.consensusj.analytics.service.TokenRichList
import org.consensusj.bitcoin.json.pojo.ChainTip
import org.consensusj.bitcoin.jsonrpc.RpcURI
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *
 */
@Ignore("Functional test")
class OmniLayerRichListServiceSpec extends Specification {

    @Shared ConsensusService consensusService
    @Shared OmniLayerRichListService omniLayerRichListService

    def "can get richlist single (indivis)"(long currencyID, int expectedSize) {
        when:
        TokenRichList<OmniValue, CurrencyID> richList
        def single = omniLayerRichListService.richList(CurrencyID.of(currencyID), 11)
                .blockingSubscribe(r -> {
                    richList = r
                }, e -> {
                    println "Error: $e"
                    throw e
                })

        then:
        richList != null
        richList.richList.size() == expectedSize

        where:
        currencyID << [3, 56, 59, 381]
        expectedSize << [11, 11, 11, 7]
    }

    def "can get richlist single (divis)"(long currencyID, int expectedSize) {
        when:
        def richList
        def single = omniLayerRichListService.richList(CurrencyID.of(currencyID), 11);
        single.blockingSubscribe(r -> {
                    richList = r
                }, e -> {
                    println: "Error $e"
                    throw e
                })

        then:
        richList != null
        richList.richList.size() == expectedSize

        where:
        currencyID << [1, 2, 31, 403]
        expectedSize << [11, 11, 11, 5]
    }

    @Ignore("This functional test should work, but with ~10-min between updates this is a long test.")
    def "can get richlist observable (divis)"() {
        given:
        def numCalls = 3

        when:
        def countDownLatch = new CountDownLatch(numCalls);
        def richList
        def observable = omniLayerRichListService.richListUpdates(CurrencyID.of(403), 11);
        observable.subscribe(r -> {
                    countDownLatch.countDown()
                    richList = r
                    println("got a richlist of size = ${richList.richList.size()}")
                })

        and:
        countDownLatch.await(1, TimeUnit.MINUTES);


        then:
        richList != null
        richList.richList.size() == 5
    }

    def setup() {
        consensusService = new OmniCoreClient(MainNetParams.get(), RpcURI.DEFAULT_MAINNET_URI, "bitcoinrpc","pass")
        omniLayerRichListService = new OmniLayerRichListService(consensusService);
    }
}
