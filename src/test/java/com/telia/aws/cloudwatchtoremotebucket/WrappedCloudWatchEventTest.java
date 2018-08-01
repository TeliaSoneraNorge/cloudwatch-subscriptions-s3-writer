package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.telia.aws.cloudwatchtoremotebucket.EnvironmentUtils.injectEnvironmentVariable;
import static com.telia.aws.cloudwatchtoremotebucket.LogsDecoder.fromBase64EncodedZippedPayload;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WrappedCloudWatchEventTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private Context context;


    private String awsLogsEvent = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \"H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA==\"\n" +
            " }\n" +
            " }";

    /**
     * Basic Bas64 and Gzip decode test.
     */
    @Test
    public void shouldDecodeBase64EncodedAndGzippedPayload() {
        CloudWatchPutRequest event = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        assertEquals(2, events.getLogEvents().size());
    }

    /**
     * This test checks that the values owner, log group and stream are copied from the parent object into
     * each of the contained log entries.
     */
    @Test
    public void shouldDenormalizeMessages() {

        CloudWatchPutRequest event = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());

        List<DenormalizedCloudwatchLogEvent> unwrappedEvents = DenormalizedCloudwatchLogEvent.from(events);
        assertEquals(events.getLogEvents().size(), unwrappedEvents.size());
        // All log items should have the same owner as the parent JSON object
        assertTrue(unwrappedEvents.stream().allMatch(e -> e.getOwner().equals(events.getOwner())));

        // All log items should have the same Log group as the parent JSON object
        assertTrue(unwrappedEvents.stream().allMatch(e -> e.getLogGroup().equals(events.getLogGroup())));

        // All log items should have the same log Stream as the parent JSON object
        assertTrue(unwrappedEvents.stream().allMatch(e -> e.getLogStream().equals(events.getLogStream())));

        // All log items should have the same subscription filter as the parent JSON object
        assertTrue(unwrappedEvents.stream().allMatch(e -> e.getSubscriptionFilters().equals(events.getSubscriptionFilters())));
    }

    /**
     * This test makes sure that metadata like owner, log group and log stream are added to the list of
     * events, when the environment denormalize_metadata is set to true
     *
     * @throws Exception
     */
    @Test
    public void shouldDenormalizeEvents() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");
        injectEnvironmentVariable("denormalize_metadata", "true");

        CloudWatchPutRequest awsCloudWatchEvent = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents payload = fromBase64EncodedZippedPayload(awsCloudWatchEvent.getAwslogs().getData());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);

        // capture the PutObjectRequest that was sent to the object by the handler
        verify(s3Client).putObject(captor.capture());

        // Assert that it contains metadata
        Type listType =
                new TypeToken<ArrayList<DenormalizedCloudwatchLogEvent>>() {
                }.getType();
        List<DenormalizedCloudwatchLogEvent> eventList =
                new Gson().fromJson(new InputStreamReader(captor.getValue().getInputStream()), listType);

        // All log items should have the same subscription filter as the parent JSON object
        assertTrue(eventList.stream().allMatch(e -> e.getOwner().equals(payload.getOwner())));
    }

    /**
     * This test makes sure that metadata like owner, log group and log stream are  NOT added to the list of
     * events, when the environment denormalize_metadata is set to false. The log events returned are the
     * "vanilla" CloudWatch event types .
     *
     * @throws Exception
     */
    @Test
    public void shouldNotDenormalizeEvents() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");
        injectEnvironmentVariable("denormalize_metadata", "false");

        CloudWatchPutRequest awsCloudWatchEvent = new Gson().fromJson(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents payload = fromBase64EncodedZippedPayload(awsCloudWatchEvent.getAwslogs().getData());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);

        // capture the PutObjectRequest that was sent to the object by the handler
        verify(s3Client).putObject(captor.capture());

        // Assert that it contains metadata
        Type listType = new TypeToken<ArrayList<CloudWatchLogEvent>>() {
        }.getType();

        // A JSON deserialization to a list of these "cloud watch native" events is a verification of the test since the
        // deserialization would have failed we tried to convert from denormalized data

        CloudWatchLogEvents vanillaEvents = new Gson().fromJson(
                new InputStreamReader(captor.getValue().getInputStream()), CloudWatchLogEvents.class);

    }

}
