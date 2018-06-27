package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {
 * "owner": "111111111111",
 * "logGroup": "CloudTrail",
 * "logStream": "111111111111_CloudTrail_us-east-1",
 * "subscriptionFilters": [
 * "Destination"
 * ],
 * "messageType": "DATA_MESSAGE",
 * "logEvents": [
 * {
 * "id": "31953106606966983378809025079804211143289615424298221568",
 * "timestamp": 1432826855000,
 * "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 * },
 * {
 * "id": "31953106606966983378809025079804211143289615424298221569",
 * "timestamp": 1432826855000,
 * "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 * },
 * {
 * "id": "31953106606966983378809025079804211143289615424298221570",
 * "timestamp": 1432826855000,
 * "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 * }
 * ]
 * }
 */
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
