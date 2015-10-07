package org.roda.core.data;

import java.util.Date;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Text;

/**
 * This class contains the basic information about a Description Object.
 * 
 * @author Rui Castro
 */
public class SimpleDescriptionObject extends RODAObject {
  private static final long serialVersionUID = 38813680938917204L;

  public static final String LEVEL = "level";
  public static final String COUNTRYCODE = "countryCode";
  public static final String REPOSITORYCODE = "repositoryCode";
  public static final String ID = "id";
  public static final String TITLE = "title";
  public static final String DATE_INITIAL = "dateInitial";
  public static final String DATE_FINAL = "dateFinal";

  // FIXME this should be defined from DescriptionLevelManager information
  public static final Filter FONDS_FILTER = new Filter(new SimpleFilterParameter(LEVEL,
    DescriptionLevel.FONDS.getLevel()));

  private static final String[] ELEMENTS = new String[] {ID, COUNTRYCODE, REPOSITORYCODE, LEVEL, TITLE, DATE_INITIAL,
    DATE_FINAL};

  /**
   * @return an array of {@link String}s with the names of all elements.
   */
  public static String[] getAllElements() {
    return ELEMENTS;
  }

  // private String id = null; // id is the super.label
  private DescriptionLevel level = null;
  private String countryCode = null;
  private String repositoryCode = null;
  private String title = null;
  private String dateInitial = null;
  private String dateFinal = null;

  private String description = null;

  private String parentPID = null;
  private int subElementsCount = 0;

  /**
   * Constructs an empty (<strong>invalid</strong>)
   * {@link SimpleDescriptionObject}.
   */
  public SimpleDescriptionObject() {
    super(null, null, "roda:d");
  }

  /**
   * Constructs a new (<strong>invalid</strong>) {@link SimpleDescriptionObject}
   * from a {@link RODAObject}.
   * 
   * @param rodaObject
   */
  public SimpleDescriptionObject(RODAObject rodaObject) {
    super(rodaObject);
  }

  /**
   * Constructs a new {@link SimpleDescriptionObject} cloning the one given by
   * argument.
   * 
   * @param other
   *          the {@link SimpleDescriptionObject} to be cloned.
   */
  public SimpleDescriptionObject(SimpleDescriptionObject other) {
    this(other.getPid(), other.getLabel(), other.getContentModel(), other.getLastModifiedDate(),
      other.getCreatedDate(), other.getState(), other.getLevel(), other.getCountryCode(), other.getRepositoryCode(),
      other.getId(), other.getTitle(), other.getDateInitial(), other.getDateFinal(), other.getDescription(), other
        .getParentPID(), other.getSubElementsCount());
  }

  /**
   * Constructs a new SimpleDescriptionObject with the given arguments.
   * 
   * @param pid
   * @param label
   * @param contentModel
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param level
   * @param countryCode
   * @param repositoryCode
   * @param id
   * @param title
   * @param dateInitial
   * @param dateFinal
   * @param description
   * @param parentPID
   * @param subElementsCount
   */
  public SimpleDescriptionObject(String pid, String label, String contentModel, Date lastModifiedDate,
    Date createdDate, String state, DescriptionLevel level, String countryCode, String repositoryCode, String id,
    String title, String dateInitial, String dateFinal, String description, String parentPID, int subElementsCount) {

    super(pid, label, contentModel, lastModifiedDate, createdDate, state);

    if (id != null) {
      setId(id);
    } else {
      setId(getLabel());
    }

    setLevel(level);
    setCountryCode(countryCode);
    setRepositoryCode(repositoryCode);
    setTitle(title);
    setDateInitial(dateInitial);
    setDateFinal(dateFinal);

    setDescription(description);

    setParentPID(parentPID);
    setSubElementsCount(subElementsCount);
  }

  /**
   * @see RODAObject#toString()
   */
  // @Override
  public String toString() {
    return "SimpleDescriptionObject(" + super.toString() + ", " + "level=" + getLevel() + ", countryCode="
      + getCountryCode() + ", repositoryCode=" + getRepositoryCode() + ", id=" + getId() + ", title=" + getTitle()
      + ", dateInitial=" + getDateInitial() + ", dateFinal=" + getDateFinal() + ", description=" + getDescription()
      + ", parentPID=" + getParentPID() + ", childCount=" + getSubElementsCount() + ")";
  }

  /**
   * @param element
   * @return the EAD-C value
   * @throws IllegalArgumentException
   */
  public EadCValue getValue(String element) throws IllegalArgumentException {

    EadCValue value = null;

    if (LEVEL.equals(element)) {
      value = getLevel();
    } else if (COUNTRYCODE.equals(element)) {
      value = getTextOrNull(getCountryCode());
    } else if (REPOSITORYCODE.equals(element)) {
      value = getTextOrNull(getRepositoryCode());
    } else if (ID.equals(element)) {
      value = getTextOrNull(getId());
    } else if (TITLE.equals(element)) {
      value = getTextOrNull(getTitle());
    } else if (DATE_INITIAL.equals(element)) {
      value = getTextOrNull(getDateInitial());
    } else if (DATE_FINAL.equals(element)) {
      value = getTextOrNull(getDateFinal());
    } else {
      // no value named 'element'
      throw new IllegalArgumentException("Unknown element named " + element);
    }

    return value;
  }

