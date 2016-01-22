/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Date;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.deprecated.RODAObject;
import org.roda.core.data.eadc.DescriptionLevel;

/**
 * This class contains the indexed information about an AIP.
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class IndexedAIP extends RODAObject {
  private static final long serialVersionUID = 38813680938917204L;

  // FIXME this should be defined from DescriptionLevelManager information
  public static final Filter FONDS_FILTER = new Filter(new SimpleFilterParameter("level", "fonds"));

  private String level = null;
  private String title = null;
  private Date dateInitial = null;
  private Date dateFinal = null;

  private String description = null;

  private String parentID = null;
  private int subElementsCount = 0;

  private RODAObjectPermissions permissions = new RODAObjectPermissions();

  /**
   * Constructs an empty (<strong>invalid</strong>) {@link IndexedAIP}.
   */
  public IndexedAIP() {
    super(null, null);
  }

  /**
   * Constructs a new (<strong>invalid</strong>) {@link IndexedAIP} from a
   * {@link RODAObject}.
   * 
   * @param rodaObject
   */
  public IndexedAIP(RODAObject rodaObject) {
    super(rodaObject);
  }

  /**
   * Constructs a new {@link IndexedAIP} cloning the one given by argument.
   * 
   * @param other
   *          the {@link IndexedAIP} to be cloned.
   */
  public IndexedAIP(IndexedAIP other) {
    this(other.getId(), other.getLabel(), other.getLastModifiedDate(), other.getCreatedDate(), other.getState(),
      other.getLevel(), other.getTitle(), other.getDateInitial(), other.getDateFinal(), other.getDescription(),
      other.getParentID(), other.getSubElementsCount(), other.getPermissions());
  }

  /**
   * Constructs a new SimpleDescriptionObject with the given arguments.
   * 
   * @param id
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param level
   * @param id
   * @param title
   * @param dateInitial
   * @param dateFinal
   * @param description
   * @param parentID
   * @param subElementsCount
   */
  public IndexedAIP(String id, String label, Date lastModifiedDate, Date createdDate, String state, String level,
    String title, Date dateInitial, Date dateFinal, String description, String parentID, int subElementsCount,
    RODAObjectPermissions permissions) {

    super(id, label, lastModifiedDate, createdDate, state);

    if (id != null) {
      setId(id);
    } else {
      setId(getLabel());
    }

    setLevel(level);
    setTitle(title);
    setDateInitial(dateInitial);
    setDateFinal(dateFinal);

    setDescription(description);

    setParentID(parentID);
    setSubElementsCount(subElementsCount);

    setPermissions(permissions);
  }

  /**
   * @see RODAObject#toString()
   */
  @Override
  public String toString() {
    return "SimpleDescriptionObject(" + super.toString() + ", " + "level=" + getLevel() + ", id=" + getId() + ", title="
      + getTitle() + ", dateInitial=" + getDateInitial() + ", dateFinal=" + getDateFinal() + ", description="
      + getDescription() + ", parentID=" + getParentID() + ", childCount=" + getSubElementsCount() + ")";
  }

  // TODO:
  public String getValue(String element) throws IllegalArgumentException {

    return element;
  }

  /**
   * @param element
   * @param value
   * @throws IllegalArgumentException
   */
  public void setValue(String element, String value) throws IllegalArgumentException {
    // TODO:
  }

  /**
   * Gets the description level of this DO.
   * 
   * @return a {@link DescriptionLevel} with the level of this DO.
   */
  public String getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this DO.
   * 
   * @param level
   *          the level to set.
   */
  public void setLevel(String level) {
    this.level = level;
  }

  /**
   * Gets the unitID of this Description Object.
   * 
   * @return a String containing the unitID of this Descriptive Object.
   */
  @Override
  public String getId() {
    return getLabel();
  }

  /**
   * Gets the title of this Description Object.
   * 
   * @return a String containing the title of this Descriptive Object.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Return the initial date of this Descriptive Object.
   * 
   * @return a {@link String} with the initial date or <code>null</code> if it
   *         doesn't exist.
   */
  public Date getDateInitial() {
    return dateInitial;
  }

  /**
   * @param dateInitial
   */
  public void setDateInitial(Date dateInitial) {
    this.dateInitial = dateInitial;
  }

  /**
   * Return the final date of this Descriptive Object.
   * 
   * @return a {@link String} with the final date or <code>null</code> if it
   *         doesn't exist.
   */
  public Date getDateFinal() {
    return dateFinal;
  }

  /**
   * @param dateFinal
   */
  public void setDateFinal(Date dateFinal) {
    this.dateFinal = dateFinal;
  }

  /**
   * @return the number of sub elements (description objects)
   */
  public int getSubElementsCount() {
    return subElementsCount;
  }

  /**
   * @param count
   *          the number of sub elements (description objects) to set
   */
  public void setSubElementsCount(int count) {
    this.subElementsCount = count;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the parentID
   */
  public String getParentID() {
    return parentID;
  }

  /**
   * @param parentID
   *          the parentID to set
   */
  public void setParentID(String parentID) {
    this.parentID = parentID;
  }

  public RODAObjectPermissions getPermissions() {
    return permissions;
  }

  public void setPermissions(RODAObjectPermissions permissions) {
    this.permissions = permissions;
  }

}
