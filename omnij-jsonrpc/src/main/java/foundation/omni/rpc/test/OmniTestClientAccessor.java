package foundation.omni.rpc.test;

/**
 * Interface for tests that use a BitcoinExtendedClient
 */
public interface OmniTestClientAccessor {

    /**
     * Preferred accessor
     * @return The Omni Client
     */
    OmniTestClient client();

    /**
     * JavaBeans style getter/accessor (for Groovy, etc)
     * @return The Omni Client
     */
    default OmniTestClient getClient() {
        return client();
    }
}
