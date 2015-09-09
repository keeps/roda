package pt.gov.dgarq.roda.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.WrongCModelException;

/**
 * This is a {@link RepresentationObject}. It contains the information about a
 * representation.
 * 
 * @author Rui Castro
 */
public class RepresentationObject extends SimpleRepresentationObject {
  private static final long serialVersionUID = -6678725617872116912L;

  private RepresentationFile rootFile = null;
  private List<RepresentationFile> partFiles = new ArrayList<RepresentationFile>();

  /**
   * Constructs an empty {@link RepresentationObject}.
   */
  public RepresentationObject() {
  }

  /**
   * Constructs a new {@link RepresentationObject} cloning an existing
   * {@link SimpleRepresentationObject}.
   * 
   * @param simpleRO
   *          a {@link SimpleRepresentationObject}.
   */
  public RepresentationObject(SimpleRepresentationObject simpleRO) {
    super(simpleRO);
  }

  /**
   * Constructs a new {@link RepresentationObject} with the given parameters.
   * 
   * @param simpleRO
   * @param rootFile
   * @param partFiles
   */
  public RepresentationObject(SimpleRepresentationObject simpleRO, RepresentationFile rootFile,
    RepresentationFile[] partFiles) {
    super(simpleRO);
    setRootFile(rootFile);
    setPartFiles(partFiles);
  }

  /**
   * Constructs a new {@link RepresentationObject} cloning an existing
   * {@link RepresentationObject}.
   * 
   * @param rObject
   *          a Representation Object.
   */
  public RepresentationObject(RepresentationObject rObject) {
    this(rObject, rObject.getRootFile(), rObject.getPartFiles());
  }

  /**
   * Constructs a new {@link RepresentationObject} with the given parameters.
   * 
   * @param rodaObject
   * @param status
   * @param descriptionObjectPID
   * @param rootFile
   * @param partFiles
   */
  public RepresentationObject(RODAObject rodaObject, String status, String descriptionObjectPID,
    RepresentationFile rootFile, RepresentationFile[] partFiles) {
    this(rodaObject, new String[] {status}, descriptionObjectPID, rootFile, partFiles);
  }

  /**
   * Constructs a {@link RepresentationObject} with the given arguments.
   * 
   * @param object
   * @param statuses
   * @param descriptionObjectPID
   * @param rootFile
   * @param partFiles
   */
  public RepresentationObject(RODAObject object, String[] statuses, String descriptionObjectPID,
    RepresentationFile rootFile, RepresentationFile[] partFiles) {
    super(object, statuses, descriptionObjectPID);
    setRootFile(rootFile);
    setPartFiles(partFiles);
  }

  /**
   * Constructs a {@link RepresentationObject} with the given arguments.
   * 
   * @param pid
   * @param id
   * @param type
   * @param subType
   * @param statuses
   * @param descriptionObjectPID
   * @param rootFile
   * @param partFiles
   */
  public RepresentationObject(String pid, String id, String type, String subType, String[] statuses,
    String descriptionObjectPID, RepresentationFile rootFile, RepresentationFile[] partFiles) {
    this(pid, id, type, subType, null, null, null, statuses, descriptionObjectPID, rootFile, partFiles);
  }

  /**
   * Constructs a {@link RepresentationObject} with the given arguments.
   * 
   * @param pid
   * @param id
   * @param type
   * @param subType
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param rootFile
   * @param partFiles
   * @param statuses
   * @param descriptionObjectPID
   */
  public RepresentationObject(String pid, String id, String type, String subType, Date lastModifiedDate,
    Date createdDate, String state, String[] statuses, String descriptionObjectPID, RepresentationFile rootFile,
    RepresentationFile[] partFiles) {

    super(pid, id, type, subType, lastModifiedDate, createdDate, state, statuses, descriptionObjectPID);

    setRootFile(rootFile);
    setPartFiles(partFiles);
  }

