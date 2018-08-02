# Cloudwatch-subscriptions-s3-writer

Lambda function that can be used as a Cloud Watch subscription target. It decodes, decompresses, denormalizes and writes log data to an S3 bucket.


## The cloud watch event

The cloud watch event put to this Lambda function looks like this 

```json
 {
    "awslogs": {
    "data": "H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA=="
    }
}
```

The data payload is Gzipped and base64 encoded. Decoded and decompressed it looks like this. By default, logs are written to the S3 bucket and ingested by splunk in this format. Splunk then needs to split messages at index- or search time. 

```json
{
  "messageType": "DATA_MESSAGE",
  "owner": "123456789123",
  "logGroup": "testLogGroup",
  "logStream": "testLogStream",
  "subscriptionFilters": [
    "testFilter"
  ],
  "logEvents": [
    {
      "id": "eventId1",
      "timestamp": 1440442987000,
      "message": "[ERROR] First test message"
    },
    {
      "id": "eventId2",
      "timestamp": 1440442987001,
      "message": "[ERROR] Second test message"
    }
  ]
}


```

## Splitting and "denormalization"

As seen in the example event, Cloud watch batch many entries into one log event. To make life easier for consumers, log entries can be split in the lambda function.

If the environment variable *split_events* is set to *true* The lambda function maps one Cloud Watch event to many events for ingestion by splunk and attaches meta data to every one. 

The resulting JSON object, will then written to the S3 bucket. 

```json
[
  {
    "event": {
      "id": "eventId1",
      "message": "[ERROR] First test message",
      "timestamp": 1440442987000
    },
    "owner": "123456789123",
    "logGroup": "testLogGroup",
    "logStream": "testLogStream",
    "subscriptionFilters": [
      "testFilter"
    ]
  },
  {
    "event": {
      "id": "eventId2",
      "message": "[ERROR] Second test message",
      "timestamp": 1440442987001
    },
    "owner": "123456789123",
    "logGroup": "testLogGroup",
    "logStream": "testLogStream",
    "subscriptionFilters": [
      "testFilter"
    ]
  }
]
```

## How to build and deploy

### Build

The Lambda function builds with the provided gradle wrapper

```./gradlw.sh buildZip```

The lambda package is written to the build/distributions/ folder as a ZIP file. 
 
### Packckage and deploy

The lambda function can be deployed using the [
AWS Serverless Application Model (AWS SAM)](https://github.com/awslabs/aws-sam-cli)

```
# Package the applicatioon 
$ sam package --template-file template.yml --s3-bucket <<mybucket>> --output-template-file packaged.yaml
  
# Deploy packaged application using cloudformation
$ sam deploy --template-file ./packaged.yaml --stack-name mystack --capabilities CAPABILITY_IAM
```

### Local test

The lambda function can be tested locally with SAM Local, with the environment variable *dry run* set to *true*. 