package foundation.omni.netapi.analytics.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import foundation.omni.netapi.omniwallet.json.AddressVerifyInfo;
import foundation.omni.netapi.omniwallet.json.OmniwalletClientModule;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.LegacyAddress;
import org.consensusj.analytics.util.collector.LargestSliceCollector;
import org.consensusj.analytics.util.collector.LargestSliceList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit so we can have a pure Java test and performance measurements
 */
public class LargestSliceFromTetherBalanceFileTest {
    static final String filename = "src/test/java/foundation/omni/netapi/analytics/test/USDT_mastercoin_verify.json";
    static final File file = new File(filename);
    static final int numSlices = 100;
    static final BigDecimal richTotalDecimal = new BigDecimal("1000518068.95603298");
    static final BigDecimal otherTotalDecimal = new BigDecimal("334481931.04396702");
    static final BigDecimal grandTotalDecimal = new BigDecimal("1335000000.00000000");
    static final long richTotal = richTotalDecimal.movePointRight(8).longValueExact();
    static final long otherTotal = otherTotalDecimal.movePointRight(8).longValueExact();
    static final long grandTotal = grandTotalDecimal.movePointRight(8).longValueExact();
    static final Address richAddress001 = LegacyAddress.fromBase58("18jm59kYYk7jJTkESn7SgVZPiGohkSUYpt", BitcoinNetwork.MAINNET);
    static final Address richAddress100 = LegacyAddress.fromBase58("1NTMakcgVwQpMdGxRQnFKyb3G1FAJysSfz", BitcoinNetwork.MAINNET);
    static final long richBalance001 = 150000100000000L;
    static final long richBalance100 = 19141023930853148L;
    static final boolean verbose = true; // enable println output?

    static List<AddressVerifyInfo> addressVerifyListStrings;
    static LargestSliceCollector<AddressVerifyInfo, Long> collectorStrings;

    static List<AddressVerifyInfoLong> addressVerifyList;
    static LargestSliceCollector<AddressVerifyInfoLong, Long> collector;

    @Test
    void testJsonFileRead() {
        assertEquals(345204, addressVerifyList.size());
    }

    @Test
    void calcRichListStringWithParallelStream() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LargestSliceList<AddressVerifyInfo, Long> richList = addressVerifyListStrings.parallelStream().collect(collectorStrings);
        long timeMicros = stopwatch.elapsed(MICROSECONDS);
        verifyRichListString(richList, timeMicros);
    }

    @Test
    void calcRichListStringWithRegularStream() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LargestSliceList<AddressVerifyInfo, Long>  richList = addressVerifyListStrings.stream().collect(collectorStrings);
        long timeMicros = stopwatch.elapsed(MICROSECONDS);
        verifyRichListString(richList, timeMicros);
    }

    @Test
    void calcRichListLongWithParallelStream() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LargestSliceList<AddressVerifyInfoLong, Long>  richList = addressVerifyList.parallelStream().collect(collector);
        long timeMicros = stopwatch.elapsed(MICROSECONDS);
        verifyRichListLong(richList, timeMicros);
    }

    @Test
    void calcRichListLongWithRegularStream() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LargestSliceList<AddressVerifyInfoLong, Long>  richList = addressVerifyList.stream().collect(collector);
        long timeMicros = stopwatch.elapsed(MICROSECONDS);
        verifyRichListLong(richList, timeMicros);
    }

    private void verifyRichListString(LargestSliceList<AddressVerifyInfo, Long> richList, long timeMicros) {
        if (verbose) System.out.println("Elapsed time: " + (double) timeMicros / 1000.0 + " ms");
        long actualRichTotal = richList.getSliceList().stream().mapToLong(LargestSliceFromTetherBalanceFileTest::stringBalanceExtractor).sum();

        assertEquals(numSlices, richList.getSliceList().size());
        assertEquals(richTotal, actualRichTotal);
        assertEquals(otherTotal, richList.getTotalOther());
        assertEquals(grandTotal, actualRichTotal + richList.getTotalOther());
        assertEquals(richAddress001, richList.getSliceList().get(0).getAddress());
        assertEquals(richAddress100, richList.getSliceList().get(99).getAddress());
        assertEquals(richBalance001, stringBalanceExtractor(richList.getSliceList().get(0)));
        assertEquals(richBalance100, stringBalanceExtractor(richList.getSliceList().get(99)));
    }

    private void verifyRichListLong(LargestSliceList<AddressVerifyInfoLong, Long> richList, long timeMicros) {
        if (verbose) System.out.println("Elapsed time: " + (double) timeMicros / 1000.0 + " ms");
        long actualRichTotal = richList.getSliceList().stream().mapToLong(AddressVerifyInfoLong::balanceExtractor).sum();

        assertEquals(numSlices, richList.getSliceList().size());
        assertEquals(richTotal, actualRichTotal);
        assertEquals(otherTotal, richList.getTotalOther());
        assertEquals(grandTotal, actualRichTotal + richList.getTotalOther());
        assertEquals(richAddress001, richList.getSliceList().get(0).address);
        assertEquals(richAddress100, richList.getSliceList().get(99).address);
        assertEquals(richBalance001, AddressVerifyInfoLong.balanceExtractor(richList.getSliceList().get(0)));
        assertEquals(richBalance100, AddressVerifyInfoLong.balanceExtractor(richList.getSliceList().get(99)));
    }

    @BeforeAll
    public static void setupTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new OmniwalletClientModule(null));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JavaType resultType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, AddressVerifyInfo.class);
        addressVerifyListStrings = objectMapper.readValue(file, resultType);
        addressVerifyList = addressVerifyListStrings.stream()
                .map(v -> new AddressVerifyInfoLong(v.getAddress(), v.getBalance(), v.getReservedBalance()))
                .collect(Collectors.toList());

        collector = new LargestSliceCollector<>(numSlices,
                AddressVerifyInfoLong::balanceExtractor,
                0L,
                Long::sum);
        collectorStrings = new LargestSliceCollector<>(numSlices,
                LargestSliceFromTetherBalanceFileTest::stringBalanceExtractor,
                0L,
                Long::sum);

        warmupJit();
    }

    private static void warmupJit() {
        Object r1 = addressVerifyListStrings.stream().collect(collectorStrings);
        Object r2 = addressVerifyListStrings.parallelStream().collect(collectorStrings);
        Object r3 = addressVerifyList.stream().collect(collector);
        Object r4 = addressVerifyList.parallelStream().collect(collector);
    }

    private static long stringBalanceExtractor(AddressVerifyInfo info) {
        long balance = (new BigDecimal(info.getBalance())).movePointRight(8).longValueExact();
        long reservedBalance = info.getReservedBalance() != null ?
        (new BigDecimal(info.getReservedBalance())).movePointRight(8).longValueExact() : 0;
        return balance + reservedBalance;
    }
}
