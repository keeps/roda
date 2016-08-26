/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.log;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "logentries")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntries implements RODAObjectList<LogEntry> {
  private List<LogEntry> logEntries;

  public LogEntries() {
    super();
    logEntries = new ArrayList<LogEntry>();
  }

  public LogEntries(List<LogEntry> logEntries) {
    super();
    this.logEntries = logEntries;
  }

  @JsonProperty(value = "logentries")
  @XmlElement(name = "logentry")
  public List<LogEntry> getObjects() {
    return logEntries;
  }

  public void setObjects(List<LogEntry> logEntries) {
    this.logEntries = logEntries;
  }

  public void addObject(LogEntry logEntry) {
    this.logEntries.add(logEntry);
  }

}
