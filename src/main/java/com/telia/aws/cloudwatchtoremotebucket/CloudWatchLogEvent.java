package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

@Data

public class CloudWatchLogEvent {
    private String id;
    private String message;
    private long timestamp;

}
