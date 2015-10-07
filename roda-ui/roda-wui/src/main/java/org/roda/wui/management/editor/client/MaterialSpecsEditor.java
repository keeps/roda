/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Materialspec;
import org.roda.core.data.eadc.Materialspecs;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.WUIButton;

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
public class MaterialSpecsEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final WUIButton addMaterialSpec;
  private final List<MaterialSpecEditor> materialSpecs;
  private final VerticalPanel materialSpecsLayout;

  /**
   * Listener for material specs
   * 
   */
  public interface MaterialSpecListener extends ChangeListener {
    /**
     * Called when an material spec is removed
     * 
     * @param sender
     */
    public void onRemove(MaterialSpecEditor sender);
  }

  public MaterialSpecsEditor() {
    layout = new DockPanel();
    addMaterialSpec = new WUIButton(constants.materialSpecsAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    materialSpecsLayout = new VerticalPanel();

    layout.add(materialSpecsLayout, DockPanel.CENTER);
    layout.add(addMaterialSpec, DockPanel.SOUTH);

    materialSpecs = new Vector<MaterialSpecEditor>();
    listeners = new Vector<ChangeListener>();

    addMaterialSpec.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createMaterialSpecEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.setCellWidth(materialSpecsLayout, "100%");
    layout.addStyleName("wui-editor-materialspecs");
    materialSpecsLayout.addStyleName("wui-editor-materialspecs-layout");
    addMaterialSpec.addStyleName("wui-editor-materialspecs-add");
  }

  protected MaterialSpecEditor createMaterialSpecEditor() {
    MaterialSpecEditor editor = new MaterialSpecEditor();
    editor.addMaterialSpecListener(new MaterialSpecListener() {

      public void onRemove(MaterialSpecEditor sender) {
        materialSpecs.remove(sender);
        updateLayout();
        MaterialSpecsEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        MaterialSpecsEditor.this.onChange(sender);
      }

    });
    materialSpecs.add(editor);
    return editor;
  }

  protected void updateLayout() {
    materialSpecsLayout.clear();
    for (MaterialSpecEditor materialSpec : materialSpecs) {
      materialSpecsLayout.add(materialSpec.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<Materialspec> materialspecs = new Vector<Materialspec>();
    for (MaterialSpecEditor materialSpecEditor : materialSpecs) {
      Materialspec materialspec = materialSpecEditor.getMaterialSpec();
      if (materialspec != null) {
        materialspecs.add(materialspec);
      }
    }
    return materialspecs.size() == 0 ? null : new Materialspecs(materialspecs.toArray(new Materialspec[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof Materialspecs) {
      Materialspecs materialspecs = (Materialspecs) value;
      materialSpecs.clear();
      for (int i = 0; i < materialspecs.getMaterialspecs().length; i++) {
        Materialspec materialspec = materialspecs.getMaterialspecs()[i];
        MaterialSpecEditor materialSpecEditor = createMaterialSpecEditor();
        materialSpecEditor.setMaterialSpec(materialspec);
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (materialSpecs.size() == 0);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (MaterialSpecEditor materialSpecEditor : materialSpecs) {
      Materialspec materialspec = materialSpecEditor.getMaterialSpec();
      if (materialspec == null) {
        valid = false;
      }
    }
    return valid;
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    FlexTable panel = new FlexTable();
    Materialspecs materialspecs = (Materialspecs) value;
    for (int i = 0; i < materialspecs.getMaterialspecs().length; i++) {
      Materialspec materialspec = materialspecs.getMaterialspecs()[i];
      Label column0 = new Label(materialspec.getAttributeLabel());
      Label column1 = new Label(materialspec.getText());
      panel.setWidget(i, 0, column0);
      panel.setWidget(i, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-genreform-readonly-column0");
      column1.addStyleName("wui-editor-genreform-readonly-column1");
    }
    panel.addStyleName("wui-editor-materialspecs-readonly");
    return panel;
  }

  /**
   * Editor for material spec
   * 
   */
  public class MaterialSpecEditor {

    private final List<MaterialSpecListener> listeners;

    private final HorizontalPanel layout;

    private final HorizontalPanel subLayout;
    private final TextBox label;
    private final TextBox value;
    private final Image remove;

    /**
     * Variables to controll the default box texts
     */
    private boolean labelNormalTextMode = false;
    private boolean valueNormalTextMode = false;

    public MaterialSpecEditor() {
      layout = new HorizontalPanel();
      subLayout = new HorizontalPanel();

      label = new TextBox();
      value = new TextBox();

      remove = commonImageBundle.minus().createImage();

      subLayout.add(label);
      subLayout.add(value);

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<MaterialSpecListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          MaterialSpecEditor.this.onChange(sender);
        }

      };

      label.setText(constants.materialSpecsLabel());
      value.setText(constants.materialSpecsValue());

      label.addChangeListener(changeListener);
      value.addChangeListener(changeListener);

      label.addFocusHandler(new FocusHandler() {

        public void onFocus(FocusEvent event) {
          if (!labelNormalTextMode) {
            labelNormalTextMode = true;
            label.setText("");
            label.removeStyleName("empty");
          }
        }
      });

      label.addBlurHandler(new BlurHandler() {

        public void onBlur(BlurEvent event) {
          if (label.getText().isEmpty()) {
            labelNormalTextMode = false;
            label.setText(constants.materialSpecsLabel());
            label.addStyleName("empty");
          }
        }
      });

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
            value.setText(constants.materialSpecsValue());
            value.addStyleName("empty");
          }
        }
      });

      layout.setCellWidth(subLayout, "100%");
      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-materialspec");
      subLayout.addStyleName("wui-editor-materialspec-center");
      remove.addStyleName("wui-editor-materialspec-remove");
      label.addStyleName("wui-editor-materialspec-label");
      value.addStyleName("wui-editor-materialspec-value");
      label.addStyleName("empty");
      value.addStyleName("empty");
      subLayout.setCellWidth(value, "100%");
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
     * Set the material spec value
     * 
     * @param materialspec
     */
    public void setMaterialSpec(Materialspec materialspec) {
      if (materialspec.getAttributeLabel() != null) {
        label.setText(materialspec.getAttributeLabel());
        labelNormalTextMode = true;
        label.removeStyleName("empty");
      }
      if (materialspec.getText() != null) {
        value.setText(materialspec.getText());
        valueNormalTextMode = true;
        value.removeStyleName("empty");
      }
    }

    /**
     * Get the material spec value
     * 
     * @return
     */
    public Materialspec getMaterialSpec() {
      Materialspec materialspec = null;
      if (label.getText().isEmpty()) {
        labelNormalTextMode = false;
      }
      if (value.getText().isEmpty()) {
        valueNormalTextMode = false;
      }
      if (labelNormalTextMode && valueNormalTextMode) {
        materialspec = new Materialspec(value.getText(), label.getText());
      }
      return materialspec;
    }

    /**
     * Add a material spec listener
     * 
     * @param listener
     */
    public void addMaterialSpecListener(MaterialSpecListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a material spec listener
     * 
     * @param listener
     */
    public void removeMaterialSpecListener(MaterialSpecListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (MaterialSpecListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (MaterialSpecListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
