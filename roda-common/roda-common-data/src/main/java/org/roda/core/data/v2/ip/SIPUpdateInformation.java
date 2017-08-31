package org.roda.core.data.v2.ip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SIPUpdateInformation {
  private Map<String, Map<String, List<String>>> updatedData = null;

  public SIPUpdateInformation() {
    updatedData = new HashMap<>();
  }

  public Map<String, Map<String, List<String>>> getUpdatedData() {
    return updatedData;
  }

  public void setUpdatedData(Map<String, Map<String, List<String>>> updatedData) {
    this.updatedData = updatedData;
  }
}
