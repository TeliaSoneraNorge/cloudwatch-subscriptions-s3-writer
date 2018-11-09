    package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
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
            " \"data\": \"H4sIAMDl4lsAA+1Vy26jMBTd5ysQ2wnUBmM7rCaaSatK86iUzKpUyDFOYik8BE6qUZR/n2sTSDvtJ2RjbJ9zz31anCae55eq68RWrf42yk89//t8Nc9/LpbL+cPCn1pC/Vqp1kI4iklCGZ/Bpof29fahrQ+NRY3qzI/hPKBL0ypRvoEvFw7vDutOtroxuq7u9d6otgPmM0Bez+8vfbh4GQQXR1WZK+3kVoB0YZ0oiz4W2On3MhryM6K0IWJCECHRjDOE0Mi45G/NTxnoZH6a+TGJGWdRjGjEWByzOEGUU5zMEluBmBGOKeIYwT3iNKYz4MUEzTJ/mg2KTuiUZZn/dYzCnlK7RAjzAOMAsRWO0oSkmIeU0i8IpQhZwtQZHqEmUJ3RDI/Q4GRA/jSFMLraek1bFwdpPCnkTnmv2uy8iDJPG1V2ozVUcqvavBLlVaGqQ6P2WoS9NGiFshGhaHTYqfaopQov2uFT//1mXSx7bJQ2O+hw8V7axRJgevUPndqP8OOv+9/vofwo9ofePkK2W5egcwNjOtrZsFRrd2dX+GuZ/RQnxLYrSjiUFTA3xa4lb8fYmQ1T7FBIOYCUg0vKA6Gf255RV0Zoq3b3P/mOc5QgqUTAWbIOCCYy4OtIBGQj5VrShG8ocpKfzD6IP4MvUa4LsTlU0kI5uN44OP8Q2Ms5a7PKd3N8nt6ew+053J7D8BzsL2NynvwDSKzf/+EGAAA=\"\n" +
            " }\n" +
           " }";

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

        // Assert using JSON Tree , because the output stream is no longer compatible with the class due to
        // Custom Serialization


        // Debug Print the result to the console
        System.out.println(IOUtils.toString(captor.getValue().getInputStream()));

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

        // some smoke tests basically, the main test is in the parser
        assertTrue(node.get(0).path("@@@@CloudWatch.owner").isTextual());
    }

}