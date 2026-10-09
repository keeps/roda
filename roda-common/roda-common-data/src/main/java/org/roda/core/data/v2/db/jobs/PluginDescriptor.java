/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.db.jobs;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A plugin, identified by its class and version, as referenced by job reports
 * and their steps ({@code plugins}). Rows are shared by all jobs; the name is
 * the plugin's (possibly i18n key) name.
 *
 * @author RODA Development Team
 */
@Entity
@Table(name = "plugins")
public class PluginDescriptor implements Serializable {
  @Serial
  private static final long serialVersionUID = -6532512981360281452L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "class_name", nullable = false)
  private String className;

  @Column(name = "version", nullable = false, length = 64)
  private String version = "";

  @Column(name = "name", nullable = false)
  private String name;

  public PluginDescriptor() {
    // used by JPA and serialization
  }

  public PluginDescriptor(String className, String version, String name) {
    this.className = className;
    this.version = version == null ? "" : version;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
