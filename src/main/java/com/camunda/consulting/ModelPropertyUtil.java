package com.camunda.consulting;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class ModelPropertyUtil {
  
  public static final String CAMUNDA_PROPERTY_SIM_NEXT_FIRE = "simNextFire";
  public static final String CAMUNDA_PROPERTY_SIM_GENERATE_PAYLOAD = "simGeneratePayload";

  public static Optional<String> getNextFire(ModelElementInstance elementInstance) {
    return readCamundaProperty((BaseElement)elementInstance, CAMUNDA_PROPERTY_SIM_NEXT_FIRE);
  }
  
  public static Optional<String> readCamundaProperty(BaseElement modelElementInstance, String propertyName) {
    if (modelElementInstance.getExtensionElements() == null) {
      return Optional.empty();
    }
    return queryCamundaPropertyValues(modelElementInstance, propertyName).findFirst();
  }

  public static Collection<String> readCamundaPropertyMulti(BaseElement modelElementInstance, String propertyName) {
    if (modelElementInstance.getExtensionElements() == null) {
      return Collections.emptyList();
    }
    return queryCamundaPropertyValues(modelElementInstance, propertyName).collect(Collectors.toList());
  }

  protected static Stream<String> queryCamundaPropertyValues(BaseElement modelElementInstance, String propertyName) {
    return modelElementInstance.getExtensionElements().getElementsQuery().filterByType(CamundaProperties.class).list().stream() //
        .map(CamundaProperties::getCamundaProperties) //
        .flatMap(Collection::stream) //
        .filter(property -> property.getCamundaName().equals(propertyName)) //
        .map(CamundaProperty::getCamundaValue) //
        .filter(Objects::nonNull) //
    ;
  }
}
