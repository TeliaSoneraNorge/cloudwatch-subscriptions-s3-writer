package com.telia.aws.cloudwatchtoremotebucket;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AccountTest {

    @Test
    public void getAccountIdFromARN() {

        Arn a = new Arn("arn:aws:lambda:eu-west-1:369412094037:function:sample_log_forwarder");
        assertEquals("369412094037", a.getAccountId());

    }

}
