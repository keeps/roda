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
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.Archref;
import org.roda.core.data.eadc.Note;
import org.roda.core.data.eadc.P;
import org.roda.core.data.eadc.Unitid;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.IDTypePicker;
import org.roda.wui.common.client.widgets.WUIButton;
import org.roda.wui.common.client.widgets.IDTypePicker.IDType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class ArchrefEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final WUIButton addUnityId;
  private final List<UnitidEditor> unitIds;
  private final VerticalPanel unitidsLayout;
  private final HorizontalPanel note;
  private final Label noteLabel;
  private final TextBox noteBox;

  private static String ATTRIBUTE_ALTRENDER_SIMPLE_ID = "id";
  private static String ATTRIBUTE_ALTRENDER_FULL_ID = "full_id";
  private static String ATTRIBUTE_ALTRENDER_NOTE = "other_binding_info";

  /**
   * Listener for unit ids
   * 
   */
  public interface UnitidListener extends ChangeListener {
    /**
     * Called when an unit id is removed
     * 
     * @param sender
     */
    public void onRemove(UnitidEditor sender);
  }

  public ArchrefEditor() {
    layout = new DockPanel();
    addUnityId = new WUIButton(constants.archrefAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    unitidsLayout = new VerticalPanel();

    note = new HorizontalPanel();
    noteLabel = new Label(constants.archrefNote());
    noteBox = new TextBox();

    note.add(noteLabel);
    note.add(noteBox);
    note.setVisible(false);

    layout.add(unitidsLayout, DockPanel.CENTER);
    layout.add(note, DockPanel.SOUTH);
    layout.add(addUnityId, DockPanel.SOUTH);

    unitIds = new Vector<UnitidEditor>();
    listeners = new Vector<ChangeListener>();

    addUnityId.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createUnitidEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.setCellWidth(unitidsLayout, "100%");
    layout.addStyleName("wui-editor-archref");
    unitidsLayout.addStyleName("wui-editor-archref-layout");
    addUnityId.addStyleName("wui-editor-archref-add");
    note.addStyleName("wui-editor-archref-note");
    note.setCellWidth(noteBox, "100%");
    noteLabel.addStyleName("wui-editor-archref-note-label");
    noteBox.addStyleName("wui-editor-archref-note-box");
  }

  protected UnitidEditor createUnitidEditor() {
    UnitidEditor editor = new UnitidEditor();
    editor.addUnitidListener(new UnitidListener() {
      public void onRemove(UnitidEditor sender) {
        unitIds.remove(sender);
        updateLayout();
        ArchrefEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        ArchrefEditor.this.onChange(sender);
      }
    });
    unitIds.add(editor);
    return editor;
  }

  protected void updateLayout() {
    unitidsLayout.clear();
    for (UnitidEditor unitidEditor : unitIds) {
      unitidsLayout.add(unitidEditor.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public Archref getValue() {
    Archref archref = null;
    List<Unitid> unitids = new Vector<Unitid>();
    for (UnitidEditor unitidEditor : unitIds) {
      Unitid unitid = unitidEditor.getUnitid();
      if (unitid != null) {
        unitids.add(unitid);
      }
    }
    if (unitids.size() != 0) {
      archref = new Archref(unitids.toArray(new Unitid[] {}), "", null);
      if (noteBox.isVisible() && !noteBox.getText().isEmpty()) {
        archref.setNote(new Note(new P(noteBox.getText())));
        archref.getNote().setAttributeAltrender(ATTRIBUTE_ALTRENDER_NOTE);
      }
    }
    return archref;
  }

  public void setValue(Archref value) {
    Archref archref = (Archref) value;
    unitIds.clear();
    for (int i = 0; i < archref.getUnitids().length; i++) {
      Unitid unitid = archref.getUnitids()[i];
      UnitidEditor unitidEditor = createUnitidEditor();
      unitidEditor.setUnitid(unitid);
    }
    Note note = archref.getNote();
    if (note != null && note.getP() != null && note.getP().getText() != null) {
      noteBox.setText(note.getP().getText());
    }
    updateLayout();
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (unitIds.size() == 0);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (UnitidEditor unitidEditor : unitIds) {
      Unitid unitid = unitidEditor.getUnitid();
      if (unitid == null) {
        valid = false;
      }
    }
    return valid;
  }

  public void setNoteVisible() {
    note.setVisible(true);
  }

  public static Widget getReadonlyWidget(Archref value) {
    VerticalPanel panel = new VerticalPanel();
    Archref archref = (Archref) value;
    if (archref.getUnitids() != null && archref.getUnitids().length > 0) {
      Label label = new Label(constants.archrefTitle());
      FlexTable sectionPanel = new FlexTable();
      int row = 0;
      for (int i = 0; i < archref.getUnitids().length; i++) {
        Unitid unitid = archref.getUnitids()[i];
        row = addToReadonlyWidget(IDTypePicker.getIDTypeLabel(unitid.getAttributeAltrender()), unitid.getText(),
          sectionPanel, row);
      }
      panel.add(label);
      panel.add(sectionPanel);
      label.addStyleName("wui-editor-archref-readonly-label");
      sectionPanel.addStyleName("wui-editor-archref-readonly-unitid");
    }
    Note note = archref.getNote();
    if (note != null && note.getP() != null && note.getP().getText() != null) {
      FlexTable sectionPanel = new FlexTable();
      addToReadonlyWidget(constants.archrefNote(), note.getP().getText(), sectionPanel, 0);
      panel.add(sectionPanel);
      sectionPanel.addStyleName("wui-editor-archref-readonly-note");
    }
    panel.addStyleName("wui-editor-archref-readonly");
    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-archref-readonly-column0");
      column1.addStyleName("wui-editor-archref-readonly-column1");
      row++;
    }
    return row;
  }

  /**
   * Editor for unit id
   * 
   */
  public class UnitidEditor {

    private final List<UnitidListener> listeners;

    private final HorizontalPanel layout;

    private final HorizontalPanel subLayout;
    private final IDTypePicker idtype;
    private final TextBox value;
    private final Image remove;

    /**
     * Variable to controll the default box texts
     */
    private boolean valueNormalTextMode = false;

    public UnitidEditor() {
      layout = new HorizontalPanel();
      subLayout = new HorizontalPanel();

      idtype = new IDTypePicker();
      value = new TextBox();

      remove = commonImageBundle.minus().createImage();

      subLayout.add(idtype);
      subLayout.add(value);

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<UnitidListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          UnitidEditor.this.onChange(sender);
        }

      };

      idtype.addChangeListener(changeListener);
      value.addChangeListener(changeListener);

      value.setText(constants.archrefValue());

      value.addFocusHandler(new FocusHandler() {

        public void onFocus(FocusEvent event) {
          if (!valueNormalTextMode) {
            valueNormalTextMode = true;
            value.setText("");
            value.removeStyleName("empty");
          }
        }
      });

      value.addBlurHandler(new BlurHandler() {

        public void onBlur(BlurEvent event) {
          if (value.getText().isEmpty()) {
            valueNormalTextMode = false;
            value.setText(constants.archrefValue());
            value.addStyleName("empty");
          }
        }
      });

      layout.setCellWidth(subLayout, "100%");
      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-unitid");
      subLayout.addStyleName("wui-editor-unitid-center");
      remove.addStyleName("wui-editor-unitid-remove");
      idtype.addStyleName("wui-editor-unitid-label");
      value.addStyleName("wui-editor-unitid-value");
      subLayout.setCellWidth(value, "100%");
      value.addStyleName("empty");
    }

    /**
     * Get editor widget
     * 
     * @return
     */
    public Widget getWidget() {
      return layout;
    }

    /**
     * Set the unit id value
     * 
     * @param unitid
     */
    public void setUnitid(Unitid unitid) {
      if (unitid.getAttributeAltrender() != null && unitid.getAttributeAltrender().equals(ATTRIBUTE_ALTRENDER_FULL_ID)) {
        idtype.setSelectedIndex(1);
      } else {
        idtype.setSelectedIndex(0);
      }
      if (unitid.getText() != null && !unitid.getText().isEmpty()) {
        value.setText(unitid.getText());
        valueNormalTextMode = true;
        value.removeStyleName("empty");
      }
    }

    /**
     * Get the unit id value
     * 
     * @return
     */
    public Unitid getUnitid() {
      Unitid unitid = null;
      if (!value.getText().isEmpty() && valueNormalTextMode) {
        unitid = new Unitid();
        if (idtype.getSelectedIDType().equals(IDType.SIMPLE_ID)) {
          unitid.setAttributeAltrender(ATTRIBUTE_ALTRENDER_SIMPLE_ID);
        } else if (idtype.getSelectedIDType().equals(IDType.FULL_ID)) {
          unitid.setAttributeAltrender(ATTRIBUTE_ALTRENDER_FULL_ID);
        }
        unitid.setText(value.getText());
      }
      return unitid;
    }

    /**
     * Add a unit id listener
     * 
     * @param listener
     */
    public void addUnitidListener(UnitidListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a unit id listener
     * 
     * @param listener
     */
    public void removeUnitidListener(UnitidListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (UnitidListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (UnitidListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
