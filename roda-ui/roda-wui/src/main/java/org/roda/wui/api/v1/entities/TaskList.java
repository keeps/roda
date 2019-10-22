/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v1.entities;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;


@javax.xml.bind.annotation.XmlRootElement(name = "tasks")
public class TaskList {
  @XmlElements({@XmlElement(name = "task", type = String.class)})
  private List<String> tasks;

  public TaskList() {
    // do nothing
  }

  public TaskList(String... values) {
    tasks = Arrays.asList(values);
  }

  public List<String> getTasks() {
    return tasks;
  }

  public void setTasks(List<String> tasks) {
    this.tasks = tasks;
  }

}
