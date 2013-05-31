package pt.gov.dgarq.roda.core.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.gov.dgarq.roda.core.common.InvalidDescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTable;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;
import pt.gov.dgarq.roda.core.data.eadc.Text;

/**
 * This is a Description Object (DO). It contains all the descriptive metadata
 * for an intelectual entity.
 * 
 * @author Rui Castro
 */
public class DescriptionObject extends SimpleDescriptionObject {
	private static final long serialVersionUID = -2188159657722817564L;

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
	public static final String ACQINFO_NUM = "acqinfoNum";
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

	public static final String COMPLETE_REFERENCE = "completeReference";
	public static final String HANDLE_URL = "handleURL";

	private static final String[] ELEMENTS = new String[] { PHYSDESC,
			PHYSDESC_DIMENSIONS, PHYSDESC_PHYSFACET, PHYSDESC_DATE_INITIAL,
			PHYSDESC_DATE_FINAL, PHYSDESC_EXTENT, ORIGINATION, BIOGHIST,
			BIOGHIST_CHRONLIST, CUSTODHIST, ACQINFO, ACQINFO_NUM, ACQINFO_DATE,
			SCOPECONTENT, APPRAISAL, ACCRUALS, ARRANGEMENT, ACCESSRESTRICT,
			USERESTRICT, LANGMATERIAL_LANGUAGES, PHYSTECH, MATERIALSPEC,
			OTHERFINDAID, RELATEDMATERIAL, BIBLIOGRAPHY, NOTE, PREFERCITE,
			COMPLETE_REFERENCE, HANDLE_URL };

