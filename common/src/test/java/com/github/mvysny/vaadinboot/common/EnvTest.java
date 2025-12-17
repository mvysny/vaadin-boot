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
    assertTrue(Env.flowBuildInfoJsonContainsProductionModeTrue(
        "{ \"productionMode\" : true, \n\"eagerServerLoad\" : false, \n\"react.enable\" : true, \n\"applicationIdentifier\" : \"app-744cac70c6fae7de4d924e4d434cc3a64c190ddc3ad2fdaa7f7fd2465ea8a08d\" \n}"));
  }

  @Test
  public void smokeFindWebRoot() throws Exception {
    assertNotNull(Env.findWebRoot());
  }
}