  /**
   * @param element
   * @param value
   * @throws IllegalArgumentException
   */
  public void setValue(String element, EadCValue value) throws IllegalArgumentException {

    // if (COMPLETE_REFERENCE.equals(element)) {
    // setCompleteReference(checkEadCText(element, value));
    // } else

    if (LEVEL.equals(element)) {
      setLevel(checkDescriptionLevel(element, value));
    } else if (COUNTRYCODE.equals(element)) {
      setCountryCode(checkEadCText(element, value));
    } else if (REPOSITORYCODE.equals(element)) {
      setRepositoryCode(checkEadCText(element, value));
    } else if (ID.equals(element)) {
      setId(checkEadCText(element, value));
    } else if (TITLE.equals(element)) {
      setTitle(checkEadCText(element, value));
    } else if (DATE_INITIAL.equals(element)) {
      setDateInitial(checkEadCText(element, value));
    } else if (DATE_FINAL.equals(element)) {
      setDateFinal(checkEadCText(element, value));
    } else {
      // no value named 'element'
      throw new IllegalArgumentException("Unknown element named " + element);
    }
  }

  /**
   * Gets the description level of this DO.
   * 
   * @return a {@link DescriptionLevel} with the level of this DO.
   */
  public DescriptionLevel getLevel() {
    return this.level;
  }

  /**
   * Sets the level of this DO.
   * 
   * @param level
   *          the level to set.
   */
  public void setLevel(DescriptionLevel level) {
    this.level = level;
  }

  /**
   * @return the countryCode
   */
  public String getCountryCode() {
    return countryCode;
  }

  /**
   * @param countryCode
   *          the countryCode to set
   */
  public void setCountryCode(String countryCode) {
    if (countryCode == null) {
      throw new NullPointerException("countryCode cannot be null");
    } else {
      this.countryCode = countryCode;
    }
  }

  /**
   * @return the repositoryCode
   */
  public String getRepositoryCode() {
    return repositoryCode;
  }

  /**
   * @param repositoryCode
   *          the repositoryCode to set
   */
  public void setRepositoryCode(String repositoryCode) {
    if (repositoryCode == null) {
      throw new NullPointerException("repositoryCode cannot be null");
    } else {
      this.repositoryCode = repositoryCode;
    }
  }

  /**
   * Gets the unitID of this Description Object.
   * 
   * @return a String containing the unitID of this Descriptive Object.
   */
  public String getId() {
    return getLabel();
  }

  /**
   * @param id
   */
  public void setId(String id) {
    if (id == null) {
      throw new NullPointerException("id cannot be null");
    } else {
      // this.id = id;
      setLabel(id);
    }
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
    if (title == null) {
      throw new NullPointerException("title cannot be null");
    } else {
      this.title = title;
    }
  }

  /**
   * Return the initial date of this Descriptive Object.
   * 
   * @return a {@link String} with the initial date or <code>null</code> if it
   *         doesn't exist.
   */
  public String getDateInitial() {
    return dateInitial;
  }

  /**
   * @param dateInitial
   */
  public void setDateInitial(String dateInitial) {
    this.dateInitial = dateInitial;
  }

  /**
   * Return the final date of this Descriptive Object.
   * 
   * @return a {@link String} with the final date or <code>null</code> if it
   *         doesn't exist.
   */
  public String getDateFinal() {
    return dateFinal;
  }

  /**
   * @param dateFinal
   */
  public void setDateFinal(String dateFinal) {
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
   * @return the parentPID
   */
  public String getParentPID() {
    return parentPID;
  }

  /**
   * @param parentPID
   *          the parentPID to set
   */
  public void setParentPID(String parentPID) {
    this.parentPID = parentPID;
  }

  protected EadCValue getTextOrNull(String value) {
    Text textValue = null;
    if (value != null) {
      textValue = new Text(value);
    }
    return textValue;
  }

  protected String checkEadCText(String element, EadCValue value) throws IllegalArgumentException {

    if (value == null) {
      return null;
    } else if (value instanceof Text) {
      return value.toString();
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type " + Text.class
        + " was expected.");
    }
  }

  protected DescriptionLevel checkDescriptionLevel(String element, EadCValue value) throws IllegalArgumentException {

    if (value == null) {
      return null;
    } else if (value instanceof DescriptionLevel) {
      return (DescriptionLevel) value;
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type "
        + DescriptionLevel.class + " was expected.");
    }
  }

}
