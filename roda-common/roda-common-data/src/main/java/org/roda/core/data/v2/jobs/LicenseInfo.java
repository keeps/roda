package org.roda.core.data.v2.jobs;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LicenseInfo implements Serializable {
  private static final long serialVersionUID = -6199653832479993919L;
  private String name;
  private String url;

  public LicenseInfo() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
