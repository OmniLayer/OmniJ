package foundation.omni.json.conversion;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import foundation.omni.OmniValue;
import org.bitcoinj.core.NetworkParameters;

/**
 *
 */
public class OmniClientModule extends SimpleModule {
    public OmniClientModule(NetworkParameters netParams) {
        super("OmniJMappingClient", new Version(1, 0, 0, null, null, null));
        this.addSerializer(OmniValue.class, new OmniValueSerializer());
    }
}
