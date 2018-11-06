package com.telia.aws.cloudwatchtoremotebucket;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class can be used to configure the global object mapper. The instantiation of this object is
 * rather expensive, hance the shared instance. According to documentation, it's  thread safe as long
 * as threads do not atempt to modify it's configuration.
 *
 */
class JacksonConfiguration {

    static ObjectMapper mapper = new ObjectMapper();


}
