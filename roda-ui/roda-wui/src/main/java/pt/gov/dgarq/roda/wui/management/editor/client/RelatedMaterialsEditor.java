/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.Archref;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.P;
import pt.gov.dgarq.roda.core.data.eadc.Relatedmaterial;
import pt.gov.dgarq.roda.core.data.eadc.Relatedmaterials;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class RelatedMaterialsEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final WUIButton addRelatedMaterial;
  private final List<RelatedMaterialEditor> relatedMaterials;
  private final VerticalPanel relatedMaterialsLayout;

  /**
   * Listener for related materials
   * 
   */
  public interface RelatedMaterialListener extends ChangeListener {
    /**
     * Called when an related material is removed
     * 
     * @param sender
     */
    public void onRemove(RelatedMaterialEditor sender);
  }

  public RelatedMaterialsEditor() {
    layout = new DockPanel();
    addRelatedMaterial = new WUIButton(constants.relatedMaterialsAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    relatedMaterialsLayout = new VerticalPanel();

    layout.add(relatedMaterialsLayout, DockPanel.CENTER);
    layout.add(addRelatedMaterial, DockPanel.SOUTH);

    relatedMaterials = new Vector<RelatedMaterialEditor>();
    listeners = new Vector<ChangeListener>();

    addRelatedMaterial.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createRelatedMaterialEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.addStyleName("wui-editor-relatedmaterials");
    relatedMaterialsLayout.addStyleName("wui-editor-relatedmaterials-layout");
    addRelatedMaterial.addStyleName("wui-editor-relatedmaterials-add");
  }

  protected RelatedMaterialEditor createRelatedMaterialEditor() {
    RelatedMaterialEditor editor = new RelatedMaterialEditor();
    editor.addRelatedMaterialListener(new RelatedMaterialListener() {
      public void onRemove(RelatedMaterialEditor sender) {
        relatedMaterials.remove(sender);
        updateLayout();
        RelatedMaterialsEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        RelatedMaterialsEditor.this.onChange(sender);
      }
    });
    relatedMaterials.add(editor);
    return editor;
  }

  protected void updateLayout() {
    relatedMaterialsLayout.clear();
    for (RelatedMaterialEditor relatedMaterial : relatedMaterials) {
      relatedMaterialsLayout.add(relatedMaterial.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<Relatedmaterial> relatedmaterials = new Vector<Relatedmaterial>();
    for (RelatedMaterialEditor relatedMaterialEditor : relatedMaterials) {
      Relatedmaterial relatedmaterial = relatedMaterialEditor.getRelatedMaterial();
      if (relatedmaterial != null) {
        relatedmaterials.add(relatedmaterial);
      }
    }
    return relatedmaterials.size() == 0 ? null : new Relatedmaterials(
      relatedmaterials.toArray(new Relatedmaterial[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof Relatedmaterials) {
      Relatedmaterials relatedmaterials = (Relatedmaterials) value;
      relatedMaterials.clear();
      for (int i = 0; i < relatedmaterials.getRelatedmaterials().length; i++) {
        Relatedmaterial relatedmaterial = relatedmaterials.getRelatedmaterials()[i];
        RelatedMaterialEditor relatedMaterialEditor = createRelatedMaterialEditor();
        relatedMaterialEditor.setRelatedMateral(relatedmaterial);
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (relatedMaterials.size() == 0);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (RelatedMaterialEditor relatedMaterialEditor : relatedMaterials) {
      Relatedmaterial relatedmaterial = relatedMaterialEditor.getRelatedMaterial();
      if (relatedmaterial == null) {
        valid = false;
      }
    }
    return valid;
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    VerticalPanel panel = new VerticalPanel();
    Relatedmaterials relatedmaterials = (Relatedmaterials) value;
    for (int i = 0; i < relatedmaterials.getRelatedmaterials().length; i++) {
      Relatedmaterial relatedmaterial = relatedmaterials.getRelatedmaterials()[i];
      P p = relatedmaterial.getP();
      Archref archref = relatedmaterial.getArchref();
      Label label = new Label(constants.relatedMaterialsTitle());
      label.addStyleName("wui-editor-relatedmaterials-readonly-label");
      panel.add(label);
      if (p != null) {
        FlexTable description = new FlexTable();
        addToReadonlyWidget(constants.relatedMaterialsDescription(), p.getText(), description, 0);
        panel.add(description);
        description.addStyleName("wui-editor-relatedmaterials-readonly-description");
      }
      if (archref != null) {
        Widget archrefWidget = ArchrefEditor.getReadonlyWidget(archref);
        panel.add(archrefWidget);
        archrefWidget.addStyleName("wui-editor-relatedmaterials-readonly-archref");
      }
    }
    panel.addStyleName("wui-editor-relatedmaterials-readonly");
    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-relatedmaterials-readonly-column0");
      column1.addStyleName("wui-editor-relatedmaterials-readonly-column1");
      row++;
    }
    return row;
  }

  /**
   * Editor for related material
   * 
   */
  public class RelatedMaterialEditor {

    private final List<RelatedMaterialListener> listeners;

    private final HorizontalPanel layout;

    private final VerticalPanel subLayout;
    private final HorizontalPanel note;
    private final Label noteLabel;
    private final TextArea noteBox;
    private final ArchrefEditor archrefEditor;
    private final Image remove;

    public RelatedMaterialEditor() {
      layout = new HorizontalPanel();
      subLayout = new VerticalPanel();
      note = new HorizontalPanel();
      noteLabel = new Label(constants.relatedMaterialsDescription());
      noteBox = new TextArea();
      archrefEditor = new ArchrefEditor();

      note.add(noteLabel);
      note.add(noteBox);

      subLayout.add(note);
      subLayout.add(archrefEditor.getWidget());

      remove = commonImageBundle.minus().createImage();

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<RelatedMaterialListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          RelatedMaterialEditor.this.onChange(sender);
        }

      };

      archrefEditor.addChangeListener(changeListener);
      noteBox.addChangeListener(changeListener);

      layout.setCellWidth(subLayout, "100%");
      subLayout.setCellWidth(note, "100%");
      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-relatedmaterial");
      subLayout.addStyleName("wui-editor-relatedmaterial-center");
      remove.addStyleName("wui-editor-relatedmaterial-remove");
      note.addStyleName("wui-editor-relatedmaterial-note");
      note.setCellWidth(noteBox, "100%");
      noteBox.addStyleName("wui-editor-relatedmaterial-note-box");
      noteLabel.addStyleName("wui-editor-relatedmaterial-note-label");
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
     * Set the related material value
     * 
     * @param relatedmaterial
     */
    public void setRelatedMateral(Relatedmaterial relatedmaterial) {
      if (relatedmaterial.getP() != null && relatedmaterial.getP().getText() != null
        && !relatedmaterial.getP().getText().isEmpty()) {
        noteBox.setText(relatedmaterial.getP().getText());
      }
      if (relatedmaterial.getArchref() != null) {
        archrefEditor.setValue(relatedmaterial.getArchref());
      }
    }

    /**
     * Get the related material value
     * 
     * @return
     */
    public Relatedmaterial getRelatedMaterial() {
      Relatedmaterial relatedmaterial = null;
      P p = null;
      Archref archref = null;
      if (!noteBox.getText().isEmpty()) {
        p = new P();
        p.setText(noteBox.getText());
      }
      if (archrefEditor.isValid()) {
        archref = archrefEditor.getValue();
      }
      if (archref != null || p != null) {
        relatedmaterial = new Relatedmaterial();
        if (p != null) {
          relatedmaterial.setP(p);
        }
        if (archref != null) {
          relatedmaterial.setArchref(archref);
        }
      }
      return relatedmaterial;
    }

    /**
     * Add a related material listener
     * 
     * @param listener
     */
    public void addRelatedMaterialListener(RelatedMaterialListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a related material listener
     * 
     * @param listener
     */
    public void removeRelatedMaterialListener(RelatedMaterialListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (RelatedMaterialListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (RelatedMaterialListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
