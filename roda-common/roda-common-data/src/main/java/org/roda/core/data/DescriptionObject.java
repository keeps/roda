/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.data.common.InvalidDescriptionLevel;
import org.roda.core.data.eadc.Acqinfo;
import org.roda.core.data.eadc.Acqinfos;
import org.roda.core.data.eadc.ArrangementTable;
import org.roda.core.data.eadc.BioghistChronlist;
import org.roda.core.data.eadc.ControlAccesses;
import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Index;
import org.roda.core.data.eadc.LangmaterialLanguages;
import org.roda.core.data.eadc.Materialspec;
import org.roda.core.data.eadc.Materialspecs;
import org.roda.core.data.eadc.Note;
import org.roda.core.data.eadc.Notes;
import org.roda.core.data.eadc.P;
import org.roda.core.data.eadc.PhysdescElement;
import org.roda.core.data.eadc.PhysdescGenreform;
import org.roda.core.data.eadc.ProcessInfo;
import org.roda.core.data.eadc.Relatedmaterial;
import org.roda.core.data.eadc.Relatedmaterials;
import org.roda.core.data.v2.RODAObject;
import org.roda.core.data.v2.SimpleDescriptionObject;

/**
 * This is a Description Object (DO). It contains all the descriptive metadata
 * for an intellectual entity.
 * 
 * @author Rui Castro
 */
public class DescriptionObject extends SimpleDescriptionObject {
  private static final long serialVersionUID = -2188159657722817564L;

  public static enum ATTRIBUTE_LABEL {
    VERSION, KEYWORDS
  };

  public static enum ATTRIBUTE_ALTRENDER {
    ID, FULL_ID, ACCEPTANCE_DATE, REDACTION, REASON_FOR_REDACTION, REASON_FOR_OUTPUT, INPUT, OUTPUT, OTHER_BINDING_INFO
  };

  public static final String PHYSDESC = "physdesc";
  public static final String PHYSDESC_DIMENSIONS = "physdescDimensions";
  public static final String PHYSDESC_PHYSFACET = "physdescPhysfacet";
  public static final String PHYSDESC_DATE_INITIAL = "physdescDateInitial";
  public static final String PHYSDESC_DATE_FINAL = "physdescDateFinal";
  public static final String PHYSDESC_EXTENT = "physdescExtent";
  public static final String ORIGINATION = "origination";
  public static final String BIOGHIST = "bioghist";
  public static final String BIOGHIST_CHRONLIST = "bioghistChronlist";
  public static final String CUSTODHIST = "custodhist";
  public static final String ACQINFO = "acqinfo";
  @Deprecated
  public static final String ACQINFO_NUM = "acqinfoNum";
  @Deprecated
  public static final String ACQINFO_DATE = "acqinfoDate";
  public static final String SCOPECONTENT = "scopecontent";
  public static final String APPRAISAL = "appraisal";
  public static final String ACCRUALS = "accruals";
  public static final String ARRANGEMENT = "arrangement";
  public static final String ACCESSRESTRICT = "accessrestrict";
  public static final String USERESTRICT = "userestrict";
  public static final String LANGMATERIAL_LANGUAGES = "langmaterialLanguages";
  public static final String PHYSTECH = "phystech";
  public static final String MATERIALSPEC = "materialspec";
  public static final String OTHERFINDAID = "otherfindaid";
  public static final String RELATEDMATERIAL = "relatedmaterial";
  public static final String BIBLIOGRAPHY = "bibliography";
  public static final String NOTE = "note";
  public static final String PREFERCITE = "prefercite";
  public static final String ABSTRACT = "abstract";
  public static final String ODD = "odd";
  public static final String CONTROLACCESS = "controlaccess";
  public static final String PHYSDESC_GENREFORM = "physdescGenreform";
  public static final String PROCESSINFO = "processinfo";
  public static final String INDEX = "index";

  public static final String COMPLETE_REFERENCE = "completeReference";
  public static final String HANDLE_URL = "handleURL";

  private static final String[] ELEMENTS = new String[] {PHYSDESC, PHYSDESC_DIMENSIONS, PHYSDESC_PHYSFACET,
    PHYSDESC_DATE_INITIAL, PHYSDESC_DATE_FINAL, PHYSDESC_EXTENT, ORIGINATION, BIOGHIST, BIOGHIST_CHRONLIST, CUSTODHIST,
    ACQINFO, SCOPECONTENT, APPRAISAL, ACCRUALS, ARRANGEMENT, ACCESSRESTRICT, USERESTRICT, LANGMATERIAL_LANGUAGES,
    PHYSTECH, MATERIALSPEC, OTHERFINDAID, RELATEDMATERIAL, BIBLIOGRAPHY, NOTE, PREFERCITE, COMPLETE_REFERENCE,
    HANDLE_URL, ABSTRACT, ODD, CONTROLACCESS, PHYSDESC_GENREFORM, PROCESSINFO, INDEX};
  public static final String ID = null;
  public static final String TITLE = null;
  public static final String LEVEL = null;
  public static final String DATE_INITIAL = null;
  public static final String DATE_FINAL = null;
  public static final String COUNTRYCODE = null;
  public static final String REPOSITORYCODE = null;

