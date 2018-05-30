package com.telia.aws.cloudwatchtoremotebucket;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.defaultClient;
import static com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl;
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

    @Override
    public String handleRequest(CloudWatchPutRequest event, Context context) {

        final String targetBucketName = System.getenv(BUCKET_NAME);
        if (targetBucketName == null) {
            throw new RuntimeException("Lambda is missing environment variable " + BUCKET_NAME);
        }
        final byte[] decoded = getMimeDecoder().decode(event.getAwslogs().getData());
        try {
            final PutObjectRequest req =
                    new PutObjectRequest(targetBucketName, UUID.randomUUID().toString(),
                            new GZIPInputStream(new ByteArrayInputStream(decoded)), null)
                            .withCannedAcl(BucketOwnerFullControl);
            return defaultClient().putObject(req).getContentMd5();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}