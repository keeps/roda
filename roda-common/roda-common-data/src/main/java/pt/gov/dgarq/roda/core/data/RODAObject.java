package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

import pt.gov.dgarq.roda.core.common.InvalidStateException;
import pt.gov.dgarq.roda.core.common.WrongCModelException;

/**
 * This is a RODA Object. A RODA Object is a Fedora Object that has a PID, a
 * label and a contentModel that starts with 'roda:'.
 * 
 * @author Rui Castro
 */
public class RODAObject implements Serializable {
  private static final long serialVersionUID = 2359726657667866639L;

  /**
   * Inactive state
   */
  public static String STATE_INACTIVE = "inactive";

  /**
   * Active state
   */
  public static String STATE_ACTIVE = "active";

  /**
   * Deleted state
   */
  public static String STATE_DELETED = "deleted";

  /**
   * Possible states
   */
  public static String[] STATES = new String[] {STATE_INACTIVE, STATE_ACTIVE, STATE_DELETED};

  private String pid = null;

  private String label = null;

  private String contentModel = null;

  private Date lastModifiedDate = null;

  private Date createdDate = null;

  private String state = null;

  /**
   * Constructs an empty RODAObject.
   */
  public RODAObject() {
    setState(STATE_ACTIVE);
  }

  /**
   * Constructs a {@link RODAObject} cloning an existing {@link RODAObject}.
   * 
   * @param object
   *          the {@link RODAObject} to be cloned.
   */
  public RODAObject(RODAObject object) {
    this(object.getPid(), object.getLabel(), object.getContentModel(), object.getLastModifiedDate(), object
      .getCreatedDate(), object.getState());
  }

  /**
   * Constructs a new RODAObject.
   * 
   * @param pid
   *          the PID of the fedora object.
   * @param label
   *          the label of the object.
   * @param contentModel
   *          the content model.
   */
  public RODAObject(String pid, String label, String contentModel) {
    this(pid, label, contentModel, null, null, STATE_ACTIVE);
  }

  /**
   * Constructs a new RODAObject.
   * 
   * @param pid
   *          the PID of the Fedora object.
   * @param label
   *          the label of the object.
   * @param contentModel
   *          the content model.
   * @param lastModifiedDate
   *          the date of the last modification.
   * @param createdDate
   * @param state
   */
  public RODAObject(String pid, String label, String contentModel, Date lastModifiedDate, Date createdDate, String state) {
    setPid(pid);
    setLabel(label);
    setContentModel(contentModel);
    setLastModifiedDate(lastModifiedDate);
    setCreatedDate(createdDate);
    setState(state);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof RODAObject) {
      RODAObject other = (RODAObject) obj;
      return getPid() == other.getPid() || getPid().equals(other.getPid());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RODAObject (PID=" + getPid() + ", label=" + getLabel() + ", contentModel=" + getContentModel()
      + ", lastModifiedDate=" + getLastModifiedDate() + ", createdDate=" + getCreatedDate() + ", state=" + getState()
      + ")";
  }

  /**
   * @return the contentModel
   */
  public String getContentModel() {
    return contentModel;
  }

  /**
   * @param contentModel
   *          the contentModel to set
   */
  public void setContentModel(String contentModel) {
    String[] parts = contentModel.split(":");

    if (parts.length < 2) {
      throw new IllegalArgumentException(contentModel + " is not a valid contentModel");
    } else {
      if (!parts[0].equalsIgnoreCase("roda")) {
        throw new WrongCModelException("contentModel has to start with 'roda'");
      } else {
        this.contentModel = contentModel;
      }
    }
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * @param label
   *          the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * @return the pid
   */
  public String getPid() {
    return pid;
  }

  /**
   * @param pid
   *          the pid to set
   */
  public void setPid(String pid) {
    this.pid = pid;
  }

  /**
   * @return the lastModifiedDate
   */
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  /**
   * @param lastModifiedDate
   *          the lastModifiedDate to set
   */
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  /**
   * @return the createdDate
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @param createdDate
   *          the createdDate to set
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * @return the state
   */
  public String getState() {
    return (state != null) ? state.toLowerCase() : null;
  }

  /**
   * @param state
   *          the state to set
   */
  public void setState(String state) {
    if (state == null) {
      this.state = STATE_ACTIVE;
    } else if (Arrays.asList(STATES).contains(state.toLowerCase())) {
      this.state = state.toLowerCase();
    } else {
      throw new InvalidStateException("'" + state + "' is not a valid state.");
    }
  }
}
