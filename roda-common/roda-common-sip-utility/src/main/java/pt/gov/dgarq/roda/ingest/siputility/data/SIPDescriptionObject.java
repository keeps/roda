package pt.gov.dgarq.roda.ingest.siputility.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.ArrangementTable;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.core.data.eadc.PhysdescElement;

/**
 * This a description object contained inside a {@link SIP}.
 * 
 * @author Rui Castro
 * @author Luis Faria
 */
public class SIPDescriptionObject extends DescriptionObject implements
		DataChangeListener {
	private static final long serialVersionUID = -3475890756402659980L;

	private static final Logger logger = Logger
			.getLogger(SIPDescriptionObject.class);

	private Set<DataChangeListener> dataChangeListeners = new HashSet<DataChangeListener>();

	private File file = null;

	private Set<SIPDescriptionObject> children = new LinkedHashSet<SIPDescriptionObject>();
	private Set<SIPRepresentationObject> representations = new LinkedHashSet<SIPRepresentationObject>();

	/**
	 * Constructs a new {@link SIPDescriptionObject}.
	 */
	public SIPDescriptionObject() {
	}

	/**
	 * Constructs a new {@link SIPDescriptionObject} cloning an existing
	 * {@link DescriptionObject}.
	 * 
	 * @param dObject
	 */
	public SIPDescriptionObject(DescriptionObject dObject) {
		super(dObject);
	}

	/**
	 * Constructs a new {@link SIPDescriptionObject} cloning an existing
	 * {@link SIPDescriptionObject}.
	 * 
	 * @param dObject
	 */
	public SIPDescriptionObject(SIPDescriptionObject dObject) {
		super(dObject);
		setFile(dObject.getFile());
		setChildren(dObject.getChildren());
		setRepresentations(dObject.getRepresentations());
	}

	/**
	 * @see DescriptionObject#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (file=" + getFile() + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ super.toString() + ", children=" + this.children.size() //$NON-NLS-1$
				+ ", representations=" + this.representations.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return the eadcFile
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param eadcFile
	 *            the eadcFile to set
	 */
	public void setFile(File eadcFile) {
		this.file = eadcFile;
	}

	@Override
	public void setAccessrestrict(String accessrestrict) {
		super.setAccessrestrict(accessrestrict);
		fireDataChangedEvent();
	}

	@Override
	public void setAccruals(String accruals) {
		super.setAccruals(accruals);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setAcqinfo(String acqinfo) {
		super.setAcqinfo(acqinfo);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setAcqinfoDate(String acqinfoDate) {
		super.setAcqinfoDate(acqinfoDate);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setAcqinfoNum(String acqinfoNum) {
		super.setAcqinfoNum(acqinfoNum);
		fireDataChangedEvent();
	}

	@Override
	public void setAppraisal(String appraisal) {
		super.setAppraisal(appraisal);
		fireDataChangedEvent();
	}

	@Override
	public void setArrangement(String arrangement) {
		super.setArrangement(arrangement);
		fireDataChangedEvent();
	}

	@Override
	public void setArrangementTable(ArrangementTable arrangementTable) {
		super.setArrangementTable(arrangementTable);
		fireDataChangedEvent();
	}

	@Override
	public void setBibliography(String bibliography) {
		super.setBibliography(bibliography);
		fireDataChangedEvent();
	}

	@Override
	public void setBioghist(String bioghist) {
		super.setBioghist(bioghist);
		fireDataChangedEvent();
	}

	@Override
	public void setBioghistChronlist(BioghistChronlist bioghistChronlist) {
		super.setBioghistChronlist(bioghistChronlist);
		fireDataChangedEvent();
	}

	@Override
	public void setCompleteReference(String completeReference) {
		super.setCompleteReference(completeReference);
		fireDataChangedEvent();
	}

	@Override
	public void setContentModel(String contentModel) {
		super.setContentModel(contentModel);
		fireDataChangedEvent();
	}

	@Override
	public void setCountryCode(String countryCode) {
		super.setCountryCode(countryCode);
		fireDataChangedEvent();
	}

	@Override
	public void setCustodhist(String custodhist) {
		super.setCustodhist(custodhist);
		fireDataChangedEvent();
	}

	@Override
	public void setDateFinal(String dateFinal) {
		super.setDateFinal(dateFinal);
		fireDataChangedEvent();
	}

	@Override
	public void setDateInitial(String dateInitial) {
		super.setDateInitial(dateInitial);
		fireDataChangedEvent();
	}

	@Override
	public void setDescription(String description) {
		super.setDescription(description);
		fireDataChangedEvent();
	}

	@Override
	public void setId(String id) {
		super.setId(id);
		updateRepresentationsIdReference(id);
		fireDataChangedEvent();
	}

	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		fireDataChangedEvent();
	}

	@Override
	public void setLangmaterialLanguages(
			LangmaterialLanguages langmaterialLanguages) {
		super.setLangmaterialLanguages(langmaterialLanguages);
		fireDataChangedEvent();
	}

	@Override
	public void setLastModifiedDate(Date lastModifiedDate) {
		super.setLastModifiedDate(lastModifiedDate);
		fireDataChangedEvent();
	}

	@Override
	public void setCreatedDate(Date createdDate) {
		super.setCreatedDate(createdDate);
		fireDataChangedEvent();
	}

	@Override
	public void setState(String state) {
		super.setState(state);
		fireDataChangedEvent();
	}

	@Override
	public void setLevel(DescriptionLevel level) {
		super.setLevel(level);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setMaterialspec(String materialspec) {
		super.setMaterialspec(materialspec);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setNote(String note) {
		super.setNote(note);
		fireDataChangedEvent();
	}

	@Override
	public void setOrigination(String origination) {
		super.setOrigination(origination);
		fireDataChangedEvent();
	}

	@Override
	public void setOtherfindaid(String otherfindaid) {
		super.setOtherfindaid(otherfindaid);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdesc(String physdesc) {
		super.setPhysdesc(physdesc);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdescDateFinal(String physdescDateFinal) {
		super.setPhysdescDateFinal(physdescDateFinal);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdescDateInitial(String physdescDateInitial) {
		super.setPhysdescDateInitial(physdescDateInitial);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdescDimensions(PhysdescElement physdescDimensions) {
		super.setPhysdescDimensions(physdescDimensions);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdescExtent(PhysdescElement physdescExtent) {
		super.setPhysdescExtent(physdescExtent);
		fireDataChangedEvent();
	}

	@Override
	public void setPhysdescPhysfacet(PhysdescElement physdescPhysfacet) {
		super.setPhysdescPhysfacet(physdescPhysfacet);
		fireDataChangedEvent();
	}

	@Override
	public void setPhystech(String phystech) {
		super.setPhystech(phystech);
		fireDataChangedEvent();
	}

	@Override
	public void setPid(String pid) {
		super.setPid(pid);
		fireDataChangedEvent();
	}

	@Override
	public void setPrefercite(String prefercite) {
		super.setPrefercite(prefercite);
		fireDataChangedEvent();
	}

	@Deprecated
	@Override
	public void setRelatedmaterial(String relatedmaterial) {
		super.setRelatedmaterial(relatedmaterial);
		fireDataChangedEvent();
	}

	@Override
	public void setRepositoryCode(String repositoryCode) {
		super.setRepositoryCode(repositoryCode);
		fireDataChangedEvent();
	}

	@Override
	public void setScopecontent(String scopecontent) {
		super.setScopecontent(scopecontent);
		fireDataChangedEvent();
	}

	@Override
	public void setSubElementsCount(int count) {
		super.setSubElementsCount(count);
		fireDataChangedEvent();
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		fireDataChangedEvent();
	}

	@Override
	public void setUserestrict(String userestrict) {
		super.setUserestrict(userestrict);
		fireDataChangedEvent();
	}

	/**
	 * @return the child DOs
	 */
	public List<SIPDescriptionObject> getChildren() {
		return new ArrayList<SIPDescriptionObject>(children);
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<SIPDescriptionObject> children) {

		for (SIPDescriptionObject childDO : this.children) {
			childDO.removeChangeListener(this);
		}
		this.children.clear();

		if (children != null) {
			for (SIPDescriptionObject childDO : children) {

				childDO.addChangeListener(this);
				this.children.add(childDO);
			}
		}

		fireDataChangedEvent();
	}

	/**
	 * Adds a child description object.
	 * 
	 * @param childDO
	 *            the child DO to add
	 */
	public void addChild(SIPDescriptionObject childDO) {
		if (childDO != null && !this.children.contains(childDO)) {

			childDO.addChangeListener(this);
			this.children.add(childDO);

			fireDataChangedEvent();
		}
	}

	/**
	 * @param childDO
	 *            the child DO to remove
	 */
	public void removeChild(SIPDescriptionObject childDO) {
		if (childDO != null && this.children.contains(childDO)) {

			childDO.removeChangeListener(this);
			this.children.remove(childDO);

			fireDataChangedEvent();
		}
	}

	/**
	 * @return a {@link List} of representation objects
	 */
	public List<SIPRepresentationObject> getRepresentations() {
		return new ArrayList<SIPRepresentationObject>(representations);
	}

	/**
	 * @param representations
	 */
	public void setRepresentations(List<SIPRepresentationObject> representations) {

		for (SIPRepresentationObject representation : this.representations) {
			representation.removeChangeListener(this);
			representation.setDescriptionObjectPID(null);
		}
		this.representations.clear();

		if (representations != null) {
			for (SIPRepresentationObject representation : representations) {

				representation.setDescriptionObject(this);
				representation.setDescriptionObjectPID(getId());
				representation.addChangeListener(this);

				this.representations.add(representation);
			}
		}

		fireDataChangedEvent();
	}

	/**
	 * @param representation
	 */
	public void addRepresentation(SIPRepresentationObject representation) {
		if (representation != null
				&& !this.representations.contains(representation)) {

			representation.setDescriptionObject(this);
			representation.setDescriptionObjectPID(getId());
			representation.addChangeListener(this);

			this.representations.add(representation);

			fireDataChangedEvent();
		}
	}

	/**
	 * @param representation
	 */
	public void removeRepresentation(SIPRepresentationObject representation) {
		if (representation != null
				&& this.representations.contains(representation)) {

			representation.removeChangeListener(this);
			representation.setDescriptionObject(null);
			representation.setDescriptionObjectPID(null);

			this.representations.remove(representation);

			fireDataChangedEvent();
		}
	}

	/**
	 * @param listener
	 */
	synchronized public void addChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	synchronized public void removeChangeListener(DataChangeListener listener) {
		dataChangeListeners.remove(listener);
	}

	/**
	 * @see DataChangeListener#dataChanged(DataChangedEvent)
	 */
	public void dataChanged(DataChangedEvent evtDataChanged) {

		if (evtDataChanged.getSource() instanceof SIPDescriptionObject) {

			if (this.children.contains(evtDataChanged.getSource())) {
				fireDataChangedEvent(evtDataChanged);
			} else {
				logger.debug("SIPDescriptionObject has changed, but is not one of mine. Why am I listening to this???"); //$NON-NLS-1$
			}

		} else if (evtDataChanged.getSource() instanceof SIPRepresentationObject) {

			if (this.representations.contains(evtDataChanged.getSource())) {
				fireDataChangedEvent(evtDataChanged);
			} else {
				logger.debug("SIPRepresentationObject has changed, but is not one of mine. Why am I listening to this???"); //$NON-NLS-1$
			}

		} else {
			logger.warn("dataChanged called but event source is " //$NON-NLS-1$
					+ evtDataChanged.getSource());
		}
	}

	private void fireDataChangedEvent() {
		fireDataChangedEvent(null);
	}

	synchronized private void fireDataChangedEvent(EventObject causeEvent) {

		DataChangedEvent event = new DataChangedEvent(this, causeEvent);

		if (dataChangeListeners != null) {
			for (DataChangeListener listener : dataChangeListeners) {
				listener.dataChanged(event);
			}
		}
	}

	private void updateRepresentationsIdReference(String id) {
		if (this.representations != null) {
			for (SIPRepresentationObject sipRO : this.representations) {
				sipRO.setDescriptionObjectPID(id);
			}
		}
	}

	/**
	 * Test if the object is equal (the same one) as this
	 * 
	 * @param object
	 * @return true if equal
	 */
	public boolean equals(Object object) {
		return this == object;
	}

}
