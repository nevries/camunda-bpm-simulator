package com.camunda.consulting;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public class SimulatorPlugin implements ProcessEnginePlugin {

  private static ProcessEngineConfigurationImpl processEngineConfiguration;
  private static ProcessEngine processEngine;

  protected static ProcessEngine getProcessEngine() {
    return processEngine;
  }

  protected static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    SimulatorPlugin.processEngineConfiguration = processEngineConfiguration;
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    SimulatorPlugin.processEngine = processEngine;
  }

}
