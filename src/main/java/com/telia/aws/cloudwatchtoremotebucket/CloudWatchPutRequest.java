package com.telia.aws.cloudwatchtoremotebucket;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CloudWatchPutRequest {
    private AWSLogs awslogs;
}

@NoArgsConstructor
@Data
class AWSLogs {
    private String data;
}