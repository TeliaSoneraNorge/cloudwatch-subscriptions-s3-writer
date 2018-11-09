package com.telia.aws.cloudwatchtoremotebucket;


/**
 * Enum for log entry keys that CloudWatch add
 */
enum CloudWatchLogKey {

    LOG_GROUP("logGroup"), OWNER("owner"), LOG_STREAM("logStream"), SUBSCRIPTION_FILTERS("subscriptionFilters"),
    TIMESTAMP("timestamp"), ID("id");

    private static final String PREFFIX = "@@@@CloudWatch.";
    private String value;

    CloudWatchLogKey(String value) {
        this.value = value;
    }

    public static boolean isCloudWatchKey(String key) {
        return key.equals(LOG_GROUP) || key.equals(OWNER) || key.equals(SUBSCRIPTION_FILTERS);
    }

    @Override
    public String toString() {
        return value;
    }

    public String withPrefix() {
        return PREFFIX + value;
    }

}
