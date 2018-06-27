package com.telia.aws.cloudwatchtoremotebucket;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class WrappedCloudWatchEventTest {


    private String awsLogsEvent = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \"H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA==\"\n" +
            " }\n" +
            " }";

    @Test
    public void shouldDecodeBase64EncodedAndGzippedPayload() {
        CloudWatchPutRequest event = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = LogsDecoder.fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        assertEquals(2, events.getLogEvents().size());
    }

    @Test
    public void shouldDenormalizeMessages() {
        CloudWatchPutRequest event = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = LogsDecoder.fromBase64EncodedZippedPayload(event.getAwslogs().getData());

        List<UnWrappedCloudWatchLogEvent> unwrappedEvents = UnWrappedCloudWatchLogEvent.from(events);
        assertEquals(events.getLogEvents().size(), unwrappedEvents.size());
        assertTrue(unwrappedEvents.stream().allMatch(e->e.getOwner().equals(events.getOwner())));
        assertTrue(unwrappedEvents.stream().allMatch(e->e.getLogGroup().equals(events.getLogGroup())));
        assertTrue(unwrappedEvents.stream().allMatch(e->e.getLogStream().equals(events.getLogStream())));
        assertTrue(unwrappedEvents.stream().allMatch(e->e.getSubscriptionFilters().equals(events.getSubscriptionFilters())));
        System.out.println(new Gson().toJson(unwrappedEvents));
    }
}
