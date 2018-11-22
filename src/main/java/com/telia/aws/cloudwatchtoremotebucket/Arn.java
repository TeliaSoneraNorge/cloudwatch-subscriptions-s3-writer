package com.telia.aws.cloudwatchtoremotebucket;

public class Arn {

    private String arn;

    public Arn(String arn) {
        this.arn = arn;
    }

    public String getAccountId() {
        return arn.split(":")[4];
    }

}


