package com.github.mvysny.vaadinboot.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvTest {

    @Test
    public void flowBuildInfoJsonParsing() {
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\": true}"));
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":true}"));
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":\ntrue}"));
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\": true,\"foo\":\"bar\"}"));
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":true,\"foo\":\"bar\"}"));
        assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":\ntrue,\"foo\":\"bar\"}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\": false}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":false}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":\nfalse}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\": false, \"foo\":true}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":false, \"foo\":true}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{\"productionMode\":\nfalse, \"foo\":true}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("{}"));
        assertFalse(Env.flowBuildInfoJsonContainsProductionModeTrue("invalid json"));
    }

    @Test
    public void smokeFindWebRoot() throws Exception {
        assertNotNull(Env.findWebRoot());
    }
}