  /**
   * @return all elements
   */
  public static String[] getAllElements() {
    List<String> list = new ArrayList<String>();
    // FIXME
    // list.addAll(Arrays.asList(SimpleDescriptionObject.getAllElements()));
    list.addAll(Arrays.asList(ELEMENTS));
    return (String[]) list.toArray(new String[list.size()]);
  }

  private String metsID = null;

  private String physdesc = null;

  private PhysdescElement physdescDimensions = null;

  private PhysdescElement physdescPhysfacet = null;

  private String physdescDateInitial = null;

  private String physdescDateFinal = null;

  private PhysdescElement physdescExtent = null;

  // new in RODA 1.2
  private PhysdescGenreform physdescGenreform = null;

  private String origination = null;

  private String bioghist = null;

  private BioghistChronlist bioghistChronlist = null;

  private String custodhist = null;

  // changed in RODA 1.2 (string to Acqinfos object)
  private Acqinfos acqinfos = null;

  private String scopecontent = null;

  private String appraisal = null;

  private String accruals = null;

  private String arrangement = null;

  private ArrangementTable arrangementTable = null;

  private String accessrestrict = null;

  private String userestrict = null;

  private LangmaterialLanguages langmaterialLanguages = null;

  private String phystech = null;

  // changed in RODA 1.2 (string to Materialspecs object)
  private Materialspecs materialspecs = null;

  private String otherfindaid = null;

  // changed in RODA 1.2 (string to Relatedmaterials object)
  private Relatedmaterials relatedmaterials = null;

  private String bibliography = null;

  // changed in RODA 1.2 (string to Notes object)
  private Notes notes = null;

  private String prefercite = null;

  private String completeReference = null;

  private String handleURL = null;
  // new in RODA 1.2
  private ControlAccesses controlaccesses = null;
  // new in RODA 1.2
  private String odd = null;
  // new in RODA 1.2
  private String abstractt = null;
  // new in RODA 1.2
  private ProcessInfo processinfo = null;
  // new in RODA 1.2
  private Index index = null;

  /**
   * Constructs a new empty Descriptive Object.
   */
  public DescriptionObject() {
  }

  /**
   * @param pid
   * @param label
   * @param contentModel
   * @param lastModifiedDate
   * @param createdDate
   * @param state
   * @param completeReference
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
   * 
   * @throws InvalidDescriptionLevel
   */
  public DescriptionObject(String pid, String label, String contentModel, Date lastModifiedDate, Date createdDate,
    String state, String completeReference, DescriptionLevel level, String countryCode, String repositoryCode,
    String id, String title, String dateInitial, String dateFinal, String description, String parentPID,
    int subElementsCount) throws InvalidDescriptionLevel {
    // FIXME
    // super(pid, label, contentModel, lastModifiedDate, createdDate, state,
    // level, countryCode, repositoryCode, id, title,
    // dateInitial, dateFinal, description, parentPID, subElementsCount);
  }

  /**
   * Constructs a new SimpleDescriptionObject with the given arguments.
   * 
   * @param object
   * @param completeReference
   * @param level
   * @param countryCode
   * @param repositoryCode
   * @param id
   * @param unitTitle
   * @param dateInitial
   * @param dateFinal
   * @param description
   * @param parentPID
   * @param subElementsCount
   * 
   * @throws InvalidDescriptionLevel
   */
  public DescriptionObject(RODAObject object, String completeReference, DescriptionLevel level, String countryCode,
    String repositoryCode, String id, String unitTitle, String dateInitial, String dateFinal, String description,
    String parentPID, int subElementsCount) throws InvalidDescriptionLevel {
    // FIXME
    // this(object.getPid(), object.getLabel(), object.getContentModel(),
    // object.getLastModifiedDate(),
    // object.getCreatedDate(), object.getState(), completeReference, level,
    // countryCode, repositoryCode, id, unitTitle,
    // dateInitial, dateFinal, description, parentPID, subElementsCount);
  }

  /**
   * Constructs a new Descriptive Object from the given SimpleDescriptionObject.
   * 
   * @param object
   *          a SimpleDescriptionObject.
   */
  public DescriptionObject(SimpleDescriptionObject object) {
    // FIXME
    // this(object.getPid(), object.getLabel(), object.getContentModel(),
    // object.getLastModifiedDate(),
    // object.getCreatedDate(), object.getState(), null, object.getLevel(),
    // object.getCountryCode(),
    // object.getRepositoryCode(), object.getId(), object.getTitle(),
    // object.getDateInitial(), object.getDateFinal(),
    // object.getDescription(), object.getParentPID(),
    // object.getSubElementsCount());
  }

