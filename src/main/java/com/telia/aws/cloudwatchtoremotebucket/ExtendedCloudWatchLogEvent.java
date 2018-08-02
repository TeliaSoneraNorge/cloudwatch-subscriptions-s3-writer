package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A CloudWathcLogEvent with metadata.
 */
@Data
@Builder
public class ExtendedCloudWatchLogEvent {

    @Tolerate
    public ExtendedCloudWatchLogEvent() {
    }

    @JsonUnwrapped
    private CloudWatchLogEvent event;

    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters;

    static List<ExtendedCloudWatchLogEvent> from(CloudWatchLogEvents events) {
        return events.getLogEvents().parallelStream()
                .map(e -> ExtendedCloudWatchLogEvent.builder()
                        .event(e)
                        .logGroup(events.getLogGroup())
                        .logStream(events.getLogStream())
                        .owner(events.getOwner())
                        .subscriptionFilters(events.getSubscriptionFilters())
                        .build())
                .collect(Collectors.toList());
    }
}
