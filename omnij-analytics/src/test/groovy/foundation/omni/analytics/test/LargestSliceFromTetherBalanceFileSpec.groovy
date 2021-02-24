package foundation.omni.analytics.test

import foundation.omni.OmniValue
import groovy.json.JsonSlurper
import org.bitcoinj.core.Address
import org.consensusj.analytics.util.collector.LargestSliceAccumulator
import org.consensusj.analytics.util.collector.LargestSliceCollector
import org.consensusj.analytics.util.collector.LargestSliceList
import org.consensusj.analytics.util.collector.LargestSliceListImpl
import spock.lang.Shared
import spock.lang.Specification

import java.time.Instant
import java.util.stream.Collectors
/**
 *
 */
class LargestSliceFromTetherBalanceFileSpec extends Specification {
    static final boolean printResults = true
    static final int numSlices = 100;
    // Correct values for current dataset file with numSlices = 100
    static final long richTotal = 1000518068.95603298G.movePointRight(8).longValueExact()
    static final long otherTotal = 334478836.71429362G.movePointRight(8).longValueExact()
    static final long grandTotal = 1334996905.67032660G.movePointRight(8).longValueExact()
    static final filename = 'src/test/java/foundation/omni/analytics/test/USDT_mastercoin_verify.json'
    static final  file = new File(filename)

    @Shared
    List<AddressVerifyInfoLong> addressVerifyList

    @Shared
    LargestSliceCollector<AddressVerifyInfoLong, Long> collector

    def "calculate richlist with parallelStream"() {
        when: "use our collector in a parallel stream"
        def startTime = Instant.now().toEpochMilli()
        LargestSliceList<AddressVerifyInfoLong, Long> richList = addressVerifyList.parallelStream().collect(collector)
        def totalTime = Instant.now().toEpochMilli() - startTime

        then:
        richList.sliceList.size() == numSlices
        richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == richTotal
        richList.totalOther == otherTotal
        richList.totalOther + richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == grandTotal
        printResults(richList, totalTime)
    }

    def "calculate richlist with stream"() {
        when: "use our collector in a regular stream"
        def startTime = Instant.now().toEpochMilli()
        def richList = addressVerifyList.stream().collect(collector)
        def totalTime = Instant.now().toEpochMilli() - startTime

        then:
        richList.sliceList.size() == numSlices
        richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == richTotal
        richList.totalOther == otherTotal
        richList.totalOther + richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == grandTotal
        printResults(richList, totalTime)
    }

    def "calculate richlist more iteratively"() {
        when: "calculate iteratively using accumulator"
        def accum = new LargestSliceAccumulator<AddressVerifyInfoLong, Long>(numSlices, AddressVerifyInfoLong::balanceExtractor, 0L, Long::sum)
        def startTime = Instant.now().toEpochMilli()
        addressVerifyList.forEach(slice -> accum.accumulate(slice))
        def richList = new LargestSliceListImpl<AddressVerifyInfoLong, Long>(accum.getSortedSliceList(), accum.getTotalOther())
        def totalTime = Instant.now().toEpochMilli() - startTime

        then:
        richList.sliceList.size() == numSlices
        richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == richTotal
        richList.totalOther == otherTotal
        richList.totalOther + richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum() == grandTotal
        printResults(richList, totalTime)
    }

    private static boolean printResults(LargestSliceList<AddressVerifyInfoLong, Long>  richList, def totalTime) {
        if (printResults) {
            println "Calc time = ${totalTime}"
            //richList.sliceList.forEach{ println "${it.address}: ${it.balance}, ${it.reservedBalance}"}
            def richTotal = richList.sliceList.stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum()
            def grandTotal = richTotal + richList.totalOther
            println "Rich Total: ${OmniValue.ofWilletts(richTotal,true)}"
            println "Other Total: ${OmniValue.ofWilletts(richList.totalOther,true)}"
            println "Grand Total: ${OmniValue.ofWilletts(grandTotal,true)}"
            //println "Accum instance count = ${LargestSliceAccumulator.getInstanceCount()}"
        }
        return true
    }

    def setupSpec() {
        // Parse the file into a list
        def jsonSlurper = new JsonSlurper()
        List<Object> object = (List<Object>) jsonSlurper.parse(file)
        addressVerifyList = object.stream()
                .map(obj -> new AddressVerifyInfoLong(Address.fromString(null, obj.address), obj.balance, obj.reservedBalance ?: "0"))
                .collect(Collectors.toList());
        collector = new LargestSliceCollector<AddressVerifyInfoLong, Long>(numSlices, AddressVerifyInfoLong::balanceExtractor, 0L, Long::sum)
        println "List size = ${addressVerifyList.size()}"
        warmupJit()
    }

    private void warmupJit() {
        def r1 = addressVerifyList.stream().collect(collector)
        def r2 = addressVerifyList.parallelStream().collect(collector)
    }
}
