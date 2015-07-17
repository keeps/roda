/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationConstants;
import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.widgets.LoadingPopup;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.BrowserService;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.CollectionsTreeItem;
import pt.gov.dgarq.roda.wui.management.editor.client.EditorService;

/**
 * @author Luis Faria
 * 
 */
public class DescriptiveMetadataPanel extends Composite implements SourcesChangeEvents {

	private static ClientLogger logger = new ClientLogger(DescriptiveMetadataPanel.class.getName());

	private static DisseminationConstants constants = (DisseminationConstants) GWT.create(DisseminationConstants.class);

	private final String pid;

	private DescriptionObject descObj;

	private final VerticalPanel layout;

	private boolean readonly;

	private boolean optionalVisible;

	private final boolean valuesAsHtml;

	private DescriptionGroupPanel identification;

	private DescriptionElement id;

	private DescriptionElement level;

	private DescriptionElement title;

	private DescriptionElement abstractNote;

	private DescriptionElement dateInitial;

	private DescriptionElement dateFinal;

	private DescriptionGroupPanel context;

	private DescriptionGroupPanel content;

	private DescriptionGroupPanel access;

	private DescriptionGroupPanel relatedMaterials;

	private DescriptionGroupPanel notes;

	private DescriptionGroupPanel processInfo;

	private final List<ChangeListener> listeners;

	private boolean initialized;

	private interface LoadListener {
		public void onLoadFinished();
	}

	private final List<LoadListener> loadlisteners;

	public DescriptiveMetadataPanel() {
		this((String) null, new AsyncCallback<DescriptionObject>() {

			public void onFailure(Throwable caught) {
				logger.error("Error initializing descriptive metadata panel", caught);
			}

			public void onSuccess(DescriptionObject obj) {
				// nothing to do
			}

		});
	}

	public DescriptiveMetadataPanel(String pid, AsyncCallback<DescriptionObject> callback) {
		this(pid, false, callback);
	}

	public DescriptiveMetadataPanel(String pid, boolean valuesAsHtml, AsyncCallback<DescriptionObject> callback) {
		super();
		this.pid = pid;
		this.descObj = null;
		this.valuesAsHtml = valuesAsHtml;
		layout = new VerticalPanel();
		readonly = true;
		optionalVisible = true;
		listeners = new Vector<ChangeListener>();
		loadlisteners = new Vector<LoadListener>();
		initialized = false;
		initWidget(layout);
		init(callback);

		this.addStyleName("descriptionMetadata");
	}

	public DescriptiveMetadataPanel(DescriptionObject descObj) {
		this(descObj, false);
	}

	public DescriptiveMetadataPanel(DescriptionObject descObj, boolean valuesAsHtml) {
		super();
		this.pid = descObj.getPid();
		this.descObj = descObj;
		this.valuesAsHtml = valuesAsHtml;
		layout = new VerticalPanel();
		readonly = true;
		optionalVisible = true;
		listeners = new Vector<ChangeListener>();
		loadlisteners = new Vector<LoadListener>();
		initWidget(layout);
		createGroups();
		initialized = true;
		this.addStyleName("descriptionMetadata");
	}

