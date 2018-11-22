package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
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
        gen.writeRawValue(value.getEvent().getMessage());
    }
}