  /**
   * Constructs a new Descriptive Object cloning an existing Description Object.
   * 
   * @param object
   *          a Description Object.
   */
  public DescriptionObject(DescriptionObject object) {
    // FIXME
    // this(object.getPid(), object.getLabel(), object.getContentModel(),
    // object.getLastModifiedDate(),
    // object.getCreatedDate(), object.getState(),
    // object.getCompleteReference(), object.getLevel(),
    // object.getCountryCode(), object.getRepositoryCode(), object.getId(),
    // object.getTitle(), object.getDateInitial(),
    // object.getDateFinal(), object.getDescription(), object.getParentPID(),
    // object.getSubElementsCount());

    // setPid(pid);
    // setLabel(label);
    // setCModel(contentModel);

    // setLevel(level);
    // setCountryCode(object.getCountryCode());
    // setRepositoryCode(object.getRepositoryCode());
    // setId(id);
    // setTitle(title);
    // setDateInitial(dateInitial);
    // setDateFinal(dateFinal);
    // setSubElementsCount(count);
    // setDescription(description);

    setPhysdesc(object.getPhysdesc());
    setPhysdescDateInitial(object.getPhysdescDateInitial());
    setPhysdescDateFinal(object.getPhysdescDateFinal());
    setPhysdescDimensions(object.getPhysdescDimensions());
    setPhysdescExtent(object.getPhysdescExtent());
    setPhysdescPhysfacet(object.getPhysdescPhysfacet());
    setOrigination(object.getOrigination());
    setBioghist(object.getBioghist());
    setBioghistChronlist(getBioghistChronlist());
    setAcqinfos(object.getAcqinfos());
    setScopecontent(object.getScopecontent());
    setAppraisal(object.getAppraisal());
    setAccruals(object.getAccruals());
    setArrangement(object.getArrangement());
    setArrangementTable(object.getArrangementTable());
    setAccessrestrict(object.getAccessrestrict());
    setUserestrict(object.getUserestrict());
    setLangmaterialLanguages(object.getLangmaterialLanguages());
    setPhystech(object.getPhystech());
    setMaterialspecs(object.getMaterialspecs());
    setOtherfindaid(object.getOtherfindaid());
    setRelatedmaterials(object.getRelatedmaterials());
    setBibliography(object.getBibliography());
    setPrefercite(object.getPrefercite());
    setAbstract(object.getAbstract());
    setOdd(object.getOdd());
    setControlaccesses(object.getControlaccesses());
    setPhysdescGenreform(object.getPhysdescGenreform());
    setProcessinfo(object.getProcessinfo());
    setIndex(object.getIndex());

    // FIXME ensure that all fields are being copied (note looks like one of the
    // fields that isn't being copied)

    setCompleteReference(object.getCompleteReference());
    setHandleURL(object.getHandleURL());

    setMetsID(object.getMetsID());
  }

  /**
   * @see SimpleDescriptionObject#toString()
   */
  public String toString() {
    return "DescriptionObject ( " + super.toString() + ", completeReference=" + getCompleteReference() + ", handleURL="
      + getHandleURL() + " )";
  }

  // FIXME
  // /**
  // * @see SimpleDescriptionObject#getValue(String)
  // */
  // public EadCValue getValue(String element) throws IllegalArgumentException {
  //
  // EadCValue value = null;
  //
  // try {
  //
  // value = super.getValue(element);
  //
  // } catch (IllegalArgumentException e) {
  //
  // if (PHYSDESC.equals(element)) {
  // value = getTextOrNull(getPhysdesc());
  // } else if (PHYSDESC_DIMENSIONS.equals(element)) {
  // value = getPhysdescDimensions();
  // } else if (PHYSDESC_PHYSFACET.equals(element)) {
  // value = getPhysdescPhysfacet();
  // } else if (PHYSDESC_DATE_INITIAL.equals(element)) {
  // value = getTextOrNull(getPhysdescDateInitial());
  // } else if (PHYSDESC_DATE_FINAL.equals(element)) {
  // value = getTextOrNull(getPhysdescDateFinal());
  // } else if (PHYSDESC_EXTENT.equals(element)) {
  // value = getPhysdescExtent();
  // } else if (ORIGINATION.equals(element)) {
  // value = getTextOrNull(getOrigination());
  // } else if (BIOGHIST.equals(element)) {
  // value = getTextOrNull(getBioghist());
  // } else if (BIOGHIST_CHRONLIST.equals(element)) {
  // value = getBioghistChronlist();
  // } else if (CUSTODHIST.equals(element)) {
  // value = getTextOrNull(getCustodhist());
  // } else if (ACQINFO.equals(element)) {
  // value = getAcqinfos();
  // } else if (SCOPECONTENT.equals(element)) {
  // value = getTextOrNull(getScopecontent());
  // } else if (APPRAISAL.equals(element)) {
  // value = getTextOrNull(getAppraisal());
  // } else if (ACCRUALS.equals(element)) {
  // value = getTextOrNull(getAccruals());
  // } else if (ARRANGEMENT.equals(element)) {
  // if (getArrangement() != null) {
  // value = new Text(getArrangement());
  // } else if (getArrangementTable() != null) {
  // value = getArrangementTable();
  // } else {
  // value = null;
  // }
  // // } else if (ARRANGEMENT_TABLE.equals(element)) {
  // } else if (ACCESSRESTRICT.equals(element)) {
  // value = getTextOrNull(getAccessrestrict());
  // } else if (USERESTRICT.equals(element)) {
  // value = getTextOrNull(getUserestrict());
  // } else if (LANGMATERIAL_LANGUAGES.equals(element)) {
  // value = getLangmaterialLanguages();
  // } else if (PHYSTECH.equals(element)) {
  // value = getTextOrNull(getPhystech());
  // } else if (MATERIALSPEC.equals(element)) {
  // value = getMaterialspecs();
  // } else if (OTHERFINDAID.equals(element)) {
  // value = getTextOrNull(getOtherfindaid());
  // } else if (RELATEDMATERIAL.equals(element)) {
  // value = getRelatedmaterials();
  // } else if (BIBLIOGRAPHY.equals(element)) {
  // value = getTextOrNull(getBibliography());
  // } else if (NOTE.equals(element)) {
  // value = getNotes();
  // } else if (PREFERCITE.equals(element)) {
  // value = getTextOrNull(getPrefercite());
  // } else if (COMPLETE_REFERENCE.equals(element)) {
  // value = getTextOrNull(getCompleteReference());
  // } else if (HANDLE_URL.equals(element)) {
  // value = getTextOrNull(getHandleURL());
  // } else if (ABSTRACT.equals(element)) {
  // value = getTextOrNull(getAbstract());
  // } else if (ODD.equals(element)) {
  // value = getTextOrNull(getOdd());
  // } else if (CONTROLACCESS.equals(element)) {
  // value = getControlaccesses();
  // } else if (PHYSDESC_GENREFORM.equals(element)) {
  // value = getPhysdescGenreform();
  // } else if (PROCESSINFO.equals(element)) {
  // value = getProcessinfo();
  // } else if (INDEX.equals(element)) {
  // value = getIndex();
  // } else {
  //
  // // no value named 'element'
  // throw new IllegalArgumentException("Unknown element named " + element);
  // }
  //
  // }
  //
  // return value;
  // }