	/**
	 * @return all elements
	 */
	public static String[] getAllElements() {
		List<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(SimpleDescriptionObject.getAllElements()));
		list.addAll(Arrays.asList(ELEMENTS));
		return (String[]) list.toArray(new String[list.size()]);
	}

	private String physdesc = null;

	private PhysdescElement physdescDimensions = null;

	private PhysdescElement physdescPhysfacet = null;

	private String physdescDateInitial = null;

	private String physdescDateFinal = null;

	private PhysdescElement physdescExtent = null;

	private String origination = null;

	private String bioghist = null;

	private BioghistChronlist bioghistChronlist = null;

	private String custodhist = null;

	private String acqinfo = null;
	private String acqinfoNum = null;
	private String acqinfoDate = null;

	private String scopecontent = null;

	private String appraisal = null;

	private String accruals = null;

	private String arrangement = null;

	private ArrangementTable arrangementTable = null;

	private String accessrestrict = null;

	private String userestrict = null;

	private LangmaterialLanguages langmaterialLanguages = null;

	private String phystech = null;

	private String materialspec = null;

	private String otherfindaid = null;

	private String relatedmaterial = null;

	private String bibliography = null;

	private String note = null;

	private String prefercite = null;

	private String completeReference = null;

	private String handleURL = null;

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
	public DescriptionObject(String pid, String label, String contentModel,
			Date lastModifiedDate, Date createdDate, String state,
			String completeReference, DescriptionLevel level,
			String countryCode, String repositoryCode, String id, String title,
			String dateInitial, String dateFinal, String description,
			String parentPID, int subElementsCount)
			throws InvalidDescriptionLevel {
		super(pid, label, contentModel, lastModifiedDate, createdDate, state,
				level, countryCode, repositoryCode, id, title, dateInitial,
				dateFinal, description, parentPID, subElementsCount);
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
	public DescriptionObject(RODAObject object, String completeReference,
			DescriptionLevel level, String countryCode, String repositoryCode,
			String id, String unitTitle, String dateInitial, String dateFinal,
			String description, String parentPID, int subElementsCount)
			throws InvalidDescriptionLevel {
		this(object.getPid(), object.getLabel(), object.getContentModel(),
				object.getLastModifiedDate(), object.getCreatedDate(), object
						.getState(), completeReference, level, countryCode,
				repositoryCode, id, unitTitle, dateInitial, dateFinal,
				description, parentPID, subElementsCount);
	}

	/**
	 * Constructs a new Descriptive Object from the given
	 * SimpleDescriptionObject.
	 * 
	 * @param object
	 *            a SimpleDescriptionObject.
	 */
	public DescriptionObject(SimpleDescriptionObject object) {
		this(object.getPid(), object.getLabel(), object.getContentModel(),
				object.getLastModifiedDate(), object.getCreatedDate(), object
						.getState(), null, object.getLevel(), object
						.getCountryCode(), object.getRepositoryCode(), object
						.getId(), object.getTitle(), object.getDateInitial(),
				object.getDateFinal(), object.getDescription(), object
						.getParentPID(), object.getSubElementsCount());
	}

	/**
	 * Constructs a new Descriptive Object cloning an existing Description
	 * Object.
	 * 
	 * @param object
	 *            a Description Object.
	 */
	public DescriptionObject(DescriptionObject object) {
		this(object.getPid(), object.getLabel(), object.getContentModel(),
				object.getLastModifiedDate(), object.getCreatedDate(), object
						.getState(), object.getCompleteReference(), object
						.getLevel(), object.getCountryCode(), object
						.getRepositoryCode(), object.getId(),
				object.getTitle(), object.getDateInitial(), object
						.getDateFinal(), object.getDescription(), object
						.getParentPID(), object.getSubElementsCount());

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
		setAcqinfo(object.getAcqinfo());
		setScopecontent(object.getScopecontent());
		setAppraisal(object.getAppraisal());
		setAccruals(object.getAccruals());
		setArrangement(object.getArrangement());
		setArrangementTable(object.getArrangementTable());
		setAccessrestrict(object.getAccessrestrict());
		setUserestrict(object.getUserestrict());
		setLangmaterialLanguages(object.getLangmaterialLanguages());
		setPhystech(object.getPhystech());
		setMaterialspec(object.getMaterialspec());
		setOtherfindaid(object.getOtherfindaid());
		setRelatedmaterial(object.getRelatedmaterial());
		setBibliography(object.getBibliography());
		setPrefercite(object.getPrefercite());

		setCompleteReference(object.getCompleteReference());
		setHandleURL(object.getHandleURL());
	}

	/**
	 * @see SimpleDescriptionObject#toString()
	 */
	public String toString() {
		return "DescriptionObject ( " + super.toString()
				+ ", completeReference=" + getCompleteReference()
				+ ", handleURL=" + getHandleURL() + " )";
	}

	/**
	 * @see SimpleDescriptionObject#getValue(String)
	 */
	public EadCValue getValue(String element) throws IllegalArgumentException {

		EadCValue value = null;

		try {

			value = super.getValue(element);

		} catch (IllegalArgumentException e) {

			if (PHYSDESC.equals(element)) {
				value = getTextOrNull(getPhysdesc());
			} else if (PHYSDESC_DIMENSIONS.equals(element)) {
				value = getPhysdescDimensions();
			} else if (PHYSDESC_PHYSFACET.equals(element)) {
				value = getPhysdescPhysfacet();
			} else if (PHYSDESC_DATE_INITIAL.equals(element)) {
				value = getTextOrNull(getPhysdescDateInitial());
			} else if (PHYSDESC_DATE_FINAL.equals(element)) {
				value = getTextOrNull(getPhysdescDateFinal());
			} else if (PHYSDESC_EXTENT.equals(element)) {
				value = getPhysdescExtent();
			} else if (ORIGINATION.equals(element)) {
				value = getTextOrNull(getOrigination());
			} else if (BIOGHIST.equals(element)) {
				value = getTextOrNull(getBioghist());
			} else if (BIOGHIST_CHRONLIST.equals(element)) {
				value = getBioghistChronlist();
			} else if (CUSTODHIST.equals(element)) {
				value = getTextOrNull(getCustodhist());
			} else if (ACQINFO.equals(element)) {
				value = getTextOrNull(getAcqinfo());
			} else if (ACQINFO_NUM.equals(element)) {
				value = getTextOrNull(getAcqinfoNum());
			} else if (ACQINFO_DATE.equals(element)) {
				value = getTextOrNull(getAcqinfoDate());
			} else if (SCOPECONTENT.equals(element)) {
				value = getTextOrNull(getScopecontent());
			} else if (APPRAISAL.equals(element)) {
				value = getTextOrNull(getAppraisal());
			} else if (ACCRUALS.equals(element)) {
				value = getTextOrNull(getAccruals());
			} else if (ARRANGEMENT.equals(element)) {
				if (getArrangement() != null) {
					value = new Text(getArrangement());
				} else if (getArrangementTable() != null) {
					value = getArrangementTable();
				} else {
					value = null;
				}
				// } else if (ARRANGEMENT_TABLE.equals(element)) {
			} else if (ACCESSRESTRICT.equals(element)) {
				value = getTextOrNull(getAccessrestrict());
			} else if (USERESTRICT.equals(element)) {
				value = getTextOrNull(getUserestrict());
			} else if (LANGMATERIAL_LANGUAGES.equals(element)) {
				value = getLangmaterialLanguages();
			} else if (PHYSTECH.equals(element)) {
				value = getTextOrNull(getPhystech());
			} else if (MATERIALSPEC.equals(element)) {
				value = getTextOrNull(getMaterialspec());
			} else if (OTHERFINDAID.equals(element)) {
				value = getTextOrNull(getOtherfindaid());
			} else if (RELATEDMATERIAL.equals(element)) {
				value = getTextOrNull(getRelatedmaterial());
			} else if (BIBLIOGRAPHY.equals(element)) {
				value = getTextOrNull(getBibliography());
			} else if (NOTE.equals(element)) {
				value = getTextOrNull(getNote());
			} else if (PREFERCITE.equals(element)) {
				value = getTextOrNull(getPrefercite());

			} else if (COMPLETE_REFERENCE.equals(element)) {
				value = getTextOrNull(getCompleteReference());
			} else if (HANDLE_URL.equals(element)) {
				value = getTextOrNull(getHandleURL());
			} else {

				// no value named 'element'
				throw new IllegalArgumentException("Unknown element named "
						+ element);
			}

		}

		return value;
	}

	// public String[] getAssignedElements() {
	// return null;
	// }

	/**
	 * @see SimpleDescriptionObject#setValue(String, EadCValue)
	 */
	public void setValue(String element, EadCValue value)
			throws IllegalArgumentException {

		try {

			super.setValue(element, value);

		} catch (IllegalArgumentException e) {
			// SimpleDescriptionObject doesn't have this element

			// if (COMPLETE_REFERENCE.equals(element)) {
			// setCompleteReference(checkEadCText(element, value));
			// } else
			// if (HANDLE_URL.equals(element)) {
			// setHandleURL(checkEadCText(element, value));
			// } else

			if (PHYSDESC.equals(element)) {
				setPhysdesc(checkEadCText(element, value));
			} else if (PHYSDESC_DIMENSIONS.equals(element)) {
				setPhysdescDimensions(checkEadCPhysdescElement(element, value));
			} else if (PHYSDESC_PHYSFACET.equals(element)) {
				setPhysdescPhysfacet(checkEadCPhysdescElement(element, value));
			} else if (PHYSDESC_DATE_INITIAL.equals(element)) {
				setDateInitial(checkEadCText(element, value));
			} else if (PHYSDESC_DATE_FINAL.equals(element)) {
				setDateFinal(checkEadCText(element, value));
			} else if (PHYSDESC_EXTENT.equals(element)) {
				setPhysdescExtent(checkEadCPhysdescElement(element, value));
			} else if (ORIGINATION.equals(element)) {
				setOrigination(checkEadCText(element, value));
			} else if (BIOGHIST.equals(element)) {
				setBioghist(checkEadCText(element, value));
			} else if (BIOGHIST_CHRONLIST.equals(element)) {
				setBioghistChronlist(checkEadCBioghistChronlist(element, value));
			} else if (CUSTODHIST.equals(element)) {
				setCustodhist(checkEadCText(element, value));
			} else if (ACQINFO.equals(element)) {
				setAcqinfo(checkEadCText(element, value));
			} else if (ACQINFO_NUM.equals(element)) {
				setAcqinfoNum(checkEadCText(element, value));
			} else if (ACQINFO_DATE.equals(element)) {
				setAcqinfoDate(checkEadCText(element, value));
			} else if (SCOPECONTENT.equals(element)) {
				setScopecontent(checkEadCText(element, value));
			} else if (APPRAISAL.equals(element)) {
				setAppraisal(checkEadCText(element, value));
			} else if (ACCRUALS.equals(element)) {
				setAccruals(checkEadCText(element, value));
			} else if (ARRANGEMENT.equals(element)) {
				if (value instanceof Text) {
					setArrangement(value.toString());
				} else if (value instanceof ArrangementTable) {
					setArrangementTable((ArrangementTable) value);
				} else {
					throw new IllegalArgumentException(
							"illegal value for element " + element
									+ ". Value of type " + Text.class + " or "
									+ ArrangementTable.class + " was expected.");
				}
				// } else if (ARRANGEMENT_TABLE.equals(element)) {
			} else if (ACCESSRESTRICT.equals(element)) {
				setAccessrestrict(checkEadCText(element, value));
			} else if (USERESTRICT.equals(element)) {
				setUserestrict(checkEadCText(element, value));
			} else if (LANGMATERIAL_LANGUAGES.equals(element)) {
				setLangmaterialLanguages(checkEadCLangmaterialLanguages(
						element, value));
			} else if (PHYSTECH.equals(element)) {
				setPhystech(checkEadCText(element, value));
			} else if (MATERIALSPEC.equals(element)) {
				setMaterialspec(checkEadCText(element, value));
			} else if (OTHERFINDAID.equals(element)) {
				setOtherfindaid(checkEadCText(element, value));
			} else if (RELATEDMATERIAL.equals(element)) {
				setRelatedmaterial(checkEadCText(element, value));
			} else if (BIBLIOGRAPHY.equals(element)) {
				setBibliography(checkEadCText(element, value));
			} else if (NOTE.equals(element)) {
				setNote(checkEadCText(element, value));
			} else if (PREFERCITE.equals(element)) {
				setPrefercite(checkEadCText(element, value));
			} else {
				// no value named 'element'
				throw new IllegalArgumentException("Unknown element named "
						+ element);
			}

		}

	}

	private LangmaterialLanguages checkEadCLangmaterialLanguages(
			String element, EadCValue value) throws IllegalArgumentException {
		if (value == null) {
			return null;
		} else if (value instanceof LangmaterialLanguages) {
			return (LangmaterialLanguages) value;
		} else {
			throw new IllegalArgumentException("illegal value for element "
					+ element + ". Value of type "
					+ LangmaterialLanguages.class + " or null was expected.");
		}
	}

	protected BioghistChronlist checkEadCBioghistChronlist(String element,
			EadCValue value) throws IllegalArgumentException {
		if (value == null) {
			return null;
		} else if (value instanceof BioghistChronlist) {
			return (BioghistChronlist) value;
		} else {
			throw new IllegalArgumentException("illegal value for element "
					+ element + ". Value of type " + BioghistChronlist.class
					+ " or null was expected.");
		}
	}

	protected PhysdescElement checkEadCPhysdescElement(String element,
			EadCValue value) throws IllegalArgumentException {
		if (value == null) {
			return null;
		} else if (value instanceof PhysdescElement) {
			return (PhysdescElement) value;
		} else {
			throw new IllegalArgumentException("illegal value for element "
					+ element + ". Value of type " + PhysdescElement.class
					+ " or null was expected.");
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
	 *            the accessrestrict to set
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
	 *            the accruals to set
	 */
	public void setAccruals(String accruals) {
		this.accruals = accruals;
	}

	/**
	 * @return the acqinfo
	 */
	public String getAcqinfo() {
		return acqinfo;
	}

	/**
	 * @param acqinfo
	 *            the acqinfo to set
	 */
	public void setAcqinfo(String acqinfo) {
		this.acqinfo = acqinfo;
	}

	/**
	 * @return the acqinfoNum
	 */
	public String getAcqinfoNum() {
		return acqinfoNum;
	}

	/**
	 * @param acqinfoNum the acqinfoNum to set
	 */
	public void setAcqinfoNum(String acqinfoNum) {
		this.acqinfoNum = acqinfoNum;
	}

	/**
	 * @return the acqinfoDate
	 */
	public String getAcqinfoDate() {
		return acqinfoDate;
	}

	/**
	 * @param acqinfoDate the acqinfoDate to set
	 */
	public void setAcqinfoDate(String acqinfoDate) {
		this.acqinfoDate = acqinfoDate;
	}

	/**
	 * @return the appraisal
	 */
	public String getAppraisal() {
		return appraisal;
	}

	/**
	 * @param appraisal
	 *            the appraisal to set
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
	 *            the arrangement to set
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
	 *            the arrangementTable to set
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
	 *            the bibliography to set
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
	 *            the bioghist to set
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
	 *            the bioghistChronlist to set
	 */
	public void setBioghistChronlist(BioghistChronlist bioghistChronlist) {
		this.bioghistChronlist = bioghistChronlist;
	}

	/**
	 * @return the countryCode
	 * @deprecated use {@link SimpleDescriptionObject#getRepositoryCode()}
	 *             instead.
	 */
	// public String getCountryCode() {
	// return countryCode;
	// }
	/**
	 * @param countryCode
	 *            the countryCode to set
	 * @deprecated use {@link SimpleDescriptionObject#setRepositoryCode(String)}
	 *             to set the country and repository code.
	 */
	// public void setCountryCode(String countryCode) {
	// this.countryCode = countryCode;
	// }
	/**
	 * @return the langmaterialLanguages
	 */
	public LangmaterialLanguages getLangmaterialLanguages() {
		return this.langmaterialLanguages;
	}

	/**
	 * @param langmaterialLanguages
	 *            the langmaterialLanguages to set
	 */
	public void setLangmaterialLanguages(
			LangmaterialLanguages langmaterialLanguages) {
		this.langmaterialLanguages = langmaterialLanguages;
	}

	/**
	 * @return the materialspec
	 */
	public String getMaterialspec() {
		return materialspec;
	}

	/**
	 * @param materialspec
	 *            the materialspec to set
	 */
	public void setMaterialspec(String materialspec) {
		this.materialspec = materialspec;
	}

	/**
	 * @return the origination
	 */
	public String getOrigination() {
		return origination;
	}

	/**
	 * @param origination
	 *            the origination to set
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
	 *            the otherfindaid to set
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
	 *            the physdesc to set
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
	 *            the physdescDateFinal to set
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
	 *            the physdescDateInitial to set
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
	 *            the physdescDimensions to set
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
	 *            the physdescExtent to set
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
	 *            the physdescPhysfacet to set
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
	 *            the phystech to set
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
	 *            the prefercite to set
	 */
	public void setPrefercite(String prefercite) {
		this.prefercite = prefercite;
	}

	/**
	 * @return the relatedmaterial
	 */
	public String getRelatedmaterial() {
		return relatedmaterial;
	}

	/**
	 * @param relatedmaterial
	 *            the relatedmaterial to set
	 */
	public void setRelatedmaterial(String relatedmaterial) {
		this.relatedmaterial = relatedmaterial;
	}

	/**
	 * @return the userestrict
	 */
	public String getUserestrict() {
		return userestrict;
	}

	/**
	 * @param userestrict
	 *            the userestrict to set
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
	 *            the scopecontent to set
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
	 *            the custodhist to set
	 */
	public void setCustodhist(String custodhist) {
		this.custodhist = custodhist;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the completeReference
	 */
	public String getCompleteReference() {
		return completeReference;
	}

	/**
	 * @param completeReference
	 *            the completeReference to set
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
	 *            the handleURL to set
	 */
	public void setHandleURL(String handleURL) {
		this.handleURL = handleURL;
	}

}
