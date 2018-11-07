package com.camunda.consulting;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class ModelPropertyUtil {

  public static final String CAMUNDA_PROPERTY_SIM_NEXT_FIRE = "simNextFire";
  public static final String CAMUNDA_PROPERTY_SIM_GENERATE_PAYLOAD = "simGeneratePayload";

  public static Optional<String> getNextFire(ModelElementInstance elementInstance) {
    return readCamundaProperty((BaseElement) elementInstance, CAMUNDA_PROPERTY_SIM_NEXT_FIRE);
  }

  public static Optional<String> getNextFire(Element xmlElement) {
    return readCamundaProperty(xmlElement, CAMUNDA_PROPERTY_SIM_NEXT_FIRE);
  }

  public static Optional<String> readCamundaProperty(Element xmlElement, String propertyName) {
    Element extentionsElement = xmlElement.element("extensionElements");
    if (extentionsElement == null) {
      return Optional.empty();
    }
    Element camundaProperties = extentionsElement.elementNS(BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS, "properties");
    if (camundaProperties == null) {
      return Optional.empty();
    }

    List<Element> propertys = camundaProperties.elements("property");
    for (Element property : propertys) {
      if (propertyName.equals(property.attribute("name"))) {
        return Optional.of(property.attribute("value"));
      }
    }
    return Optional.empty();
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
