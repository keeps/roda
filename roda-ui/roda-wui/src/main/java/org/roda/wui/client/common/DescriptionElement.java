/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

import org.roda.core.data.DescriptionObject;
import org.roda.core.data.eadc.Acqinfos;
import org.roda.core.data.eadc.ArrangementTable;
import org.roda.core.data.eadc.BioghistChronitem;
import org.roda.core.data.eadc.BioghistChronlist;
import org.roda.core.data.eadc.ControlAccesses;
import org.roda.core.data.eadc.DescriptionLevel;
import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Index;
import org.roda.core.data.eadc.LangmaterialLanguages;
import org.roda.core.data.eadc.Materialspecs;
import org.roda.core.data.eadc.Notes;
import org.roda.core.data.eadc.PhysdescElement;
import org.roda.core.data.eadc.PhysdescGenreform;
import org.roda.core.data.eadc.ProcessInfo;
import org.roda.core.data.eadc.Relatedmaterials;
import org.roda.core.data.eadc.Text;
import org.roda.wui.client.browse.ArrangementTableGroupPanel;
import org.roda.wui.client.images.EditorsIconBundle;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.management.editor.client.AcqInfosEditor;
import org.roda.wui.management.editor.client.ArrangementTableEditor;
import org.roda.wui.management.editor.client.ChronListEditor;
import org.roda.wui.management.editor.client.ControlAccessesEditor;
import org.roda.wui.management.editor.client.CountryEditor;
import org.roda.wui.management.editor.client.DateEditor;
import org.roda.wui.management.editor.client.ElementLevelEditor;
import org.roda.wui.management.editor.client.KeywordsEditor;
import org.roda.wui.management.editor.client.LanguageEditor;
import org.roda.wui.management.editor.client.MaterialSpecsEditor;
import org.roda.wui.management.editor.client.MetadataElementEditor;
import org.roda.wui.management.editor.client.NotesEditor;
import org.roda.wui.management.editor.client.PhysDescElementEditor;
import org.roda.wui.management.editor.client.PhysDescGenreformEditor;
import org.roda.wui.management.editor.client.ProcessInfoEditor;
import org.roda.wui.management.editor.client.RelatedMaterialsEditor;
import org.roda.wui.management.editor.client.TextEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationConstants;

/**
 * @author Luis Faria
 * 
 */
public class DescriptionElement implements SourcesChangeEvents {

  public static enum EditMode {
    TEXT_LINE, TEXT_AREA, TEXT_BIGAREA, DATE, COUNTRYCODE, ARRANGEMENT_TABLE, CHRON_LIST, LANGUAGES_LIST,
    PHYSDESC_DIMENSIONS, PHYSDESC_GENREFORM, PHYSDESC_PHYSFACET, PHYSDESC_EXTENT, LEVEL, NOTES, PROCESS_INFO,
    CONTROL_ACCESSES, MATERIAL_SPECS, RELATED_MATERIALS, ACQUISITIONS_INFOS, KEYWORDS
  }

  private static DisseminationConstants constants = (DisseminationConstants) GWT.create(DisseminationConstants.class);

