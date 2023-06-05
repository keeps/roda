/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */
  @JsonRootName("vendor")
  public class Vendor implements Serializable{
    @Serial
    private static final long serialVersionUID = 8062643455231089592L;
    @JsonProperty("name")
    private String name = null;
    @JsonProperty("homepage")
    private String homepage = null;

    public Vendor(){
    }
    public Vendor(String name, String homepage) {
      this.name = name;
      this.homepage = homepage;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getHomepage() {
      return homepage;
    }

    public void setHomepage(String homepage) {
      this.homepage = homepage;
    }
  }
