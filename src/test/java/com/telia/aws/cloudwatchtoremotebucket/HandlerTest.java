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



    /**
     * {
     *     "@timestamp": "2018-11-20T12:08:27.800+00:00",
     *     "@version": "1",
     *     "message": "Refreshing caches...",
     *     "logger_name": "no.netcom.minbedrift.caching.CacheWarmerProcess",
     *     "thread_name": "pool-3-thread-2",
     *     "level": "INFO",
     *     "level_value": 20000,
     *     "source": "/minbedrift/logs/server.log",
     *     "sourcetype": "minbedrift:server"
     * }
     */
    private String awsLogsEvent = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \"H4sIAMDl4lsAA+1Vy26jMBTd5ysQ2wnUBmM7rCaaSatK86iUzKpUyDFOYik8BE6qUZR/n2sTSDvtJ2RjbJ9zz31anCae55eq68RWrf42yk89//t8Nc9/LpbL+cPCn1pC/Vqp1kI4iklCGZ/Bpof29fahrQ+NRY3qzI/hPKBL0ypRvoEvFw7vDutOtroxuq7u9d6otgPmM0Bez+8vfbh4GQQXR1WZK+3kVoB0YZ0oiz4W2On3MhryM6K0IWJCECHRjDOE0Mi45G/NTxnoZH6a+TGJGWdRjGjEWByzOEGUU5zMEluBmBGOKeIYwT3iNKYz4MUEzTJ/mg2KTuiUZZn/dYzCnlK7RAjzAOMAsRWO0oSkmIeU0i8IpQhZwtQZHqEmUJ3RDI/Q4GRA/jSFMLraek1bFwdpPCnkTnmv2uy8iDJPG1V2ozVUcqvavBLlVaGqQ6P2WoS9NGiFshGhaHTYqfaopQov2uFT//1mXSx7bJQ2O+hw8V7axRJgevUPndqP8OOv+9/vofwo9ofePkK2W5egcwNjOtrZsFRrd2dX+GuZ/RQnxLYrSjiUFTA3xa4lb8fYmQ1T7FBIOYCUg0vKA6Gf255RV0Zoq3b3P/mOc5QgqUTAWbIOCCYy4OtIBGQj5VrShG8ocpKfzD6IP4MvUa4LsTlU0kI5uN44OP8Q2Ms5a7PKd3N8nt6ew+053J7D8BzsL2NynvwDSKzf/+EGAAA=\"\n" +
            " }\n" +
            " }";



    /**
     *{
     *     "@timestamp": "2018-11-20T12:00:03.274+00:00",
     *     "@version": "1",
     *     "message": "entry: \r\nlogging_filter_version=1.25\r\nremote=10.199.18.59:54656\r\nlocal=10.199.18.207:9940\r\ncomponent=minbedrift\r\ncomponent_version=2.4901\r\nhostname=ip-aws-prod\r\nurl=http://10.199.18.207:9940/system/monitoring\r\nmethod=GET\r\npath=/system/monitoring\r\nheader_req_accept=null\r\nheader_req_x_forwarded_proto=null\r\nheader_req_user_agent=kube-probe/1.10\r\nheader_req_x_forwarded_for=null\r\nheader_req_host=10.199.18.207:9940\r\nheader_req_content_type=null\r\nheader_req_authorization=null\r\nsoap_method=null\r\nheader_resp_content_type=text/plain\r\nrequest_time=2018-11-20T12:00:03.273+0000\r\nresponse_code=200\r\nresponse_time=1\r\nclient_id=null\r\nunique_process_call_id=07658446-cd4f-4823-ad01-750c7d040fca\r\ncall_sequence_trace=.minbedrift#get-10_199_18_207:9940/system/monitoring\r\nrequest_body=<empty>\r\nresponse_body=/system/monitoring/common/memory\r\n/system/monitoring/common/appname\r\n/system/monitoring/common/versions-api\r\n/system/monitoring/common/ping\r\n/system/monitoring/common/busy\r\n/system/monitoring/common/node-status\r\n/system/monitoring/common/version\r\n/system/monitoring/common/metainf\r\n/system/monitoring/common/git-commit\r\n/system/monitoring/common/dependencies\r\n/system/monitoring/local/http_connection_counter\r\n/system/monitoring/local/background_task_counter\r\n/system/monitoring/local/http_connection_tracker\r\n/system/monitoring/local/background_task_tracker\r\n/system/monitoring/local/db\r\n/system/monitoring/adapters/CDS\r\n/system/monitoring/adapters/MessagingAdmin\r\n/system/monitoring/adapters/OCM\r\n/system/monitoring/adapters/SM\r\n/system/monitoring/adapters/PM\r\n\r\n",
     *     "logger_name": "HttpLogger",
     *     "thread_name": "http-nio-9940-exec-6",
     *     "level": "DEBUG",
     *     "level_value": 10000,
     *     "source": "/minbedrift/logs/payload.log",
     *     "sourcetype": "minbedrift:payload"
     * }
     */
    private String awsLogsEventNewLine = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \" H4sIAAfp81sAA7VXXW/bNhR9z68wvMdFMqlvGfOwLEnTAs1awOlTVQgUSdtE9FWJSuIF+e+7FCVbTlxPHbAXh+I599x7yUvy5vlsMplmvK7Jmt9tSz6dT6ZXF3cX8e31cnlxcz09V4TiMeeVgrBlO67nByEMNJQW65uqaEqFSl7Lj/13jy5lxUk2gLuJFq+bpKaVKKUo8ncilbyqgfkVoInm68kpTHzrBa8feC73tOf2FyDBlBOu0A8Mt/paRkB+kmQqROw4yHGsMPARQjtGl78yf46qKFeT0fSPnWEESDS1EA4MjA0L3aFwjtAc2ablO7+qEYqm5wPLB8gDMtJ2+ADrfGkIQq22MAA4yiG1tcjX8apNOe40Fti0XE2oeFZIvsDIxGFo4sB0w7nrYTfo7SlJB6iF/HkYOkijtMjKIgeHi0zkCWeVWMlXyM6lZTohwhrdFLXMScYXojTIY22UVcE00lTpYiNlOY9m0eyI21m9rSXPollW5EIWFeSmDTMuNwVb3Fzf6e+SyM3ix/QNJwzWo+LfY0IpL+Uib9L0DfYUr4rqkVSMsxiClMVxWlPDADYAFuK+SbjKJ+EQv4nRSUkYHRdUC/TDVR/waJFLtcgSTtlxJdLAulTib6JOw4BSF6SMu0U7YliXh9qSP8loVqZE5H3dfG+gkGNVz4vjVWxDFSPU02uoh5qDKlP819OtTFcdNBXKrxgG1uQC/Kk9oFDrMRRlqggWw4HtBcQIeZAYcIsERkjZynAYCyhfWR63vE5UWdQq6JyCu4pQvjD3VfvLmksDoxgWPMZB/O/11uefFGy7+I1npdz+/iqnFjqqMIPjAZ8wB6ev2mq700xSlurEjKF2J642SCnG8MtdUqd5SVOPCjWHLTbgjpNN/RPhjqFCvUIBrsZQ10IaaizkGDbjJc8ZlIbgJ2Nur8Nopm4odT5yTtWxgmEDZ6UaYZkQer+GpyxnsST1/U9YvvapSvj+P/kcb8mSUyTCSKke12h2ebUcR7xtHyqYvGCZOLnle5tPl7fjiMuRvM89T/8OH1L1XML915609jF9D6v+sZ084MkNtBtswFO7Y+SiMNStYfAnTg3nUBl6iFRzr67//HLzFowfSNq0cljdmwO4LpqKdo4gqf1TC/u0hoRKsk0Lwkz4OpDVdur+1rZ7y3lnAnrAfpm2TcvL+f/S+4AOuI+mtmP7gW/ZyLN837Z920VeAJ1G6Kruz/adAHsowAjmUeDZXgg820GhSmnQ4UQgGR00UpGajfa9FPLvsDV3nTm8nJ7ndb1UpHSiQR/Vm+Ed1DvpkS8lg2czX09Uf9JQOaGEbvjkUcjNxPL8iYAiq3fWw9rpFfLClDwVxMz6wjdpSUy4mE1oGR4E5WanbX7Wfy+Vi6XGdtLDcuul21gM7O39txXWwx/+evfpEOrqSxEsXWA66PaB39mpsNTdEE1f2oUf9qvYddR2WW4AywpY28G3WzJs4VuzvoNvUUhZvUVGl3JP0D27ZkCvATc7qM1ek2dBgFxEOTEC34U3HjvUCBKLGM6K0oR6brDy2k75WN8P4l/BF8kSRlZNrm9OcN21w28C+/aizkN3HNT/B2cvZ2dn/wC0hDuF0AwAAA==\"\n" +
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

        PutObjectRequest por = captor.getValue();

        JsonNode node = mapper.readTree(por.getInputStream());

        // some smoke tests basically, the main test is in the parser
        assertTrue(node.get(0).path("@@@@CloudWatch.owner").isTextual());
    }

    @Test
    public void testJsonUnwrappingWithJacksonNewLine() throws Exception {

        injectEnvironmentVariable("bucket_name", "somebucket");
        injectEnvironmentVariable("split", "true");

        CloudWatchPutRequest awsCloudWatchEvent = mapper.readValue(awsLogsEventNewLine, CloudWatchPutRequest.class);
        System.out.println(awsLogsEvent);

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);

        Handler handler = new Handler();
        handler.setS3Client(s3Client);
        handler.handleRequest(awsCloudWatchEvent, context);
        verify(s3Client).putObject(captor.capture());

        PutObjectRequest por = captor.getValue();

        JsonNode node = mapper.readTree(por.getInputStream());

        // some smoke tests basically, the main test is in the parser
        assertTrue(node.get(0).path("@@@@CloudWatch.owner").isTextual());


    }
}