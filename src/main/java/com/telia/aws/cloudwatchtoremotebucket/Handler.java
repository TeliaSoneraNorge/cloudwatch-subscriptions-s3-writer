package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.defaultClient;
import static com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl;
import static com.telia.aws.cloudwatchtoremotebucket.DenormalizedCloudwatchLogEvent.from;
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
 */
public class Handler implements RequestHandler<CloudWatchPutRequest, String> {

    private static final String BUCKET_NAME = "bucket_name";
    private static final String DENORMALIZE = "denormalize_metadata";

    private AmazonS3 s3Client = defaultClient();

    private final Gson gson = new Gson();

    @Override
    public String handleRequest(CloudWatchPutRequest event, Context context) {

        final String targetBucketName = getenv(BUCKET_NAME);
        if (targetBucketName == null) {
            throw new RuntimeException("Lambda is missing environment variable " + BUCKET_NAME);
        }
        CloudWatchLogEvents events = fromBase64EncodedZippedPayload(event.getAwslogs().getData());
        byte[] decoded;
        final boolean denormalizeMetadata = parseBoolean(getenv(DENORMALIZE));
        if (denormalizeMetadata) {
            final List<DenormalizedCloudwatchLogEvent> denormalizedEvents = from(events);
            decoded = gson.toJson(denormalizedEvents).getBytes();
        } else {
            decoded = getMimeDecoder().decode(event.getAwslogs().getData());
        }
        try {
            final PutObjectRequest req =
                    new PutObjectRequest(targetBucketName, UUID.randomUUID().toString(),
                            new GZIPInputStream(new ByteArrayInputStream(decoded)), null)
                            .withCannedAcl(BucketOwnerFullControl);
            s3Client.putObject(req); 
            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setS3Client(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }
}