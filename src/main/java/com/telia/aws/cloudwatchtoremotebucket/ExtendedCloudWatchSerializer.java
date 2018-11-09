package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static com.telia.aws.cloudwatchtoremotebucket.CloudWatchLogKey.*;

public class ExtendedCloudWatchSerializer extends StdSerializer<ExtendedCloudWatchLogEvent> {


    public ExtendedCloudWatchSerializer() {
        this(null);
    }

    protected ExtendedCloudWatchSerializer(Class<ExtendedCloudWatchLogEvent> t) {
        super(t);
    }

    @Override
    public void serialize(ExtendedCloudWatchLogEvent value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getSubscriptionFilters() == null) {
            System.out.println(value);
        }
        gen.writeStartObject();
        gen.writeStringField(OWNER.withPrefix(), value.getOwner());
        gen.writeStringField(LOG_GROUP.withPrefix(), value.getLogGroup());
        gen.writeStringField(LOG_STREAM.withPrefix(), value.getLogStream());
        gen.writeArrayFieldStart(SUBSCRIPTION_FILTERS.withPrefix());
        for (String f : value.getSubscriptionFilters()) {
            gen.writeString(f);
        }
        gen.writeEndArray();
        gen.writeStringField(ID.withPrefix(), value.getEvent().getId());
        gen.writeNumberField(TIMESTAMP.withPrefix(), value.getEvent().getTimestamp());
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

        JsonParser parser = factory.createParser(value.getEvent().getMessage());
        JsonNode root = mapper.readTree(parser);
        String messagePayload = root.findPath("message").asText();
        parser = factory.createParser(messagePayload);
        JsonNode payloadNode = parser.readValueAsTree();
        copyJsonInline(payloadNode, gen);
        gen.writeEndObject();
    }

    /**
     * This flattens out a JSON tree, and writes all keys under the resulting root.
     * If a key names "Message" is encountered, it parses it into a JsonNode and passes it into itself.
     * <p>
     * Generates Json from a root node, and recursively parse any JSON in a contained "Message"
     *
     * @param root
     * @param gen
     */
    private void copyJsonInline(JsonNode root, JsonGenerator gen) {
        root.fields().forEachRemaining(n -> {
            try {
                String key = n.getKey();
                if (n.getValue().isLong()) {
                    gen.writeNumberField(key, n.getValue().asLong());
                } else if (n.getValue().isTextual()) {
                    gen.writeStringField(key, n.getValue().textValue());
                } else if (n.getValue().isInt()) {
                    gen.writeNumberField(key, n.getValue().intValue());
                } else if (n.getValue().isBoolean()) {
                    gen.writeBoolean(n.getValue().asBoolean());
                } else if (n.getValue().isNumber()) {
                    gen.writeNumber(n.getValue().asDouble());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}