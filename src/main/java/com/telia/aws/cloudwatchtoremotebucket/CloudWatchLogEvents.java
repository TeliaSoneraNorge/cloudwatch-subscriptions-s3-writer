package com.telia.aws.cloudwatchtoremotebucket;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class CloudWatchLogEvents {
    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters;
    private List<CloudWatchLogEvent> logEvents;

}
