package foundation.omni.netapi.omniwallet

import foundation.omni.netapi.omniwallet.WalletBackupFile
import spock.lang.Specification
import spock.lang.Unroll


/**
 *
 */
class WalletBackupFileSpec extends Specification {
    static final testFilePath = "foundation/omni/netapi/omniwallet/"
    static final testFileNames = ["sample_omniwallet_backup.json", "sample_omniwallet_backup_address_only.json"]

    @Unroll
    def "test with #testFile"(String testFile) {
        given:
        def path =  testFilePath + testFile
        def file = new File(ClassLoader.getSystemResource(path).toURI())

        when:
        def backup = WalletBackupFile.read(file)

        then:
        backup.addresses.size() == 3
        backup.addresses[0].address == "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P"
        backup.addresses[1].address == "1Co1dhYDeF76DQyEyj4B5JdXF9J7TtfWWE"
        backup.addresses[2].address == "1Po1oWkD2LmodfkBYiAktwh76vkF93LKnh"

        where:
        testFile << testFileNames
    }

    @Unroll
    def "test save with data from #testFile"(String testFile) {
        given:
        def path =  testFilePath + testFile
        def file = new File(ClassLoader.getSystemResource(path).toURI())
        def tempFile =  File.createTempFile("test", ".json")

        when:
        def start = WalletBackupFile.read(file)

        and:
        WalletBackupFile.save(tempFile, start)

        and:
        def finish = WalletBackupFile.read(tempFile)

        then:
        finish.addresses.size() == 3
        finish.addresses[0].address == "1EXoDusjGwvnjZUyKkxZ4UHEf77z6A5S4P"
        finish.addresses[1].address == "1Co1dhYDeF76DQyEyj4B5JdXF9J7TtfWWE"
        finish.addresses[2].address == "1Po1oWkD2LmodfkBYiAktwh76vkF93LKnh"

        where:
        testFile << testFileNames
    }
}