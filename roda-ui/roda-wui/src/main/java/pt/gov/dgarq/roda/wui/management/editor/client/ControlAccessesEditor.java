/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.ControlAccess;
import pt.gov.dgarq.roda.core.data.eadc.ControlAccesses;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class ControlAccessesEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final WUIButton addControlAccess;
  private final List<ControlAccessEditor> controlAccesses;
  private final VerticalPanel controlAccessesLayout;

  /**
   * Listener for control accesses
   * 
   */
  public interface ControlAccessListener extends ChangeListener {
    /**
     * Called when an control access is removed
     * 
     * @param sender
     */
    public void onRemove(ControlAccessEditor sender);
  }

  public ControlAccessesEditor() {
    layout = new DockPanel();
    addControlAccess = new WUIButton(constants.controlAccessesAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    controlAccessesLayout = new VerticalPanel();

    layout.add(controlAccessesLayout, DockPanel.CENTER);
    layout.add(addControlAccess, DockPanel.SOUTH);

    controlAccesses = new Vector<ControlAccessEditor>();
    listeners = new Vector<ChangeListener>();

    addControlAccess.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createControlAccessEditor();
        updateLayout();
        onChange(layout);
      }
    });

    layout.setCellWidth(controlAccessesLayout, "100%");
    layout.addStyleName("wui-editor-controlaccesses");
    controlAccessesLayout.addStyleName("wui-editor-controlaccesses-layout");
    addControlAccess.addStyleName("wui-editor-controlaccesses-add");
  }

  protected ControlAccessEditor createControlAccessEditor() {
    ControlAccessEditor editor = new ControlAccessEditor();
    editor.addControlAccessListener(new ControlAccessListener() {

      public void onRemove(ControlAccessEditor sender) {
        controlAccesses.remove(sender);
        updateLayout();
        ControlAccessesEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        ControlAccessesEditor.this.onChange(sender);
      }

    });
    controlAccesses.add(editor);
    return editor;
  }

  protected void updateLayout() {
    controlAccessesLayout.clear();
    for (ControlAccessEditor controlAccess : controlAccesses) {
      controlAccessesLayout.add(controlAccess.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<ControlAccess> controlaccesses = new Vector<ControlAccess>();
    for (ControlAccessEditor controlAccessEditor : controlAccesses) {
      ControlAccess controlaccess = controlAccessEditor.getControlAccess();
      if (controlaccess != null) {
        controlaccesses.add(controlaccess);
      }
    }
    return controlaccesses.size() == 0 ? null : new ControlAccesses(controlaccesses.toArray(new ControlAccess[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof ControlAccesses) {
      ControlAccesses controlaccesses = (ControlAccesses) value;
      controlAccesses.clear();
      for (int i = 0; i < controlaccesses.getControlaccesses().length; i++) {
        ControlAccess controlaccess = controlaccesses.getControlaccesses()[i];
        ControlAccessEditor controlAccessEditor = createControlAccessEditor();
        controlAccessEditor.setControlAccess(controlaccess);
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (controlAccesses.size() == 0);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (ControlAccessEditor controlAccessEditor : controlAccesses) {
      ControlAccess controlaccess = controlAccessEditor.getControlAccess();
      if (controlaccess == null) {
        valid = false;
        controlAccessEditor.getWidget().addStyleName("unvalid");
      } else {
        controlAccessEditor.getWidget().removeStyleName("unvalid");
      }
    }
    return valid;
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    VerticalPanel panel = new VerticalPanel();
    ControlAccesses controlaccesses = (ControlAccesses) value;
    for (int i = 0; i < controlaccesses.getControlaccesses().length; i++) {
      ControlAccess controlaccess = controlaccesses.getControlaccesses()[i];
      Label sectionLabel = new Label(constants.controlAccessesTitle());
      FlexTable sectionPanel = new FlexTable();
      int row = 0;
      row = addToReadonlyWidget(constants.controlAccessesID(), controlaccess.getAttributeEncodinganalog(),
        sectionPanel, row);
      row = addToReadonlyWidget(constants.controlAccessesName(), controlaccess.getHead(), sectionPanel, row);
      row = addToReadonlyWidget(constants.controlAccessesDescription(), controlaccess.getSubject(), sectionPanel, row);
      row = addToReadonlyWidget(constants.controlAccessesLevel(), controlaccess.getFunction(), sectionPanel, row);
      row = addToReadonlyWidget(constants.controlAccessesReason(), controlaccess.getP(), sectionPanel, row);
      panel.add(sectionLabel);
      panel.add(sectionPanel);
      sectionLabel.addStyleName("wui-editor-controlaccesses-readonly-label");
      sectionPanel.addStyleName("wui-editor-controlaccesses-readonly-controlaccess");
    }
    panel.addStyleName("wui-editor-controlaccesses-readonly");
    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-controlaccesses-readonly-column0");
      column1.addStyleName("wui-editor-controlaccesses-readonly-column1");
      row++;
    }
    return row;
  }

  /**
   * Editor for control access
   * 
   */
  public class ControlAccessEditor {

    private final List<ControlAccessListener> listeners;

    private final VerticalPanel layout;

    private final Label title;
    private final HorizontalPanel center;
    private final FlexTable table;
    private final Image remove;

    private final Label sourceLabel;
    private final TextBox sourceBox;
    private final Label encodingLabel;
    private final TextBox encodingBox;
    private final Label headLabel;
    private final TextBox headBox;
    private final Label subjectLabel;
    private final TextBox subjectBox;
    private final Label functionLabel;
    private final TextBox functionBox;
    private final Label pLabel;
    private final TextBox pBox;

    public ControlAccessEditor() {
      layout = new VerticalPanel();
      title = new Label(constants.controlAccessesTitle());
      center = new HorizontalPanel();
      table = new FlexTable();
      remove = commonImageBundle.minus().createImage();

      sourceLabel = new Label(constants.controlAccessesSource());
      sourceBox = new TextBox();
      encodingLabel = new Label(constants.controlAccessesID());
      encodingBox = new TextBox();
      headLabel = new Label(constants.controlAccessesName());
      headBox = new TextBox();
      subjectLabel = new Label(constants.controlAccessesDescription());
      subjectBox = new TextBox();
      functionLabel = new Label(constants.controlAccessesLevel());
      functionBox = new TextBox();
      pLabel = new Label(constants.controlAccessesReason());
      pBox = new TextBox();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          ControlAccessEditor.this.onChange(sender);
        }

      };

      sourceBox.addChangeListener(changeListener);
      encodingBox.addChangeListener(changeListener);
      headBox.addChangeListener(changeListener);
      subjectBox.addChangeListener(changeListener);
      functionBox.addChangeListener(changeListener);
      pBox.addChangeListener(changeListener);

      table.setWidget(0, 0, sourceLabel);
      table.setWidget(0, 1, sourceBox);
      table.setWidget(1, 0, encodingLabel);
      table.setWidget(1, 1, encodingBox);
      table.setWidget(2, 0, headLabel);
      table.setWidget(2, 1, headBox);
      table.setWidget(3, 0, subjectLabel);
      table.setWidget(3, 1, subjectBox);
      table.setWidget(4, 0, functionLabel);
      table.setWidget(4, 1, functionBox);
      table.setWidget(5, 0, pLabel);
      table.setWidget(5, 1, pBox);

      center.add(table);
      center.add(remove);

      layout.add(title);
      layout.add(center);

      listeners = new Vector<ControlAccessListener>();

      layout.setCellWidth(title, "100%");
      layout.setCellWidth(center, "100%");
      center.setCellWidth(table, "100%");
      center.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      center.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-controlaccess");
      title.addStyleName("wui-editor-controlaccess-title");
      center.addStyleName("wui-editor-controlaccess-center");
      table.addStyleName("wui-editor-controlaccess-center");
      remove.addStyleName("wui-editor-controlaccess-remove");

      sourceLabel.addStyleName("wui-editor-controlaccess-source-label");
      sourceBox.addStyleName("wui-editor-controlaccess-source-box");
      encodingLabel.addStyleName("wui-editor-controlaccess-encoding-label");
      encodingBox.addStyleName("wui-editor-controlaccess-encoding-box");
      headLabel.addStyleName("wui-editor-controlaccess-head-label");
      headBox.addStyleName("wui-editor-controlaccess-head-box");
      subjectLabel.addStyleName("wui-editor-controlaccess-subject-label");
      subjectBox.addStyleName("wui-editor-controlaccess-subject-box");
      functionLabel.addStyleName("wui-editor-controlaccess-function-label");
      functionBox.addStyleName("wui-editor-controlaccess-function-box");
      pLabel.addStyleName("wui-editor-controlaccess-p-label");
      pBox.addStyleName("wui-editor-controlaccess-p-box");
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
     * Set the control access value
     * 
     * @param controlAccess
     */
    public void setControlAccess(ControlAccess controlAccess) {
      encodingBox.setText(controlAccess.getAttributeEncodinganalog());
      headBox.setText(controlAccess.getHead());
      subjectBox.setText(controlAccess.getSubject());
      functionBox.setText(controlAccess.getFunction());
      pBox.setText(controlAccess.getP());
    }

    /**
     * Get the control access value
     * 
     * @return
     */
    public ControlAccess getControlAccess() {
      ControlAccess controlAccess = null;
      if (!sourceBox.getText().isEmpty() && !encodingBox.getText().isEmpty() && !headBox.getText().isEmpty()
        && !subjectBox.getText().isEmpty() && !functionBox.getText().isEmpty() && !pBox.getText().isEmpty()) {
        controlAccess = new ControlAccess(encodingBox.getText(), sourceBox.getText(), headBox.getText(),
          subjectBox.getText(), functionBox.getText(), pBox.getText());
      }
      return controlAccess;
    }

    /**
     * Add a control access listener
     * 
     * @param listener
     */
    public void addControlAccessListener(ControlAccessListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a control access listener
     * 
     * @param listener
     */
    public void removeControlAccessListener(ControlAccessListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (ControlAccessListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (ControlAccessListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
