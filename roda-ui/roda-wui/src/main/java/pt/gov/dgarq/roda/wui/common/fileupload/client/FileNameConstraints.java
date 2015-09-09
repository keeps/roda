/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.fileupload.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * @author Luis Faria
 * 
 */
public class FileNameConstraints {

  /**
   * Default filename constraint that allows an infinite of files with any file
   * name extension
   */
  public static FileNameConstraints DEFAULT_FILENAME_CONSTRAINT = new FileNameConstraints() {
    {
      addConstraint(null, -1);
    }
  };

  /**
   * File name constraint
   */
  public class FileNameConstraint {
    private Set<String> extentions;
    private int count;

    /**
     * Create a new file name constraint
     * 
     * @param extensions
     *          possible extensions
     * @param count
     *          maximum number of files with this extension, -1 for infinite
     */
    public FileNameConstraint(Set<String> extensions, int count) {
      this.extentions = extensions;
      this.count = count;
    }

    /**
     * Create a new file name constraint
     * 
     * @param constraint
     * 
     */
    public FileNameConstraint(FileNameConstraint constraint) {
      this.extentions = (constraint != null && constraint.getExtensions() != null ? new HashSet<String>(
        constraint.getExtensions()) : null);
      this.count = constraint.getCount();
    }

    /**
     * Get possible extensions
     * 
     * @return
     */
    public Set<String> getExtensions() {
      return extentions;
    }

    /**
     * Set possible extensions
     * 
     * @param extensions
     */
    public void setExtensions(Set<String> extensions) {
      this.extentions = extensions;
    }

    /**
     * Get the maximum number of files with this extension, -1 for infinite
     * 
     * @return
     */
    public int getCount() {
      return count;
    }

    /**
     * Set the maximum number of files with this extension, -1 for infinite
     * 
     * @param count
     */
    public void setCount(int count) {
      this.count = count;
    }

    @Override
    public String toString() {
      return "Filename constraint extensions=" + extentions + " count=" + count;
    }

  }

  private List<FileNameConstraint> constraints;

  /**
   * Create new file name constraints
   */
  public FileNameConstraints() {
    this.constraints = new Vector<FileNameConstraint>();
  }

  /**
   * Create new file name constraints
   * 
   * @param constraints
   */
  public FileNameConstraints(List<FileNameConstraint> constraints) {
    this.constraints = constraints;
  }

  /**
   * Create new file name constraints
   * 
   * @param constraints
   */
  public FileNameConstraints(FileNameConstraints constraints) {
    this.constraints = new Vector<FileNameConstraint>();
    for (FileNameConstraint constraint : constraints.getConstraints()) {
      this.constraints.add(new FileNameConstraint(constraint));
    }
  }

  /**
   * Get all file name constraints
   * 
   * @return
   */
  public List<FileNameConstraint> getConstraints() {
    return constraints;
  }

  /**
   * Set all file name constraints
   * 
   * @param constraints
   */
  public void setConstraints(List<FileNameConstraint> constraints) {
    this.constraints = constraints;
  }

  /**
   * Add a new file name constraint
   * 
   * @param extensions
   * @param count
   */
  public void addConstraint(String[] extensions, int count) {
    if (extensions != null) {
      constraints.add(new FileNameConstraint(new HashSet<String>(Arrays.asList(extensions)), count));
    } else {
      constraints.add(new FileNameConstraint(null, count));
    }
  }

  public String toString() {
    return constraints.toString();
  }

}
