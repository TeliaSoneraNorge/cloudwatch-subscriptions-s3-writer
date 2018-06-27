package com.telia.aws.cloudwatchtoremotebucket;

import lombok.Data;

@Data
public class CloudWatchLogEvent {
    private String id;
    private String message;
    private long timestamp;
}