  /**
   * Constructs a {@link RepresentationObject} with the given arguments.
   * 
   * @param pid
   * @param id
   * @param type
   * @param subType
   * @param rootFile
   * @param partFiles
   * @param statuses
   * @param descriptionObjectPID
   * @deprecated use
   *             {@link #RepresentationObject(String, String, String, String, String[], String, RepresentationFile, RepresentationFile[])}
   *             instead.
   */
  public RepresentationObject(String pid, String id, String type, String subType, RepresentationFile rootFile,
    RepresentationFile[] partFiles, String[] statuses, String descriptionObjectPID) {
    this(pid, id, type, subType, null, null, null, statuses, descriptionObjectPID, rootFile, partFiles);
  }

  /**
   * Constructs a new {@link RepresentationObject} with the given parameters.
   * 
   * @param rodaObject
   * @param rootFile
   * @param partFiles
   * @param status
   * @param descriptionObjectPID
   * @deprecated use
   *             {@link #RepresentationObject(RODAObject, String, String, RepresentationFile, RepresentationFile[])}
   *             instead.
   */
  public RepresentationObject(RODAObject rodaObject, RepresentationFile rootFile, RepresentationFile[] partFiles,
    String status, String descriptionObjectPID) {
    this(rodaObject, rootFile, partFiles, new String[] {status}, descriptionObjectPID);
  }

  /**
   * Constructs a {@link RepresentationObject} with the given arguments.
   * 
   * @param object
   * @param rootFile
   * @param partFiles
   * @param statuses
   * @param descriptionObjectPID
   * @deprecated use
   *             {@link #RepresentationObject(RODAObject, String[], String, RepresentationFile, RepresentationFile[])}
   *             instead.
   */
  public RepresentationObject(RODAObject object, RepresentationFile rootFile, RepresentationFile[] partFiles,
    String[] statuses, String descriptionObjectPID) {
    super(object, statuses, descriptionObjectPID);
    setRootFile(rootFile);
    setPartFiles(partFiles);
  }

  /**
   * @see RODAObject#toString()
   */
  public String toString() {

    int partFilesCount = (getPartFiles() != null) ? getPartFiles().length : 0;

    return "RepresentationObject( " + super.toString() + ", rootFile=" + getRootFile() + ", partFiles="
      + partFilesCount + " )";
  }

  /**
   * @see RODAObject#setContentModel(String)
   */
  public void setContentModel(String contentModel) {
    super.setContentModel(contentModel);

    String[] names = getContentModel().split(":");
    if (names.length < 3) {
      throw new IllegalArgumentException(contentModel + " is not a valid contentModel for a representation");
    } else {
      if (!names[1].equalsIgnoreCase("r")) {
        throw new WrongCModelException("contentModel should start with 'roda:r' (" + contentModel + ")");
      } else {
        // it's already set, by super.setCModel(contentModel)
        // check that the type is supported

      }
    }
  }

  /**
   * @return the rootFile
   */
  public RepresentationFile getRootFile() {
    return rootFile;
  }

  /**
   * @param rootFile
   *          the rootFile to set
   */
  public void setRootFile(RepresentationFile rootFile) {
    if (rootFile == null) {
      throw new NullPointerException("root file cannot be null");
    }
    this.rootFile = rootFile;
  }

  /**
   * @return the partFiles
   */
  public RepresentationFile[] getPartFiles() {
    return (RepresentationFile[]) partFiles.toArray(new RepresentationFile[partFiles.size()]);
  }

  /**
   * @param partFiles
   *          the partFiles to set
   */
  public void setPartFiles(RepresentationFile[] partFiles) {
    this.partFiles.clear();
    if (partFiles != null) {
      this.partFiles.addAll(Arrays.asList(partFiles));
    }
  }

  /**
   * @param partFile
   *          the partFile to add
   */
  public void addPartFile(RepresentationFile partFile) {
    if (partFile != null) {
      this.partFiles.add(partFile);
    }
  }

}