  // public String[] getAssignedElements() {
  // return null;
  // }

  /**
   * @see SimpleDescriptionObject#setValue(String, EadCValue)
   */
  public void setValue(String element, EadCValue value) throws IllegalArgumentException {

    // FIXME
    // try {
    //
    // super.setValue(element, value);
    //
    // } catch (IllegalArgumentException e) {
    // // SimpleDescriptionObject doesn't have this element
    //
    // // if (COMPLETE_REFERENCE.equals(element)) {
    // // setCompleteReference(checkEadCText(element, value));
    // // } else
    // // if (HANDLE_URL.equals(element)) {
    // // setHandleURL(checkEadCText(element, value));
    // // } else
    //
    // if (PHYSDESC.equals(element)) {
    // setPhysdesc(checkEadCText(element, value));
    // } else if (PHYSDESC_DIMENSIONS.equals(element)) {
    // setPhysdescDimensions(checkEadCPhysdescElement(element, value));
    // } else if (PHYSDESC_PHYSFACET.equals(element)) {
    // setPhysdescPhysfacet(checkEadCPhysdescElement(element, value));
    // } else if (PHYSDESC_DATE_INITIAL.equals(element)) {
    // setDateInitial(checkEadCText(element, value));
    // } else if (PHYSDESC_DATE_FINAL.equals(element)) {
    // setDateFinal(checkEadCText(element, value));
    // } else if (PHYSDESC_EXTENT.equals(element)) {
    // setPhysdescExtent(checkEadCPhysdescElement(element, value));
    // } else if (ORIGINATION.equals(element)) {
    // setOrigination(checkEadCText(element, value));
    // } else if (BIOGHIST.equals(element)) {
    // setBioghist(checkEadCText(element, value));
    // } else if (BIOGHIST_CHRONLIST.equals(element)) {
    // setBioghistChronlist(checkEadCBioghistChronlist(element, value));
    // } else if (CUSTODHIST.equals(element)) {
    // setCustodhist(checkEadCText(element, value));
    // } else if (ACQINFO.equals(element)) {
    // setAcqinfos(checkEadCAcqinfos(element, value));
    // } else if (SCOPECONTENT.equals(element)) {
    // setScopecontent(checkEadCText(element, value));
    // } else if (APPRAISAL.equals(element)) {
    // setAppraisal(checkEadCText(element, value));
    // } else if (ACCRUALS.equals(element)) {
    // setAccruals(checkEadCText(element, value));
    // } else if (ARRANGEMENT.equals(element)) {
    // if (value instanceof Text) {
    // setArrangement(value.toString());
    // } else if (value instanceof ArrangementTable) {
    // setArrangementTable((ArrangementTable) value);
    // } else {
    // throw new IllegalArgumentException("illegal value for element " + element
    // + ". Value of type " + Text.class
    // + " or " + ArrangementTable.class + " was expected.");
    // }
    // // } else if (ARRANGEMENT_TABLE.equals(element)) {
    // } else if (ACCESSRESTRICT.equals(element)) {
    // setAccessrestrict(checkEadCText(element, value));
    // } else if (USERESTRICT.equals(element)) {
    // setUserestrict(checkEadCText(element, value));
    // } else if (LANGMATERIAL_LANGUAGES.equals(element)) {
    // setLangmaterialLanguages(checkEadCLangmaterialLanguages(element, value));
    // } else if (PHYSTECH.equals(element)) {
    // setPhystech(checkEadCText(element, value));
    // } else if (MATERIALSPEC.equals(element)) {
    // setMaterialspecs(checkEadCMaterialspecs(element, value));
    // } else if (OTHERFINDAID.equals(element)) {
    // setOtherfindaid(checkEadCText(element, value));
    // } else if (RELATEDMATERIAL.equals(element)) {
    // setRelatedmaterials(checkEadCRelatedmaterials(element, value));
    // } else if (BIBLIOGRAPHY.equals(element)) {
    // setBibliography(checkEadCText(element, value));
    // } else if (NOTE.equals(element)) {
    // setNotes(checkEadCNotes(element, value));
    // } else if (PREFERCITE.equals(element)) {
    // setPrefercite(checkEadCText(element, value));
    // } else if (ABSTRACT.equals(element)) {
    // setAbstract(checkEadCText(element, value));
    // } else if (ODD.equals(element)) {
    // setOdd(checkEadCText(element, value));
    // } else if (CONTROLACCESS.equals(element)) {
    // setControlaccesses(checkEadCControlaccessesElement(element, value));
    // } else if (PHYSDESC_GENREFORM.equals(element)) {
    // setPhysdescGenreform(checkEadCPhysdescGenreform(element, value));
    // } else if (PROCESSINFO.equals(element)) {
    // setProcessinfo(checkEadCProcessinfo(element, value));
    // } else if (INDEX.equals(element)) {
    // setIndex(checkEadCIndex(element, value));
    // } else {
    // // no value named 'element'
    // throw new IllegalArgumentException("Unknown element named " + element);
    // }
    //
    // }
  }

