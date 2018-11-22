    package com.telia.aws.cloudwatchtoremotebucket;

    import com.amazonaws.services.lambda.runtime.Context;
    import com.amazonaws.services.s3.AmazonS3;
    import com.amazonaws.services.s3.model.PutObjectRequest;
    import com.amazonaws.util.IOUtils;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.mockito.ArgumentCaptor;
    import org.mockito.Mock;
    import org.mockito.runners.MockitoJUnitRunner;

    import java.io.IOException;

    import static com.telia.aws.cloudwatchtoremotebucket.EnvironmentUtils.injectEnvironmentVariable;
    import static com.telia.aws.cloudwatchtoremotebucket.JacksonConfiguration.mapper;
    import static com.telia.aws.cloudwatchtoremotebucket.LogsDecoder.fromBase64EncodedZippedPayload;
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
            " \"data\": \"H4sIAGGh9lsAA+1S3WqDMBi971OUXNsRE01N72RzZbDCqDIGyxBXQwmokSRuFPHdF9POuT7C2HcVOD/fOR/pF0s7QH42XIHNEvizAd4ZrORxq2TXjvhtJbsyU4WoZmhqFC/qa3n+w807veKFNqvJU3fv+qBEa4Rs7kVluNJW/+pAR9jzg2gFb8zF3CFvF3XNtS6OPDu1fNx6F2dxvkvSNN4ms1jJh5X/tu2nlyOJcpRjn4bYh4RAQgmhEcbrKIIUohCuaQQDZMsEGEWU+GGAAkSTl3j39Pi9anIzwuYyRT0eygkQicIQQnjFu6QfV/cM8DHls+1vL8HAhgH/BmIGPAY6zdVDaVFhThaxXGMLO85eSsPAACbjwfvv+Cc6nv/5YvgClUAhi5kDAAA=\"\n" +
            " }\n" +
            " }";


    /**
     * Basic Bas64 and Gzip decode test.
     */
    @Test
    public void shouldDecodeBase64EncodedAndGzippedPayload() throws IOException {
        CloudWatchPutRequest event = mapper.readValue(awsLogsEvent, CloudWatchPutRequest.class);
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        assertEquals(3, events.getLogEvents().size());
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


        System.out.println(IOUtils.toString(captor.getValue().getInputStream()));

    }


}