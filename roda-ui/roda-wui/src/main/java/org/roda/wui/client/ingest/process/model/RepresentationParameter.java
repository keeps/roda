/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.ingest.process.model;

public class RepresentationParameter implements PrintableParameter {

  private String value;
  private boolean markAsPreservation = true;

  public RepresentationParameter() {
    // empty constructor
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public boolean isMarkAsPreservation() {
    return markAsPreservation;
  }

  public void setMarkAsPreservation(boolean markAsPreservation) {
    this.markAsPreservation = markAsPreservation;
  }

  @Override
  public String printAsParameter(String type) {
    return "type=" + type + ";value=" + getValue() + ";markAsPreservation=" + isMarkAsPreservation();
  }
}