  private Index checkEadCIndex(String element, EadCValue value) {
    if (value == null) {
      return null;
    } else if (value instanceof Index) {
      return (Index) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + Index.class + " or null was expected.");
    }
  }

  private Acqinfos checkEadCAcqinfos(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof Acqinfos) {
      return (Acqinfos) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + Acqinfos.class + " or null was expected.");
    }
  }

  private Materialspecs checkEadCMaterialspecs(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof Materialspecs) {
      return (Materialspecs) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + Materialspecs.class + " or null was expected.");
    }
  }

  private Notes checkEadCNotes(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof Notes) {
      return (Notes) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + Notes.class + " or null was expected.");
    }
  }

  private Relatedmaterials checkEadCRelatedmaterials(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof Relatedmaterials) {
      return (Relatedmaterials) value;
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type "
        + Relatedmaterials.class + " or null was expected.");
    }
  }

  private ProcessInfo checkEadCProcessinfo(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof ProcessInfo) {
      return (ProcessInfo) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + ProcessInfo.class + " or null was expected.");
    }
  }

  private LangmaterialLanguages checkEadCLangmaterialLanguages(String element, EadCValue value)
    throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof LangmaterialLanguages) {
      return (LangmaterialLanguages) value;
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type "
        + LangmaterialLanguages.class + " or null was expected.");
    }
  }

  protected BioghistChronlist checkEadCBioghistChronlist(String element, EadCValue value)
    throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof BioghistChronlist) {
      return (BioghistChronlist) value;
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type "
        + BioghistChronlist.class + " or null was expected.");
    }
  }

  protected PhysdescElement checkEadCPhysdescElement(String element, EadCValue value) throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof PhysdescElement) {
      return (PhysdescElement) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + PhysdescElement.class + " or null was expected.");
    }
  }

  protected PhysdescGenreform checkEadCPhysdescGenreform(String element, EadCValue value)
    throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof PhysdescGenreform) {
      return (PhysdescGenreform) value;
    } else {
      throw new IllegalArgumentException("illegal value for element " + element + ". Value of type "
        + PhysdescGenreform.class + " or null was expected.");
    }
  }

  protected Materialspecs checkEadCMaterialSpecElement(String element, EadCValue value)
    throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof Materialspecs) {
      return (Materialspecs) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + Materialspecs.class + " or null was expected.");
    }
  }

  protected ControlAccesses checkEadCControlaccessesElement(String element, EadCValue value)
    throws IllegalArgumentException {
    if (value == null) {
      return null;
    } else if (value instanceof ControlAccesses) {
      return (ControlAccesses) value;
    } else {
      throw new IllegalArgumentException(
        "illegal value for element " + element + ". Value of type " + ControlAccesses.class + " or null was expected.");
    }
  }

  /**
   * @return the accessrestrict
   */
  public String getAccessrestrict() {
    return accessrestrict;
  }

  /**
   * @param accessrestrict
   *          the accessrestrict to set
   */
  public void setAccessrestrict(String accessrestrict) {
    this.accessrestrict = accessrestrict;
  }

  /**
   * @return the accruals
   */
  public String getAccruals() {
    return accruals;
  }

  /**
   * @param accruals
   *          the accruals to set
   */
  public void setAccruals(String accruals) {
    this.accruals = accruals;
  }

  /**
   * @return the appraisal
   */
  public String getAppraisal() {
    return appraisal;
  }

  /**
   * @param appraisal
   *          the appraisal to set
   */
  public void setAppraisal(String appraisal) {
    this.appraisal = appraisal;
  }

  /**
   * @return the arrangement
   */
  public String getArrangement() {
    return arrangement;
  }

  /**
   * @param arrangement
   *          the arrangement to set
   */
  public void setArrangement(String arrangement) {
    this.arrangement = arrangement;
  }

  /**
   * @return the arrangementTable
   */
  public ArrangementTable getArrangementTable() {
    return arrangementTable;
  }

  /**
   * @param arrangementTable
   *          the arrangementTable to set
   */
  public void setArrangementTable(ArrangementTable arrangementTable) {
    this.arrangementTable = arrangementTable;
  }

  /**
   * @return the bibliography
   */
  public String getBibliography() {
    return bibliography;
  }

  /**
   * @param bibliography
   *          the bibliography to set
   */
  public void setBibliography(String bibliography) {
    this.bibliography = bibliography;
  }

  /**
   * @return the bioghist
   */
  public String getBioghist() {
    return bioghist;
  }

  /**
   * @param bioghist
   *          the bioghist to set
   */
  public void setBioghist(String bioghist) {
    this.bioghist = bioghist;
  }

  /**
   * @return the bioghistChronlist
   */
  public BioghistChronlist getBioghistChronlist() {
    return bioghistChronlist;
  }

  /**
   * @param bioghistChronlist
   *          the bioghistChronlist to set
   */
  public void setBioghistChronlist(BioghistChronlist bioghistChronlist) {
    this.bioghistChronlist = bioghistChronlist;
  }

  /**
   * @return the langmaterialLanguages
   */
  public LangmaterialLanguages getLangmaterialLanguages() {
    return this.langmaterialLanguages;
  }

  /**
   * @param langmaterialLanguages
   *          the langmaterialLanguages to set
   */
  public void setLangmaterialLanguages(LangmaterialLanguages langmaterialLanguages) {
    this.langmaterialLanguages = langmaterialLanguages;
  }

  /**
   * @return the origination
   */
  public String getOrigination() {
    return origination;
  }

  /**
   * @param origination
   *          the origination to set
   */
  public void setOrigination(String origination) {
    this.origination = origination;
  }

  /**
   * @return the otherfindaid
   */
  public String getOtherfindaid() {
    return otherfindaid;
  }

  /**
   * @param otherfindaid
   *          the otherfindaid to set
   */
  public void setOtherfindaid(String otherfindaid) {
    this.otherfindaid = otherfindaid;
  }

  /**
   * @return the physdesc
   */
  public String getPhysdesc() {
    return physdesc;
  }

  /**
   * @param physdesc
   *          the physdesc to set
   */
  public void setPhysdesc(String physdesc) {
    this.physdesc = physdesc;
  }

  /**
   * @return the physdescDateFinal
   */
  public String getPhysdescDateFinal() {
    return physdescDateFinal;
  }

  /**
   * @param physdescDateFinal
   *          the physdescDateFinal to set
   */
  public void setPhysdescDateFinal(String physdescDateFinal) {
    this.physdescDateFinal = physdescDateFinal;
  }

  /**
   * @return the physdescDateInitial
   */
  public String getPhysdescDateInitial() {
    return physdescDateInitial;
  }

  /**
   * @param physdescDateInitial
   *          the physdescDateInitial to set
   */
  public void setPhysdescDateInitial(String physdescDateInitial) {
    this.physdescDateInitial = physdescDateInitial;
  }

  /**
   * @return the physdescDimensions
   */
  public PhysdescElement getPhysdescDimensions() {
    return physdescDimensions;
  }

  /**
   * @param physdescDimensions
   *          the physdescDimensions to set
   */
  public void setPhysdescDimensions(PhysdescElement physdescDimensions) {
    this.physdescDimensions = physdescDimensions;
  }

  /**
   * @return the physdescExtent
   */
  public PhysdescElement getPhysdescExtent() {
    return physdescExtent;
  }

  /**
   * @param physdescExtent
   *          the physdescExtent to set
   */
  public void setPhysdescExtent(PhysdescElement physdescExtent) {
    this.physdescExtent = physdescExtent;
  }

  /**
   * @return the physdescPhysfacet
   */
  public PhysdescElement getPhysdescPhysfacet() {
    return physdescPhysfacet;
  }

  /**
   * @param physdescPhysfacet
   *          the physdescPhysfacet to set
   */
  public void setPhysdescPhysfacet(PhysdescElement physdescPhysfacet) {
    this.physdescPhysfacet = physdescPhysfacet;
  }

  /**
   * @return the phystech
   */
  public String getPhystech() {
    return phystech;
  }

  /**
   * @param phystech
   *          the phystech to set
   */
  public void setPhystech(String phystech) {
    this.phystech = phystech;
  }

  /**
   * @return the prefercite
   */
  public String getPrefercite() {
    return prefercite;
  }

  /**
   * @param prefercite
   *          the prefercite to set
   */
  public void setPrefercite(String prefercite) {
    this.prefercite = prefercite;
  }

  /**
   * @return the userestrict
   */
  public String getUserestrict() {
    return userestrict;
  }

  /**
   * @param userestrict
   *          the userestrict to set
   */
  public void setUserestrict(String userestrict) {
    this.userestrict = userestrict;
  }

  /**
   * @return the scopecontent
   */
  public String getScopecontent() {
    return scopecontent;
  }

  /**
   * @param scopecontent
   *          the scopecontent to set
   */
  public void setScopecontent(String scopecontent) {
    if (scopecontent == null) {
      throw new NullPointerException("scopecontent cannot be null");
    } else {
      this.scopecontent = scopecontent;
    }
  }

  /**
   * @return the custodhist
   */
  public String getCustodhist() {
    return custodhist;
  }

  /**
   * @param custodhist
   *          the custodhist to set
   */
  public void setCustodhist(String custodhist) {
    this.custodhist = custodhist;
  }

  /**
   * @return the completeReference
   */
  public String getCompleteReference() {
    return completeReference;
  }

  /**
   * @param completeReference
   *          the completeReference to set
   */
  public void setCompleteReference(String completeReference) {
    this.completeReference = completeReference;
  }

  /**
   * @return the handleURL
   */
  public String getHandleURL() {
    return handleURL;
  }

  /**
   * @param handleURL
   *          the handleURL to set
   */
  public void setHandleURL(String handleURL) {
    this.handleURL = handleURL;
  }

  public String getAbstract() {
    return abstractt;
  }

  public void setAbstract(String abstractt) {
    this.abstractt = abstractt;
  }

  public String getOdd() {
    return odd;
  }

  public void setOdd(String odd) {
    this.odd = odd;
  }

  public ControlAccesses getControlaccesses() {
    return controlaccesses;
  }

  public void setControlaccesses(ControlAccesses controlaccesses) {
    this.controlaccesses = controlaccesses;
  }

  public PhysdescGenreform getPhysdescGenreform() {
    return physdescGenreform;
  }

  public void setPhysdescGenreform(PhysdescGenreform physdescGenreform) {
    this.physdescGenreform = physdescGenreform;
  }

  public ProcessInfo getProcessinfo() {
    return processinfo;
  }

  public void setProcessinfo(ProcessInfo processinfo) {
    this.processinfo = processinfo;
  }

  public Relatedmaterials getRelatedmaterials() {
    return relatedmaterials;
  }

  public void setRelatedmaterials(Relatedmaterials relatedmaterials) {
    this.relatedmaterials = relatedmaterials;
  }

  public Notes getNotes() {
    return notes;
  }

  public void setNotes(Notes notes) {
    this.notes = notes;
  }

  public Materialspecs getMaterialspecs() {
    return materialspecs;
  }

  public void setMaterialspecs(Materialspecs materialspecs) {
    this.materialspecs = materialspecs;
  }

  /**
   * @deprecated from now on, several materialspec may exist and therefore this
   *             method doesn't make sense
   */
  @Deprecated
  public void setMaterialspec(String materialspec) {
    if (materialspecs == null || materialspecs.getMaterialspecs() == null) {
      materialspecs = new Materialspecs(new Materialspec[] {new Materialspec(materialspec)});
    } else {
      Materialspec[] oldMaterialspecs = materialspecs.getMaterialspecs();
      Materialspec[] newMaterialspecs = new Materialspec[oldMaterialspecs.length + 1];
      for (int i = 0; i < oldMaterialspecs.length; i++) {
        newMaterialspecs[i] = oldMaterialspecs[i];
      }
      newMaterialspecs[oldMaterialspecs.length] = new Materialspec(materialspec);

      materialspecs.setMaterialspecs(newMaterialspecs);
    }
  }

  /**
   * @deprecated from now on, several relatedmaterial may exist and therefore
   *             this method doesn't make sense
   */
  @Deprecated
  public void setRelatedmaterial(String relatedmaterial) {
    if (relatedmaterials == null || relatedmaterials.getRelatedmaterials() == null) {
      relatedmaterials = new Relatedmaterials(new Relatedmaterial[] {new Relatedmaterial(new P(relatedmaterial))});
    } else {
      Relatedmaterial[] oldRelatedmaterials = relatedmaterials.getRelatedmaterials();
      Relatedmaterial[] newRelatedmaterials = new Relatedmaterial[oldRelatedmaterials.length + 1];
      for (int i = 0; i < oldRelatedmaterials.length; i++) {
        newRelatedmaterials[i] = oldRelatedmaterials[i];
      }
      newRelatedmaterials[oldRelatedmaterials.length] = new Relatedmaterial(new P(relatedmaterial));

      relatedmaterials.setRelatedmaterials(newRelatedmaterials);
    }
  }

  /**
   * @deprecated from now on, several note may exist and therefore this method
   *             doesn't make sense
   */
  @Deprecated
  public void setNote(String note) {
    addNote(note);
  }

  public void addNote(String note) {
    if (notes == null || notes.getNotes() == null) {
      notes = new Notes(new Note[] {new Note(new P(note))});
    } else {
      Note[] oldNotes = notes.getNotes();
      Note[] newNotes = new Note[oldNotes.length + 1];
      for (int i = 0; i < oldNotes.length; i++) {
        newNotes[i] = oldNotes[i];
      }
      newNotes[oldNotes.length] = new Note(new P(note));

      notes.setNotes(newNotes);
    }
  }

  /**
   * @deprecated from now on, several note may exist and therefore this method
   *             doesn't make sense
   */
  @Deprecated
  public String getNote() {
    if (notes != null && notes.getNotes() != null && notes.getNotes()[0] != null
      && notes.getNotes()[0].getP() != null) {
      return notes.getNotes()[0].getP().getText();
    }
    return null;
  }

  public Acqinfos getAcqinfos() {
    return acqinfos;
  }

  public void setAcqinfos(Acqinfos acqinfos) {
    this.acqinfos = acqinfos;
  }

  /**
   * @deprecated from now on, several acqinfo may exist and therefore this
   *             method doesn't make sense
   */
  @Deprecated
  public void setAcqinfo(String acqinfo) {
    if (acqinfos == null || acqinfos.getAcqinfos() == null) {
      acqinfos = new Acqinfos(new Acqinfo[] {new Acqinfo(acqinfo, null)});
    } else {
      // Acqinfo[] oldAcqinfos = acqinfos.getAcqinfos();
      // Acqinfo[] newAcqinfos = new Acqinfo[oldAcqinfos.length + 1];
      //
      // for (int i = 0; i < oldAcqinfos.length; i++) {
      // newAcqinfos[i] = oldAcqinfos[i];
      // }
      // newAcqinfos[oldAcqinfos.length] = new Acqinfo(acqinfo, null);
      //
      // acqinfos.setAcqinfos(newAcqinfos);
      Acqinfo firstAcqinfo = acqinfos.getAcqinfos()[0];
      firstAcqinfo.getP().setText(acqinfo);
    }

  }

  /**
   * @deprecated from now on, several acqinfo may exist and therefore this
   *             method doesn't make sense
   */
  @Deprecated
  public void setAcqinfoDate(String acqinfoDate) {
    if (acqinfos == null || acqinfos.getAcqinfos() == null) {
      acqinfos = new Acqinfos(
        new Acqinfo[] {new Acqinfo(new P(null, null, acqinfoDate, null, null, null, null), null)});
    } else {
      Acqinfo firstAcqinfo = acqinfos.getAcqinfos()[0];
      firstAcqinfo.getP().setDate(acqinfoDate);
    }
  }

  /**
   * @deprecated from now on, several acqinfo may exist and therefore this
   *             method doesn't make sense
   */
  @Deprecated
  public void setAcqinfoNum(String acqinfoNum) {
    if (acqinfos == null || acqinfos.getAcqinfos() == null) {
      acqinfos = new Acqinfos(new Acqinfo[] {new Acqinfo(new P(null, null, null, acqinfoNum, null, null, null), null)});
    } else {
      Acqinfo firstAcqinfo = acqinfos.getAcqinfos()[0];
      firstAcqinfo.getP().setNum(acqinfoNum);
    }
  }

  /**
   * Adds a new Acqinfo to the Acqinfo array
   * 
   * @param acqinfo
   *          new acqinfo to add
   */
  public void addAcqinfo(Acqinfo acqinfo) {
    if (acqinfos == null || acqinfos.getAcqinfos() == null) {
      acqinfos = new Acqinfos(new Acqinfo[] {acqinfo});
    } else {
      Acqinfo[] oldAcqinfos = acqinfos.getAcqinfos();
      Acqinfo[] newAcqinfos = new Acqinfo[oldAcqinfos.length + 1];

      for (int i = 0; i < oldAcqinfos.length; i++) {
        newAcqinfos[i] = oldAcqinfos[i];
      }
      newAcqinfos[oldAcqinfos.length] = acqinfo;

      acqinfos.setAcqinfos(newAcqinfos);
    }
  }

  public boolean hasAcqinfo(String acqinfoNum, String acqinfoCorpname) {
    boolean res = false;

    if (acqinfos != null && acqinfos.getAcqinfos() != null) {
      for (Acqinfo acqinfo : acqinfos.getAcqinfos()) {
        if (acqinfo.getP() != null && acqinfo.getP().getNum() != null && acqinfo.getP().getNum().equals(acqinfoNum)
          && acqinfo.getP().getCorpname() != null && acqinfo.getP().getCorpname().equals(acqinfoCorpname)) {
          res = true;
          break;
        }
      }
    }

    return res;
  }

  public Index getIndex() {
    return index;
  }

  public void setIndex(Index index) {
    this.index = index;
  }

  /** WARNING: only used for special handling of mets/metadata information */
  public String getMetsID() {
    return metsID;
  }

  /** WARNING: only used for special handling of mets/metadata information */
  public void setMetsID(String metsID) {
    this.metsID = metsID;
  }
}
