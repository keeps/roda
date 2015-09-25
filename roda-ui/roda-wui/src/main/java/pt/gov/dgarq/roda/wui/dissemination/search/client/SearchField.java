package pt.gov.dgarq.roda.wui.dissemination.search.client;

import java.io.Serializable;
import java.util.Map;

public class SearchField  implements Serializable{
  private static final long serialVersionUID = -2809811191632936028L;
  private String field;
  private Map<String,String> labels;
  private String type;
  
  public String getField() {
    return field;
  }
  public void setField(String field) {
    this.field = field;
  }
  public Map<String, String> getLabels() {
    return labels;
  }
  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
}
