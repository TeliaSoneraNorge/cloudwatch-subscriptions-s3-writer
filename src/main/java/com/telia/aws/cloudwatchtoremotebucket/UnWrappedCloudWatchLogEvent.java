package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UnWrappedCloudWatchLogEvent {

    @Tolerate
    public UnWrappedCloudWatchLogEvent() {
    }

    @JsonUnwrapped
    private CloudWatchLogEvent event;

    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters;

    static List<UnWrappedCloudWatchLogEvent> from(CloudWatchLogEvents events) {
        return events.getLogEvents().stream()
                .map(e -> UnWrappedCloudWatchLogEvent.builder()
                        .event(e)
                        .logGroup(events.getLogGroup())
                        .logStream(events.getLogStream())
                        .owner(events.getOwner())
                        .subscriptionFilters(events.getSubscriptionFilters())
                        .build())
                .collect(Collectors.toList());
    }
}