  private static EditorsIconBundle editorsIconBundle = (EditorsIconBundle) GWT.create(EditorsIconBundle.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private final DescriptionObject object;

  private final String element;

  private final String description;

  private final SimplePanel body;

  private boolean readonly;

  private final boolean valueAsHTML;

  private Widget readonlyWidget;

  private Widget editWidget;

  private EadCValue value;

  private EditMode[] editModes;

  private final List<ChangeListener> listeners;

  private boolean required;

  private List<MetadataElementEditor> editors;

  public DescriptionElement(DescriptionObject object, String element, String description, boolean required,
    boolean valueAsHTML) {
    this(object, element, description, new EditMode[] {}, required, valueAsHTML);
  }

  public DescriptionElement(DescriptionObject object, String element, String description, EditMode[] editModes,
    boolean required, boolean valueAsHTML) {
    readonly = true;
    this.object = object;
    this.element = element;
    this.description = description;
    this.editModes = editModes;
    this.required = required;
    this.valueAsHTML = valueAsHTML;
    body = new SimplePanel();
    value = getValue();
    readonlyWidget = createReadonlyWidget();
    editWidget = null;
    listeners = new Vector<ChangeListener>();
    body.setWidget(readonlyWidget);
    body.addStyleName("descriptionElement");
    editors = new ArrayList<MetadataElementEditor>();
  }

  public void addEditMode(EditMode mode) {
    Vector<EditMode> editModes = new Vector<EditMode>(Arrays.asList(this.editModes));
    editModes.add(mode);
    this.editModes = (EditMode[]) editModes.toArray(new EditMode[] {});

  }

  /**
   * If the remote value is set
   * 
   * @return true if it is
   */
  public boolean isSet() {
    return getValue() != null;
  }

  private EadCValue getValue() {
    // FIXME
    // return object.getValue(element);
    return null;
  }

  private void setValue(EadCValue value) {
    object.setValue(element, value);
  }

  public void save() {
    if (isChanged()) {
      setValue(value);
      readonlyWidget = null;
    }
  }

  public void cancel() {
    if (isChanged()) {
      this.value = getValue();
      readonlyWidget = createReadonlyWidget();
      editWidget = createEditWidget();
      body.setWidget(readonly ? readonlyWidget : editWidget);
    }
  }

  private Widget createEditWidget() {
    Widget ret = null;
    ChangeListener editorListener = new ChangeListener() {

      public void onChange(Widget sender) {
        DescriptionElement.this.onChange(sender);
      }

    };
    if (editModes.length == 0) {
      ret = createReadonlyWidget();
    } else if (editModes.length == 1) {
      MetadataElementEditor editor = createEditWidgetMode(editModes[0]);
      editor.addChangeListener(editorListener);
      ret = editor.getWidget();
      ret.addStyleName("edit-widget-mode");
      editors.add(editor);
    } else if (editModes.length > 1) {
      TabPanel tabs = new TabPanel();
      for (int i = 0; i < editModes.length; i++) {
        MetadataElementEditor editor = createEditWidgetMode(editModes[i]);
        tabs.add(editor.getWidget(), new Image(createEditorModeIcon(editModes[i])));
        if (!editor.isEmpty()) {
          tabs.selectTab(i);
        }
        editor.addChangeListener(editorListener);
        editor.getWidget().addStyleName("edit-widget-mode");
        editors.add(editor);
      }
      ret = tabs;
      ret.addStyleName("edit-widget-tabs");
    }
    if (ret != null) {
      ret.addStyleName("edit-widget");
    }
    return ret;
  }

  private MetadataElementEditor createEditWidgetMode(EditMode mode) {
    MetadataElementEditor ret = null;
    if (mode.equals(EditMode.TEXT_LINE)) {
      ret = new TextEditor(TextEditor.Size.LINE);
    } else if (mode.equals(EditMode.TEXT_AREA)) {
      ret = new TextEditor(TextEditor.Size.AREA);
    } else if (mode.equals(EditMode.TEXT_BIGAREA)) {
      ret = new TextEditor(TextEditor.Size.BIGAREA);
    } else if (mode.equals(EditMode.DATE)) {
      ret = new DateEditor();
    } else if (mode.equals(EditMode.COUNTRYCODE)) {
      ret = new CountryEditor();
    } else if (mode.equals(EditMode.PHYSDESC_DIMENSIONS)) {
      ret = new PhysDescElementEditor(DescriptionObject.PHYSDESC_DIMENSIONS);
    } else if (mode.equals(EditMode.PHYSDESC_PHYSFACET)) {
      ret = new PhysDescElementEditor(DescriptionObject.PHYSDESC_PHYSFACET);
    } else if (mode.equals(EditMode.PHYSDESC_EXTENT)) {
      ret = new PhysDescElementEditor(DescriptionObject.PHYSDESC_EXTENT);
    } else if (mode.equals(EditMode.LANGUAGES_LIST)) {
      ret = new LanguageEditor();
    } else if (mode.equals(EditMode.CHRON_LIST)) {
      ret = new ChronListEditor();
    } else if (mode.equals(EditMode.ARRANGEMENT_TABLE)) {
      ret = new ArrangementTableEditor();
    } else if (mode.equals(EditMode.LEVEL)) {
      ret = new ElementLevelEditor(object.getId());
    } else if (mode.equals(EditMode.NOTES)) {
      ret = new NotesEditor();
    } else if (mode.equals(EditMode.PHYSDESC_GENREFORM)) {
      ret = new PhysDescGenreformEditor();
    } else if (mode.equals(EditMode.PROCESS_INFO)) {
      ret = new ProcessInfoEditor();
    } else if (mode.equals(EditMode.CONTROL_ACCESSES)) {
      ret = new ControlAccessesEditor();
    } else if (mode.equals(EditMode.MATERIAL_SPECS)) {
      ret = new MaterialSpecsEditor();
    } else if (mode.equals(EditMode.RELATED_MATERIALS)) {
      ret = new RelatedMaterialsEditor();
    } else if (mode.equals(EditMode.ACQUISITIONS_INFOS)) {
      ret = new AcqInfosEditor();
    } else if (mode.equals(EditMode.KEYWORDS)) {
      ret = new KeywordsEditor();
    }

    if (ret != null) {
      final MetadataElementEditor editor = ret;
      editor.setValue(value);
      editor.addChangeListener(new ChangeListener() {

        public void onChange(Widget sender) {
          value = editor.getValue();
        }

      });
    }

    return ret;
  }

  /**
   * Create widget used to show the information in a non-editable way
   * 
   * @param readonly
   * @param type
   * @return
   */
  private Widget createReadonlyWidget() {
    Widget ret = null;
    if (value != null) {
      if (value instanceof Text) {
        Text text = (Text) value;
        if (valueAsHTML) {
          ret = new HTML(text.getText());
        } else {
          ret = new Label(text.getText());
        }
      } else if (value instanceof DescriptionLevel) {
        ret = new Label(DescriptionLevelUtils.getElementLevelTranslation((DescriptionLevel) value));
      } else if (value instanceof ArrangementTable) {
        ArrangementTable arrangementTable = (ArrangementTable) value;
        VerticalPanel arrangementTables = new VerticalPanel();
        for (int i = 0; i < arrangementTable.getArrangementTableGroups().length; i++) {
          arrangementTables.add(new ArrangementTableGroupPanel(arrangementTable.getArrangementTableGroups()[i]));
        }
        ret = arrangementTables;
      } else if (value instanceof BioghistChronlist) {
        BioghistChronlist chronlist = (BioghistChronlist) value;
        BioghistChronitem[] chronitems = chronlist.getBioghistChronitems();
        FlexTable bioghistChronLayout = new FlexTable();
        for (int i = 0; i < chronitems.length; i++) {
          Label dateInitial = new Label(chronitems[i].getDateInitial());
          Label dateFinal = new Label(chronitems[i].getDateFinal());
          Label text = new Label(chronitems[i].getEvent());

          bioghistChronLayout.setWidget(i, 0, dateInitial);
          bioghistChronLayout.setWidget(i, 1, dateFinal);
          bioghistChronLayout.setWidget(i, 2, text);
        }
        ret = bioghistChronLayout;
      } else if (value instanceof LangmaterialLanguages) {
        LangmaterialLanguages languages = (LangmaterialLanguages) value;
        VerticalPanel languagesLayout = new VerticalPanel();
        for (int i = 0; i < languages.getLangmaterialLanguages().length; i++) {
          String langCode = languages.getLangmaterialLanguages()[i];
          Label language;
          try {
            language = new Label(constants.getString("lang_" + langCode));
          } catch (MissingResourceException e) {
            language = new Label(langCode);
          }
          languagesLayout.add(language);
        }
        ret = languagesLayout;
      } else if (value instanceof PhysdescElement) {
        PhysdescElement physdescElement = (PhysdescElement) value;
        Label physdescElementWidget = new Label(
          physdescElement.getValue() + " " + (physdescElement.getUnit() != null ? physdescElement.getUnit() : ""));
        ret = physdescElementWidget;
      } else if (value instanceof PhysdescGenreform) {
        ret = PhysDescGenreformEditor.getReadonlyWidget(value);
      } else if (value instanceof Materialspecs) {
        ret = MaterialSpecsEditor.getReadonlyWidget(value);
      } else if (value instanceof Notes) {
        ret = NotesEditor.getReadonlyWidget(value);
      } else if (value instanceof Index) {
        ret = KeywordsEditor.getReadonlyWidget(value);
      } else if (value instanceof ControlAccesses) {
        ret = ControlAccessesEditor.getReadonlyWidget(value);
      } else if (value instanceof Acqinfos) {
        ret = AcqInfosEditor.getReadonlyWidget(value);
      } else if (value instanceof Relatedmaterials) {
        ret = RelatedMaterialsEditor.getReadonlyWidget(value);
      } else if (value instanceof ProcessInfo) {
        ret = ProcessInfoEditor.getReadonlyWidget(value);
      } else {
        logger.error("Unsupported value type " + value.getClass().getName());
      }
    }
    if (ret != null) {
      ret.addStyleName("readonly-widget");
    }
    return ret;
  }

  public boolean isReadonly() {
    return readonly;
  }

  public void setReadonly(boolean readonly) {
    if (this.readonly != readonly) {
      this.readonly = readonly;
      if (readonly) {
        if (isChanged() || readonlyWidget == null) {
          readonlyWidget = createReadonlyWidget();
        }
        body.setWidget(readonlyWidget);
      } else {
        if (editWidget == null) {
          editWidget = createEditWidget();
        }
        body.setWidget(editWidget);
      }
    }
  }

  public boolean isChanged() {
    boolean ret;
    EadCValue original = getValue();
    if (original == null || value == null) {
      ret = original != value;
    } else {
      ret = !original.equals(value);
    }
    return ret;
  }

  public SimplePanel getBody() {
    return body;
  }

  public String getDescription() {
    return description;
  }

  public boolean isValueAsHTML() {
    return valueAsHTML;
  }

  public ImageResource createEditorModeIcon(EditMode mode) {
    ImageResource ret = null;
    if (mode.equals(EditMode.TEXT_LINE) || mode.equals(EditMode.TEXT_AREA) || mode.equals(EditMode.TEXT_BIGAREA)) {
      ret = editorsIconBundle.editorText();
    } else if (mode.equals(EditMode.DATE)) {
      ret = editorsIconBundle.editorDate();
    } else if (mode.equals(EditMode.CHRON_LIST)) {
      ret = editorsIconBundle.editorChronList();
    } else if (mode.equals(EditMode.LANGUAGES_LIST)) {
      ret = editorsIconBundle.editorLanguagesList();
    } else if (mode.equals(EditMode.ARRANGEMENT_TABLE)) {
      ret = editorsIconBundle.editorArrangementTable();
    } else {
      ret = editorsIconBundle.editorOther();
    }
    return ret;
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

  /**
   * Check if the value of the description element is valid
   * 
   * @return
   */
  public boolean isValid() {
    boolean valid = (value != null) || !required;
    if (value != null) {
      for (MetadataElementEditor editor : editors) {
        valid = valid && editor.isValid();
      }
    }
    if (!valid) {
      if (editWidget != null) {
        editWidget.addStyleName("unvalid");
      }
      logger.debug("Element " + description + " is not valid");
    } else {
      if (editWidget != null) {
        editWidget.removeStyleName("unvalid");
      }
    }
    return valid;
  }

  /**
   * Is the description element obligatory / required
   * 
   * @return
   */
  public boolean isRequired() {
    return required;
  }

}
