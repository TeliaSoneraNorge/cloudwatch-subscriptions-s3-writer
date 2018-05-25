# Cloudwatch-subscriptions-s3-writer
Lambda function that can be used as a Cloud Watch subscription target. It decodes, decompresses and writes log data to an S3 bucket.

## How to build and deploy


### Build
The Lambda function builds with the provided gradle wrapper

```./gradlw.sh build```

### Packckage and deploy

During the build, the lambda package is written to the build/distributions/ folder as a ZIP file. The lambda function can be deployed using the [
AWS Serverless Application Model (AWS SAM)](https://github.com/awslabs/aws-sam-cli)

```
# Package the applicatioon 
$ sam package --template-file template.yml --s3-bucket <<mybucket>> --output-template-file packaged.yaml
  
# Deploy packaged application using cloudformation
$ sam deploy --template-file ./packaged.yaml --stack-name mystack --capabilities CAPABILITY_IAM
```



