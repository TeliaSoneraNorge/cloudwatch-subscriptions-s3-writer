package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class DenormalizedCloudwatchLogEvent {

    @Tolerate
    public DenormalizedCloudwatchLogEvent() {
    }

    @JsonUnwrapped
    private CloudWatchLogEvent event;

    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters;

    static List<DenormalizedCloudwatchLogEvent> from(CloudWatchLogEvents events) {
        return events.getLogEvents().parallelStream()
                .map(e -> DenormalizedCloudwatchLogEvent.builder()
                        .event(e)
                        .logGroup(events.getLogGroup())
                        .logStream(events.getLogStream())
                        .owner(events.getOwner())
                        .subscriptionFilters(events.getSubscriptionFilters())
                        .build())
                .collect(Collectors.toList());
    }
}