	private void init(final AsyncCallback<DescriptionObject> callback) {
		if (pid != null) {
			BrowserService.Util.getInstance().getDescriptionObject(pid, new AsyncCallback<DescriptionObject>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(DescriptionObject obj) {
					descObj = obj;
					postInit();
					callback.onSuccess(descObj);
				}

			});
		} else {
			EditorService.Util.getInstance().getDefaultDescriptionObject(new AsyncCallback<DescriptionObject>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(DescriptionObject obj) {
					descObj = obj;
					descObj.setLevel(DescriptionLevelUtils.REPRESENTATION_DESCRIPTION_LEVELS.get(0));
					postInit();
					callback.onSuccess(descObj);
				}

			});

		}

	}

	private void postInit() {
		createGroups();

		// Enforce readonly state
		identification.setReadonly(readonly);
		context.setReadonly(readonly);
		content.setReadonly(readonly);
		access.setReadonly(readonly);
		relatedMaterials.setReadonly(readonly);
		notes.setReadonly(readonly);
		processInfo.setReadonly(readonly);

		// Enforce optional visible state
		identification.setOptionalVisible(optionalVisible);
		context.setOptionalVisible(optionalVisible);
		content.setOptionalVisible(optionalVisible);
		access.setOptionalVisible(optionalVisible);
		relatedMaterials.setOptionalVisible(optionalVisible);
		notes.setOptionalVisible(optionalVisible);
		processInfo.setOptionalVisible(optionalVisible);

		initialized = true;
		for (LoadListener listener : loadlisteners) {
			listener.onLoadFinished();
		}
		loadlisteners.clear();
	}

	private void ensureLoaded(LoadListener listener) {
		if (initialized) {
			listener.onLoadFinished();
		} else {
			loadlisteners.add(listener);
		}
	}

	private void createGroups() {
		identification = createIdentificationGroup();
		context = createContextGroup();
		content = createContentGroup();
		access = createAccessGroup();
		relatedMaterials = createRelatedMaterialsGroup();
		notes = createNotesGroup();
		processInfo = createProcessInfoGroup();

		layout.add(identification);
		layout.add(context);
		layout.add(content);
		layout.add(access);
		layout.add(relatedMaterials);
		layout.add(notes);
		layout.add(processInfo);

		identification.setOpen(true);
		context.setOpen(true);
		content.setOpen(true);
		access.setOpen(true);
		relatedMaterials.setOpen(true);
		notes.setOpen(true);
		processInfo.setOpen(true);

		ChangeListener listener = new ChangeListener() {

			public void onChange(Widget sender) {
				DescriptiveMetadataPanel.this.onChange(sender);
			}

		};

		identification.addChangeListener(listener);
		context.addChangeListener(listener);
		content.addChangeListener(listener);
		access.addChangeListener(listener);
		relatedMaterials.addChangeListener(listener);
		notes.addChangeListener(listener);
		processInfo.addChangeListener(listener);
	}

	private DescriptionGroupPanel createIdentificationGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.identificationGroupLabel(), descObj);
		id = new DescriptionElement(descObj, DescriptionObject.ID, constants.reference(), true, valuesAsHtml);
		DescriptionElement completeReference = new DescriptionElement(descObj, DescriptionObject.COMPLETE_REFERENCE,
				constants.completeReference(), false, valuesAsHtml);

		DescriptionElement handle = new DescriptionElement(descObj, DescriptionObject.HANDLE_URL, constants.handle(),
				false, valuesAsHtml);

		title = new DescriptionElement(descObj, DescriptionObject.TITLE, constants.title(), true, valuesAsHtml);

		abstractNote = new DescriptionElement(descObj, DescriptionObject.ABSTRACT, constants.abstractNote(), false,
				valuesAsHtml);

		level = new DescriptionElement(descObj, DescriptionObject.LEVEL, constants.level(), true, valuesAsHtml);

		dateInitial = new DescriptionElement(descObj, DescriptionObject.DATE_INITIAL, constants.dateInitial(), true,
				valuesAsHtml);
		dateFinal = new DescriptionElement(descObj, DescriptionObject.DATE_FINAL, constants.dateFinal(), true,
				valuesAsHtml);

		DescriptionElement countryCode = new DescriptionElement(descObj, DescriptionObject.COUNTRYCODE,
				constants.countryCode(), true, valuesAsHtml);

		DescriptionElement repositoryCode = new DescriptionElement(descObj, DescriptionObject.REPOSITORYCODE,
				constants.repositoryCode(), true, valuesAsHtml);

		DescriptionElement origination = new DescriptionElement(descObj, DescriptionObject.ORIGINATION,
				constants.origination(), true, valuesAsHtml);

		// DescriptionElement acqInfoNum = new DescriptionElement(descObj,
		// DescriptionObject.ACQINFO_NUM, constants.acqInfoNum(), false,
		// valuesAsHtml);
		//
		// DescriptionElement acqInfoDate = new DescriptionElement(descObj,
		// DescriptionObject.ACQINFO_DATE, constants.acqInfoDate(), false,
		// valuesAsHtml);

		DescriptionElement materialSpec = new DescriptionElement(descObj, DescriptionObject.MATERIALSPEC,
				constants.materialspec(), false, valuesAsHtml);

		DescriptionElement physDescGenreform = new DescriptionElement(descObj, DescriptionObject.PHYSDESC_GENREFORM,
				constants.physDescGenreform(), false, valuesAsHtml);

		DescriptionElement physDesc = new DescriptionElement(descObj, DescriptionObject.PHYSDESC, constants.physDesc(),
				false, valuesAsHtml);

		DescriptionElement physDescDateInitial = new DescriptionElement(descObj,
				DescriptionObject.PHYSDESC_DATE_INITIAL, constants.physDescDateInitial(), false, valuesAsHtml);

		DescriptionElement physDescDateFinal = new DescriptionElement(descObj, DescriptionObject.PHYSDESC_DATE_FINAL,
				constants.physDescDateFinal(), false, valuesAsHtml);

		DescriptionElement physDescDimensions = new DescriptionElement(descObj, DescriptionObject.PHYSDESC_DIMENSIONS,
				constants.physDescDimensions(), false, valuesAsHtml);

		DescriptionElement physDescPhysFacet = new DescriptionElement(descObj, DescriptionObject.PHYSDESC_PHYSFACET,
				constants.physDescPhysFacet(), false, valuesAsHtml);

		DescriptionElement physDescExtent = new DescriptionElement(descObj, DescriptionObject.PHYSDESC_EXTENT,
				constants.physDescExtent(), false, valuesAsHtml);

		DescriptionElement languages = new DescriptionElement(descObj, DescriptionObject.LANGMATERIAL_LANGUAGES,
				constants.langMaterialLanguages(), false, valuesAsHtml);

		DescriptionElement prefercite = new DescriptionElement(descObj, DescriptionObject.PREFERCITE,
				constants.preferCite(), false, valuesAsHtml);

		id.addEditMode(DescriptionElement.EditMode.TEXT_LINE);
		title.addEditMode(DescriptionElement.EditMode.TEXT_LINE);
		abstractNote.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		level.addEditMode(DescriptionElement.EditMode.LEVEL);
		dateInitial.addEditMode(DescriptionElement.EditMode.DATE);
		dateFinal.addEditMode(DescriptionElement.EditMode.DATE);
		countryCode.addEditMode(DescriptionElement.EditMode.COUNTRYCODE);
		repositoryCode.addEditMode(DescriptionElement.EditMode.TEXT_LINE);
		origination.addEditMode(DescriptionElement.EditMode.TEXT_LINE);
		materialSpec.addEditMode(DescriptionElement.EditMode.MATERIAL_SPECS);
		physDescGenreform.addEditMode(DescriptionElement.EditMode.PHYSDESC_GENREFORM);
		physDesc.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		physDescDateInitial.addEditMode(DescriptionElement.EditMode.DATE);
		physDescDateFinal.addEditMode(DescriptionElement.EditMode.DATE);
		physDescDimensions.addEditMode(DescriptionElement.EditMode.PHYSDESC_DIMENSIONS);
		physDescPhysFacet.addEditMode(DescriptionElement.EditMode.PHYSDESC_PHYSFACET);
		physDescExtent.addEditMode(DescriptionElement.EditMode.PHYSDESC_EXTENT);
		languages.addEditMode(DescriptionElement.EditMode.LANGUAGES_LIST);
		prefercite.addEditMode(DescriptionElement.EditMode.TEXT_LINE);

		group.addElement(id);
		group.addElement(completeReference);
		group.addElement(handle);
		group.addElement(title);
		group.addElement(abstractNote);
		group.addElement(level);
		group.addElement(dateInitial);
		group.addElement(dateFinal);
		group.addElement(countryCode);
		group.addElement(repositoryCode);
		group.addElement(origination);
		// group.addElement(acqInfoNum);
		// group.addElement(acqInfoDate);
		group.addElement(materialSpec);
		group.addElement(physDescGenreform);
		group.addElement(physDesc);
		group.addElement(physDescDateInitial);
		group.addElement(physDescDateFinal);
		group.addElement(physDescDimensions);
		group.addElement(physDescPhysFacet);
		group.addElement(physDescExtent);
		group.addElement(languages);
		group.addElement(prefercite);

		return group;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(final boolean readonly) {
		if (!valuesAsHtml) {
			if (this.readonly != readonly) {
				ensureLoaded(new LoadListener() {

					public void onLoadFinished() {
						DescriptiveMetadataPanel.this.readonly = readonly;
						identification.setReadonly(readonly);
						context.setReadonly(readonly);
						content.setReadonly(readonly);
						access.setReadonly(readonly);
						relatedMaterials.setReadonly(readonly);
						notes.setReadonly(readonly);
						processInfo.setReadonly(readonly);
					}

				});
			}
		}
	}

	public boolean isOptionalVisible() {
		return optionalVisible;
	}

	public void setOptionalVisible(final boolean optionalVisible) {
		if (!valuesAsHtml && this.optionalVisible != optionalVisible) {
			this.optionalVisible = optionalVisible;
			ensureLoaded(new LoadListener() {
				public void onLoadFinished() {
					identification.setOptionalVisible(optionalVisible);
					context.setOptionalVisible(optionalVisible);
					content.setOptionalVisible(optionalVisible);
					access.setOptionalVisible(optionalVisible);
					relatedMaterials.setOptionalVisible(optionalVisible);
					notes.setOptionalVisible(optionalVisible);
					processInfo.setOptionalVisible(optionalVisible);
				}

			});
		}
	}

	private DescriptionGroupPanel createContextGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.contextGroupLabel(), descObj);

		DescriptionElement bioghist = new DescriptionElement(descObj, DescriptionObject.BIOGHIST, constants.bioghist(),
				false, valuesAsHtml);
		DescriptionElement chronlist = new DescriptionElement(descObj, DescriptionObject.BIOGHIST_CHRONLIST,
				constants.bioghistChronlist(), false, valuesAsHtml);
		DescriptionElement custodhist = new DescriptionElement(descObj, DescriptionObject.CUSTODHIST,
				constants.custodhist(), false, valuesAsHtml);
		DescriptionElement acqinfo = new DescriptionElement(descObj, DescriptionObject.ACQINFO, constants.acqinfo(),
				false, valuesAsHtml);

		bioghist.addEditMode(DescriptionElement.EditMode.TEXT_BIGAREA);
		chronlist.addEditMode(DescriptionElement.EditMode.CHRON_LIST);
		custodhist.addEditMode(DescriptionElement.EditMode.TEXT_BIGAREA);
		acqinfo.addEditMode(DescriptionElement.EditMode.ACQUISITIONS_INFOS);

		group.addElement(bioghist);
		group.addElement(chronlist);
		group.addElement(custodhist);
		group.addElement(acqinfo);

		return group;
	}

	private DescriptionGroupPanel createContentGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.contentGroupLabel(), descObj);

		DescriptionElement scopeContent = new DescriptionElement(descObj, DescriptionObject.SCOPECONTENT,
				constants.scopeContent(), true, valuesAsHtml);
		DescriptionElement arrangement = new DescriptionElement(descObj, DescriptionObject.ARRANGEMENT,
				constants.arrangement(), false, valuesAsHtml);
		DescriptionElement appraisal = new DescriptionElement(descObj, DescriptionObject.APPRAISAL,
				constants.appraisal(), false, valuesAsHtml);
		DescriptionElement accruals = new DescriptionElement(descObj, DescriptionObject.ACCRUALS, constants.accruals(),
				false, valuesAsHtml);

		scopeContent.addEditMode(DescriptionElement.EditMode.TEXT_BIGAREA);
		arrangement.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		arrangement.addEditMode(DescriptionElement.EditMode.ARRANGEMENT_TABLE);
		appraisal.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		accruals.addEditMode(DescriptionElement.EditMode.TEXT_AREA);

		group.addElement(scopeContent);
		group.addElement(arrangement);
		group.addElement(appraisal);
		group.addElement(accruals);

		return group;

	}

	private DescriptionGroupPanel createAccessGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.accessGroupLabel(), descObj);

		DescriptionElement phystech = new DescriptionElement(descObj, DescriptionObject.PHYSTECH, constants.physTech(),
				false, valuesAsHtml);
		DescriptionElement accessRestrict = new DescriptionElement(descObj, DescriptionObject.ACCESSRESTRICT,
				constants.accessRestrict(), false, valuesAsHtml);
		DescriptionElement useRestrict = new DescriptionElement(descObj, DescriptionObject.USERESTRICT,
				constants.useRestrict(), false, valuesAsHtml);
		DescriptionElement controlAccesses = new DescriptionElement(descObj, DescriptionObject.CONTROLACCESS,
				constants.controlAccesses(), false, valuesAsHtml);

		phystech.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		accessRestrict.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		useRestrict.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		controlAccesses.addEditMode(DescriptionElement.EditMode.CONTROL_ACCESSES);

		group.addElement(phystech);
		group.addElement(accessRestrict);
		group.addElement(useRestrict);
		group.addElement(controlAccesses);

		return group;
	}

	private DescriptionGroupPanel createRelatedMaterialsGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.relatedMaterialsGroupLabel(), descObj);
		DescriptionElement relatedMaterial = new DescriptionElement(descObj, DescriptionObject.RELATEDMATERIAL,
				constants.relatedMaterialsGroupLabel(), false, valuesAsHtml);
		DescriptionElement otherFindAid = new DescriptionElement(descObj, DescriptionObject.OTHERFINDAID,
				constants.otherFindAid(), false, valuesAsHtml);

		relatedMaterial.addEditMode(DescriptionElement.EditMode.RELATED_MATERIALS);
		otherFindAid.addEditMode(DescriptionElement.EditMode.TEXT_AREA);

		group.addElement(relatedMaterial);
		group.addElement(otherFindAid);

		return group;
	}

	private DescriptionGroupPanel createNotesGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.notesGroupLabel(), descObj);

		DescriptionElement notes = new DescriptionElement(descObj, DescriptionObject.NOTE, constants.notes(), false,
				valuesAsHtml);
		DescriptionElement keywords = new DescriptionElement(descObj, DescriptionObject.INDEX, constants.keywords(),
				false, valuesAsHtml);
		DescriptionElement bibliography = new DescriptionElement(descObj, DescriptionObject.BIBLIOGRAPHY,
				constants.bibliography(), false, valuesAsHtml);
		DescriptionElement odd = new DescriptionElement(descObj, DescriptionObject.ODD, constants.odd(), false,
				valuesAsHtml);

		notes.addEditMode(DescriptionElement.EditMode.NOTES);
		keywords.addEditMode(DescriptionElement.EditMode.KEYWORDS);
		bibliography.addEditMode(DescriptionElement.EditMode.TEXT_AREA);
		odd.addEditMode(DescriptionElement.EditMode.TEXT_AREA);

		group.addElement(notes);
		group.addElement(keywords);
		group.addElement(bibliography);
		group.addElement(odd);

		return group;
	}

	private DescriptionGroupPanel createProcessInfoGroup() {
		DescriptionGroupPanel group = new DescriptionGroupPanel(constants.processInfoGroupLabel(), descObj);

		DescriptionElement redaction = new DescriptionElement(descObj, DescriptionObject.PROCESSINFO,
				constants.redaction(), false, valuesAsHtml);

		redaction.addEditMode(DescriptionElement.EditMode.PROCESS_INFO);

		group.addElement(redaction);

		return group;
	}

	/**
	 * Save all changes made by editors into the description object
	 * 
	 */
	public void save() {
		identification.save();
		context.save();
		content.save();
		access.save();
		relatedMaterials.save();
		notes.save();
		processInfo.save();
	}

	/**
	 * Commit all changes to the descriptive metadata into server
	 * 
	 * @param callback
	 *            Handle called when commit finished. The argument is the
	 *            changed DescriptionObject
	 */
	public void commit(final AsyncCallback<DescriptionObject> callback) {
		final LoadingPopup loading = new LoadingPopup(getParent() == null ? this : getParent());
		loading.show();

		final boolean hierarchyUpdateNeeded = id.isChanged();

		// final boolean infoUpdateNeeded = level.isChanged() ||
		// title.isChanged() || dateInitial.isChanged() ||
		// dateFinal.isChanged();
		final boolean infoUpdateNeeded = true;

		save();

		EditorService.Util.getInstance().saveEdition(descObj, new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				clear(new AsyncCallback<DescriptionObject>() {

					public void onFailure(Throwable caught) {
						loading.hide();
						logger.error("Error clearing descriptive " + "metadata panel", caught);
					}

					public void onSuccess(DescriptionObject obj) {
						loading.hide();

					}

				});

				// TODO catch validation errors
				callback.onFailure(caught);
			}

			public void onSuccess(Void result) {
				update(infoUpdateNeeded, hierarchyUpdateNeeded, new AsyncCallback<CollectionsTreeItem>() {

					public void onFailure(Throwable caught) {
						loading.hide();
						callback.onFailure(caught);
					}

					public void onSuccess(CollectionsTreeItem treeItem) {
						loading.hide();
						callback.onSuccess(descObj);
					}
				});
			}
		});
	}

	private void update(boolean info, boolean hierarchy, final AsyncCallback<CollectionsTreeItem> callback) {
		if (hierarchy) {
			BrowserService.Util.getInstance().getParent(pid, new AsyncCallback<String>() {

				public void onFailure(Throwable caught) {
					logger.error("Error getting " + pid + " parent", caught);
				}

				public void onSuccess(String parentPID) {
					Browse.getInstance().update(parentPID, false, true, new AsyncCallback<CollectionsTreeItem>() {

						public void onFailure(Throwable caught) {
							callback.onFailure(caught);
						}

						public void onSuccess(CollectionsTreeItem treeItem) {
							callback.onSuccess(null);
						}

					});

				}

			});

		} else if (info) {
			Browse.getInstance().update(pid, true, false, callback);
		} else {
			callback.onSuccess(null);
		}
	}

	public void cancel() {
		identification.cancel();
		context.cancel();
		content.cancel();
		access.cancel();
		relatedMaterials.cancel();
		notes.cancel();
		processInfo.cancel();
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	protected void onChange(Widget sender) {
		for (ChangeListener listener : listeners) {
			listener.onChange(sender);
		}
	}

	public void isValid(final AsyncCallback<Boolean> callback) {
		ensureLoaded(new LoadListener() {

			public void onLoadFinished() {
				callback.onSuccess(new Boolean(isValid()));
			}

		});
	}

	public void isChanged(final AsyncCallback<Boolean> callback) {
		ensureLoaded(new LoadListener() {

			public void onLoadFinished() {
				callback.onSuccess(new Boolean(isChanged()));
			}

		});
	}

	private boolean isValid() {
		boolean valid = true;

		valid &= identification.isValid();
		valid &= context.isValid();
		valid &= content.isValid();
		valid &= access.isValid();
		valid &= relatedMaterials.isValid();
		valid &= notes.isValid();
		valid &= processInfo.isValid();

		return valid;
	}

	private boolean isChanged() {
		boolean changed = false;

		changed |= identification.isChanged();
		changed |= context.isChanged();
		changed |= content.isChanged();
		changed |= access.isChanged();
		changed |= relatedMaterials.isChanged();
		changed |= notes.isChanged();
		changed |= processInfo.isChanged();

		return changed;
	}

	/**
	 * Get the description object
	 * 
	 * @return
	 * 
	 * @tip Don't forget to call save before this method to make all used
	 *      editors modify the description object
	 */
	public DescriptionObject getDescriptionObject() {
		return descObj;
	}

	/**
	 * Clear the descriptive panel and re-initialize it
	 * 
	 * @param callback
	 *            handle the finish of the re-initialization
	 */
	public void clear(AsyncCallback<DescriptionObject> callback) {
		layout.clear();
		initialized = false;
		init(callback);
	}

	/**
	 * Clear the descriptive panel and re-initialize it, without callback
	 */
	public void clear() {
		clear(new AsyncCallback<DescriptionObject>() {

			public void onFailure(Throwable caught) {
				// nothing to do
			}

			public void onSuccess(DescriptionObject obj) {
				// nothing to do

			}

		});
	}

}
