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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_LOGS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogEntries implements RODAObjectList<LogEntry> {
  private static final long serialVersionUID = 5324915190817055434L;
  private List<LogEntry> logList;

  public LogEntries() {
    super();
    logList = new ArrayList<>();
  }

  public LogEntries(List<LogEntry> logEntries) {
    super();
    this.logList = logEntries;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_LOGS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_LOG)
  public List<LogEntry> getObjects() {
    return logList;
  }

  @Override
  public void setObjects(List<LogEntry> logEntries) {
    this.logList = logEntries;
  }

  @Override
  public void addObject(LogEntry logEntry) {
    this.logList.add(logEntry);
  }

}
