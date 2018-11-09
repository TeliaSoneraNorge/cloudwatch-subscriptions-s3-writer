package com.telia.aws.cloudwatchtoremotebucket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import static java.util.Base64.getMimeDecoder;

/**
 * Utility class that takes the Base 64 encoded zipped Cloud Watch payload and decods into an object
 * representation of the type #CloudWatchLogEvents
 */
class LogsDecoder {


    static CloudWatchLogEvents fromBase64EncodedZippedPayload(String base64Encoded) {
        final byte[] decoded = getMimeDecoder().decode(base64Encoded);
        final Reader reader;
        try {
            final GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(decoded));
            reader = new InputStreamReader(in);
            return JacksonConfiguration.mapper.readValue(reader, CloudWatchLogEvents.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}