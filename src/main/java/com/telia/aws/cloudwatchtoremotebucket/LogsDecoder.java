package com.telia.aws;

import com.google.gson.Gson;
import com.telia.aws.cloudwatchtoremotebucket.CloudWatchLogEvents;

import static java.util.Base64.getMimeDecoder;

public class LogsDecoder {

    public static CloudWatchLogEvents fromJson(String data) {
        final byte[] decoded = getMimeDecoder().decode(data);
        return new Gson().fromJson(new String(decoded), CloudWatchLogEvents.class);
    }

}
