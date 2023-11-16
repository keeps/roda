package org.roda.wui.client.ingest.process.model;

public class RepresentationParameter implements PrintableParameter {

  private String value;

  public RepresentationParameter() {
    // empty constructor
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String printAsParameter(String type) {
    return "type=" + type + ";value=" + getValue();
  }
}
