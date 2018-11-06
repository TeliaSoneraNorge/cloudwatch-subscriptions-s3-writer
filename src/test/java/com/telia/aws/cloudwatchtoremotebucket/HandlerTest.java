package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static com.telia.aws.cloudwatchtoremotebucket.EnvironmentUtils.injectEnvironmentVariable;
import static com.telia.aws.cloudwatchtoremotebucket.JacksonConfiguration.mapper;
import static com.telia.aws.cloudwatchtoremotebucket.LogsDecoder.fromBase64EncodedZippedPayload;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class HandlerTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private Context context;


    private String awsLogsEvent = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \"H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA==\"\n" +
            " }\n" +
           " }";


    private String ecsapedJson = "{\"event\":{\"id\":\"34374210759739096915414743846824364166776183001735954432\",\"message\":\"{\\\"time\\\":\\\"2018-11-05T04:42:00.119992289Z\\\",\\\"id\\\":\\\"\\\",\\\"remote_ip\\\":\\\"100.109.0.0\\\",\\\"host\\\":\\\"100.109.0.5:8082\\\",\\\"method\\\":\\\"GET\\\",\\\"uri\\\":\\\"/health\\\",\\\"status\\\":200,\\\"latency\\\":23441,\\\"latency_human\\\":\\\"23.441µs\\\",\\\"bytes_in\\\":0,\\\"bytes_out\\\":21,\\\"log\\\":\\\"{\\\\\\\"time\\\\\\\":\\\\\\\"2018-11-05T04:42:00.119992289Z\\\\\\\",\\\\\\\"id\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"remote_ip\\\\\\\":\\\\\\\"100.109.0.0\\\\\\\",\\\\\\\"host\\\\\\\":\\\\\\\"100.109.0.5:8082\\\\\\\",\\\\\\\"method\\\\\\\":\\\\\\\"GET\\\\\\\",\\\\\\\"uri\\\\\\\":\\\\\\\"/health\\\\\\\",\\\\\\\"status\\\\\\\":200, \\\\\\\"latency\\\\\\\":23441,\\\\\\\"latency_human\\\\\\\":\\\\\\\"23.441µs\\\\\\\",\\\\\\\"bytes_in\\\\\\\":0,\\\\\\\"bytes_out\\\\\\\":21}\\\\n\\\",\\\"stream\\\":\\\"stdout\\\",\\\"docker\\\":{\\\"container_id\\\":\\\"abf397af6adf077df2977bece79c087736b1427c1a259e106c21381139073a31\\\"},\\\"kubernetes\\\":{\\\"container_name\\\":\\\"divx-taas-identity-docs\\\",\\\"namespace_name\\\":\\\"identity-staging\\\",\\\"pod_name\\\":\\\"taas-identity-docs-2237894582-klwbj\\\",\\\"pod_id\\\":\\\"655433d4-c187-11e8-b633-0ab3651b0e9e\\\",\\\"labels\\\":{\\\"app\\\":\\\"taas-identity-docs\\\",\\\"pod-template-hash\\\":\\\"2237894582\\\"},\\\"host\\\":\\\"ip-172-20-35-67.eu-west-1.compute.internal\\\",\\\"master_url\\\":\\\"https://100.64.0.1:443/api\\\",\\\"namespace_id\\\":\\\"51b58562-fa6f-11e6-aa27-06773373c135\\\"}}\",\"timestamp\":1541392920000},\"owner\":\"884694863851\",\"logGroup\":\"taas-kubernetes\",\"logStream\":\"kubernetes.var.log.containers.taas-identity-docs-2237894582-klwbj_identity-staging_divx-taas-identity-docs-abf397af6adf077df2977bece79c087736b1427c1a259e106c21381139073a31.log\",\"subscriptionFilters\":[\"lambdafunction_logfilter_taas-kubernetes\"]}"  ;

    /**
     * Basic Bas64 and Gzip decode test.
     */
    @Test
    public void shouldDecodeBase64EncodedAndGzippedPayload() throws IOException {
        CloudWatchPutRequest event = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        assertEquals(2, events.getLogEvents().size());
    }

    /**
     * This verifies that the code that splits Cloud Watch Events and copies the values owner, log group and stream into each of the contained log entry
     */
    @Test
    public void shouldDenormalizeMessages() throws IOException {

        CloudWatchPutRequest event = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());

        List<ExtendedCloudWatchLogEvent> unwrappedEvents = ExtendedCloudWatchLogEvent.from(events);
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
     * This test makes sure that the Cloudwatch Log event is split, and that metadata like owner, log group and log
     * stream are added to the each item, when the environment variable 'split' is set to true
     *
     * @throws Exception
     */
    @Test
    public void shouldSplitEvents() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");
        injectEnvironmentVariable("split", "true");

        CloudWatchPutRequest awsCloudWatchEvent = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents payload = fromBase64EncodedZippedPayload(awsCloudWatchEvent.getAwslogs().getData());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);

        // capture the PutObjectRequest that was sent to the object by the handler
        verify(s3Client).putObject(captor.capture());

        TypeReference<List<ExtendedCloudWatchLogEvent>> LIST_TYPE = new TypeReference<List<ExtendedCloudWatchLogEvent>>() {};
        List<ExtendedCloudWatchLogEvent> eventList =
                mapper.readValue(new InputStreamReader(captor.getValue().getInputStream()),LIST_TYPE) ;

        // Assert that it contains metadata
        // All log items should have the same subscription filter as the parent JSON object
        assertTrue(eventList.stream().allMatch(e -> e.getOwner().equals(payload.getOwner())));

        assertEquals(2, eventList.size());
    }

    /**
     * This test makes sure that metadata like owner, log group and log stream are NOT added to the list of
     * events, when the environment split is set to false. The log events returned are the
     * "vanilla" CloudWatch event types .
     *
     * @throws Exception
     */
    @Test
    public void shouldNotDenormalizeEvents() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");

        CloudWatchPutRequest awsCloudWatchEvent = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents payload = fromBase64EncodedZippedPayload(awsCloudWatchEvent.getAwslogs().getData());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);

        // capture the PutObjectRequest that was sent to the object by the handler
        verify(s3Client).putObject(captor.capture());

        // Verifiy that we can deserialize the data to a CloudWatchLogEvents composite object (not split)
        CloudWatchLogEvents vanillaEvents = mapper.readValue(
                new InputStreamReader(captor.getValue().getInputStream()), CloudWatchLogEvents.class);
        assertEquals(2, vanillaEvents.getLogEvents().size());
    }


    /**
     * This test verifies that the elements of the CloudWatch Events are "unwrapped" into the Event object that
     * is written to S3
     *
     * @throws Exception
     */

    @Test
    public void testJsonUnwrappingWithJackson() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");
        injectEnvironmentVariable("split", "true");

        CloudWatchPutRequest awsCloudWatchEvent = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);
        verify(s3Client).putObject(captor.capture());

        PutObjectRequest por = captor.getValue() ;
        
        JsonNode node = mapper.readTree(por.getInputStream());

        // assert that the ExtendedLogEvent is unwrapped into the main event.
        assertTrue(node.get(0).path("message").isValueNode());
        assertTrue(node.get(0).path("id").isValueNode());
        assertTrue(node.get(0).path("timestamp").isValueNode());

    }

}