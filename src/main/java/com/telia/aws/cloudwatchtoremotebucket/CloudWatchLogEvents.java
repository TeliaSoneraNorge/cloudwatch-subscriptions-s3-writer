package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudWatchLogEvents {
    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters;
    private List<CloudWatchLogEvent> logEvents;

}
