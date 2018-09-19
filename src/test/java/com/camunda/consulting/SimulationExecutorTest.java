package com.camunda.consulting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.historyService;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
@Deployment(resources = "executorTestModel.bpmn")
public class SimulationExecutorTest {

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  public void testBasicCycleStart() {
    SimulationExecutor.execute(DateTime.now().plusHours(1).toDate(), DateTime.now().plusHours(2).toDate());

    long count = historyService().createHistoricProcessInstanceQuery().processDefinitionKey("oneStartPerMinute").completed().count();
    assertThat(count).isEqualTo(61);
  }

}
