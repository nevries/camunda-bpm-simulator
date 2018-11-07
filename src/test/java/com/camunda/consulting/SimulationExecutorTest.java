package com.camunda.consulting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.historyService;

import java.util.List;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
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
    // have it already there when engine is built
    Mocks.register("generator", new PayloadGenerator());
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  public void testBasicCycleStart() {
    SimulationExecutor.execute(DateTime.now().plusHours(1).toDate(), DateTime.now().plusHours(2).toDate());

    long count = historyService().createHistoricProcessInstanceQuery().processDefinitionKey("oneStartPerMinute").completed().count();
    assertThat(count).isEqualTo(60);
  }

  @Test
  public void testHistoricSimulation() {
    SimulationExecutor.execute(DateTime.now().plusHours(-2).toDate(), DateTime.now().plusHours(-1).toDate());
    long count = historyService().createHistoricProcessInstanceQuery().processDefinitionKey("oneStartPerMinute").completed().count();
    assertThat(count).isEqualTo(60);
  }

  @Test
  @Deployment(resources = "simulateStartTestModel.bpmn")
  public void testSimulateStartEvent() {
    SimulationExecutor.execute(DateTime.now().plusHours(-1).toDate(), DateTime.now().toDate());
    long count = historyService().createHistoricProcessInstanceQuery().processDefinitionKey("oneStartPerMinute").completed().count();
    List<HistoricProcessInstance> list = historyService().createHistoricProcessInstanceQuery().processDefinitionKey("oneStartPerMinute").completed().list();
    for (HistoricProcessInstance historicProcessInstance : list) {
      assertThat(historicProcessInstance.getBusinessKey()).isNotEmpty();
    }
    long countFirst = 0;
    long countSecond = 0;
    List<HistoricVariableInstance> list2 = historyService().createHistoricVariableInstanceQuery().processDefinitionKey("oneStartPerMinute").list();
    for (HistoricVariableInstance historicVariableInstance : list2) {
      if (historicVariableInstance.getName().equals("first")) {
        countFirst++;
        assertThat(historicVariableInstance.getValue()).isEqualTo((int)1);
      }
      if (historicVariableInstance.getName().equals("second")) {
        countSecond++;
        assertThat(historicVariableInstance.getValue()).isEqualTo("2");
      }
    }
    assertThat(countFirst).isEqualTo(59);
    assertThat(countSecond).isEqualTo(59);
    assertThat(count).isEqualTo(59);
  }
}
