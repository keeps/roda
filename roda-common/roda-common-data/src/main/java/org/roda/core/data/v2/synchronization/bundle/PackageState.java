/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.synchronization.bundle;

import org.roda.core.data.v2.IsRODAObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PackageState implements Serializable {
  private static final long serialVersionUID = -8011849103435939717L;

  private Class<? extends IsRODAObject> className;
  private Status status = Status.CREATED;
  private int count = 0;
  private List<String> idList = new ArrayList<>();
  private String checksum;

  private String filePath;

  public enum Status {
    CREATED, FAILED, SUCCESS
  }

  public PackageState() {
  }

  public Class<? extends IsRODAObject> getClassName() {
    return className;
  }

  public void setClassName(Class<? extends IsRODAObject> className) {
    this.className = className;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public List<String> getIdList() {
    return idList;
  }

  public void setIdList(List<String> idList) {
    this.idList = idList;
  }

  public void addIdList(String idList){
    this.idList.add(idList);
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
