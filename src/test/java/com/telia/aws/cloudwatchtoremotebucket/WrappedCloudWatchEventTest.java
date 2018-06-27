package com.telia.aws.logs;

import com.telia.aws.cloudwatchtoremotebucket.UnWrappedCloudWatchLogEvent;
import org.junit.Test;

public class WrappedCloudWatchEventTest {


    private String awsLogsEvent = "{\n" +
            " \"awslogs\": {\n" +
            " \"data\": \"H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA==\"\n" +
            " }\n" +
            " }"

    @Test
    public void shouldDecodeAndUnwrap() {

        UnWrappedCloudWatchLogEvent

    }

}
