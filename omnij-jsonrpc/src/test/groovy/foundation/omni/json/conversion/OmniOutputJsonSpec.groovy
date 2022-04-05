package foundation.omni.json.conversion

import com.fasterxml.jackson.databind.module.SimpleModule
import foundation.omni.OmniOutput
import foundation.omni.net.OmniTestNetParams

/**
 * Basic test of serialization/deserialization of {@link OmniOutput}
 */
class OmniOutputJsonSpec extends BaseObjectMapperSpec {

    def "serialize"() {
        given:
        OmniOutput output = new OmniOutput(OmniTestNetParams.get().exodusAddress, 123.divisible)

        when:
        String serialized = mapper.writeValueAsString(output)

        then:
        serialized == '{"address":"mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv","amount":"123.0"}'
    }


    def "deserialize"() {
        given:
        String serialized = '{"address":"mpexoDuSkGGqvqrkrjiFng38QPkJQVFyqv","amount":"123.0"}'

        when:
        OmniOutput output = mapper.readValue(serialized, OmniOutput.class)

        then:
        output != null
        output.address() == OmniTestNetParams.get().exodusAddress
        output.amount() == 123.divisible

    }

    @Override
    def configureModule(SimpleModule module) {
        module.addSerializer(OmniOutput.class, new OmniOutputSerializer())
        module.addDeserializer(OmniOutput.class, new OmniOutputDeserializer())
    }
}
