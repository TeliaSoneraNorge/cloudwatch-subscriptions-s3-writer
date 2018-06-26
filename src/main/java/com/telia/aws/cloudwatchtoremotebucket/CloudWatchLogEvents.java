package com.telia.aws.cloudwatchtoremotebucket;

import lombok.Data;

import java.util.List;

/**
 * {
 "owner": "111111111111",
 "logGroup": "CloudTrail",
 "logStream": "111111111111_CloudTrail_us-east-1",
 "subscriptionFilters": [
 "Destination"
 ],
 "messageType": "DATA_MESSAGE",
 "logEvents": [
 {
 "id": "31953106606966983378809025079804211143289615424298221568",
 "timestamp": 1432826855000,
 "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 },
 {
 "id": "31953106606966983378809025079804211143289615424298221569",
 "timestamp": 1432826855000,
 "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 },
 {
 "id": "31953106606966983378809025079804211143289615424298221570",
 "timestamp": 1432826855000,
 "message": "{\"eventVersion\":\"1.03\",\"userIdentity\":{\"type\":\"Root\"}"
 }
 ]
 }
 */
@Data
public class CloudWatchLogEvents {
    private String owner;
    private String logGroup;
    private String logStream;
    private String[] subscriptionFilters ;
    private List<CloudWatchLogEvent> logEvents;
}
