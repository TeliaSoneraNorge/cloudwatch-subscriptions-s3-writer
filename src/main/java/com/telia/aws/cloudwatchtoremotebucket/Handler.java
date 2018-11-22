package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.defaultClient;
import static com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl;
import static com.telia.aws.cloudwatchtoremotebucket.ExtendedCloudWatchLogEvent.from;
import static com.telia.aws.cloudwatchtoremotebucket.LogsDecoder.fromBase64EncodedZippedPayload;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;
import static java.util.Base64.getMimeDecoder;

/**
 * This lambda receives incoming Cloudwatch Events and writes them to an S3 bucket.
 * This bucket will typically be in a shared AWS account dedicated to log data.
 * <p>
 * The bucket name can be set in a lambda function environment variable called
 * bucket_name
 * <p>
 * For details, ee README
 */
public class Handler implements RequestHandler<CloudWatchPutRequest, String> {

    private static final String BUCKET_NAME = "bucket_name";
    private static final String SPLIT_ENTRIES = "split";

    private AmazonS3 s3Client = defaultClient();
    private ObjectMapper objectMapper = JacksonConfiguration.mapper;


    @Override
    public String handleRequest(CloudWatchPutRequest event, Context context) {

        final String targetBucketName = getenv(BUCKET_NAME);
        final boolean split = parseBoolean(getenv(SPLIT_ENTRIES));

        if (targetBucketName == null) {
            throw new RuntimeException("Lambda is missing environment variable " + BUCKET_NAME);
        }

        final CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        InputStream dataStream;

        if (split) {
            final List<ExtendedCloudWatchLogEvent> denormalizedEvents = from(events);
            try {
                dataStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(denormalizedEvents));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            dataStream = cwPayloadToStream(event);
        }

        final UUID key = UUID.randomUUID();
        String objectName = context.getInvokedFunctionArn() + "/" + key.toString();
        final PutObjectRequest req =
                new PutObjectRequest(targetBucketName, objectName,
                        dataStream, null)
                        .withCannedAcl(BucketOwnerFullControl);
        s3Client.putObject(req);
        return key.toString();
    }

    /**
     * Converts the compressed Cloud Watch event data to a readable input stream
     *
     * @param event the cloud watch put request event
     * @return an Inputstream
     */
    private GZIPInputStream cwPayloadToStream(CloudWatchPutRequest event) {
        try {
            return new GZIPInputStream(new ByteArrayInputStream(getMimeDecoder().decode(event.getAwslogs().getData())));
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    void setS3Client(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }
}