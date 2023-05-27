package foundation.omni.netapi.omnicore

import foundation.omni.net.OmniMainNetParams
import foundation.omni.json.pojo.OmniJBalances
import foundation.omni.json.pojo.WalletAddressBalance
import foundation.omni.netapi.omniwallet.WalletBackupFile
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.utils.AppDataDirectory
import org.consensusj.bitcoin.jsonrpc.RpcURI
import spock.lang.Ignore
import spock.lang.Requires;
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path

import static foundation.omni.CurrencyID.*

/**
 *
 */
@Ignore("Requires a local Omni Core setup and running")
public class OmniCoreClientSpec extends Specification {
    @Shared
    boolean hasAddressIndex

    @Shared
    OmniCoreClient client

    def "get block height" () {
        when:
        def height = client.currentBlockHeightAsync().get()

        then: "height is a reasonable MainNet block height"
        height > 400000
    }

    @Unroll
    def "we can get consensus info for currency: #currency"() {
        when: "we get data"
        def balances = client.getConsensusForCurrency(currency)

        then: "something is there"
        balances.size() >= 1

        where:
        currency << [OMNI, TOMNI, MAID, USDT, AGRS, EURT, SAFEX]
    }

    private static final String PORTFOLIO_ADDRESSES_FILENAME = "PortfolioAddresses.json";

    @Ignore
    def "can get balances for a list of addresses"() {
        given:
        Path dataDirectory = AppDataDirectory.get("OmniPortfolio")
        File portfolioAddressesFile = dataDirectory.resolve(PORTFOLIO_ADDRESSES_FILENAME).toFile();
        if (!portfolioAddressesFile.exists()) {
            throw new RuntimeException(PORTFOLIO_ADDRESSES_FILENAME + " does not exist");
        }
        WalletBackupFile walletBackup = WalletBackupFile.read(portfolioAddressesFile);
        def addresses = walletBackup.getAddressList();

        when:
        def start = System.currentTimeMillis()
        OmniJBalances balances = client.balancesForAddressesAsync(addresses).join()
        def end = System.currentTimeMillis()
        System.out.println("${end-start} milliseconds")

        then:
        balances != null
        balances.size() == addresses.size()
    }

    def "can get mocked up combined btc/omni balances"() {
        given:
        Address address = OmniMainNetParams.get().exodusAddress

        when:
        WalletAddressBalance wab = client.balancesForAddressAsync(address).get()

        then:
        wab != null
    }

    @Requires({ instance.hasAddressIndex})
    def "can get address balance via address index call"() {
        given:
        Address address = OmniMainNetParams.get().exodusAddress

        when:
        def map = client.omniGetAllBalancesForAddressAsync(address).get()

        then:
        map != null
    }

    def setupSpec() {
        client = new OmniCoreClient(BitcoinNetwork.MAINNET,
                RpcURI.getDefaultMainNetURI(),
                "bitcoinrpc",
                "pass")
        hasAddressIndex = client.getRxOmniClient().isAddressIndexEnabled()
    }

}
