/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.io.Serializable;

/**
 * Object permissions to show to user
 * 
 * @author Luis Faria
 * 
 */
public class ObjectPermissions implements Serializable {

  /**
   * No access to object
   */
  public static ObjectPermissions NoAccess = new ObjectPermissions(false, false, false, false);
  /**
   * Can only read metadata and access disseminations
   */
  public static ObjectPermissions ReadOnly = new ObjectPermissions(true, false, false, false);
  /**
   * ReadOnly plus it can edit descriptive metadata
   */
  public static ObjectPermissions ReadAndEditMetadata = new ObjectPermissions(true, true, false, false);
  /**
   * Can read, edit and remove metadata and objects and can grant permissions to
   * other users
   */
  public static ObjectPermissions FullControl = new ObjectPermissions(true, true, true, true);

  private static ObjectPermissions[] VALUES = new ObjectPermissions[] {NoAccess, ReadOnly, ReadAndEditMetadata,
    FullControl};

  /**
   * Get possible static values
   * 
   * @return the values
   */
  public static ObjectPermissions[] values() {
    return VALUES;
  }

  private boolean read;
  private boolean editMetadata;
  private boolean remove;
  private boolean grant;

  /**
   * Constructor for GWT serialization
   */
  public ObjectPermissions() {
    this.read = false;
    this.editMetadata = false;
    this.remove = false;
    this.grant = false;
  }

  /**
   * Create new Object permissions
   * 
   * @param read
   * @param editMetadata
   * @param remove
   * @param grant
   */
  public ObjectPermissions(boolean read, boolean editMetadata, boolean remove, boolean grant) {
    this.read = read;
    this.editMetadata = editMetadata;
    this.remove = remove;
    this.grant = grant;
  }

  /**
   * Can be read
   * 
   * @return true if can read
   */
  public boolean isRead() {
    return read;
  }

  /**
   * Can edit metadata
   * 
   * @return true if can edit metadata
   */
  public boolean isEditMetadata() {
    return editMetadata;
  }

  /**
   * Can remove
   * 
   * @return true if can remove
   */
  public boolean isRemove() {
    return remove;
  }

  /**
   * Can grant permissions
   * 
   * @return true if can grant permissions
   */
  public boolean isGrant() {
    return grant;
  }

  /**
   * Check equality to other object permissions
   * 
   * @param other
   * @return true if equal
   */
  public boolean equals(ObjectPermissions other) {
    boolean ret = true;
    ret &= (other.isRead() == isRead());
    ret &= (other.isEditMetadata() == isEditMetadata());
    ret &= (other.isRemove() == isRemove());
    ret &= (other.isGrant() == isGrant());
    return ret;
  }

  public String toString() {
    return "ObjectPermissions(" + isRead() + "," + isEditMetadata() + "," + isRemove() + "," + isGrant() + ")";
  }

  /**
   * Set read permission
   * 
   * @param read
   */
  public void setRead(boolean read) {
    this.read = read;
  }

  /**
   * Set edit metadata permission
   * 
   * @param editMetadata
   */
  public void setEditMetadata(boolean editMetadata) {
    this.editMetadata = editMetadata;
  }

  /**
   * Set remove permission
   * 
   * @param remove
   */
  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  /**
   * Set grant permission
   * 
   * @param grant
   */
  public void setGrant(boolean grant) {
    this.grant = grant;
  }
}
