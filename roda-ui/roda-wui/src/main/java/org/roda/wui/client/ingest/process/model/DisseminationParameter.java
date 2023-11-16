package org.roda.wui.client.ingest.process.model;

public class DisseminationParameter implements PrintableParameter {

  private String title = "Dissemination title";
  private String description = "Dissemination description";

  public DisseminationParameter() {
    // empty constructor
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String printAsParameter(String type) {
    return "type=" + type + ";title=" + getTitle() + ";description=" + getDescription();
  }
}
