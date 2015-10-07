/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.Acqinfo;
import org.roda.core.data.eadc.Acqinfos;
import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.P;
import org.roda.wui.common.client.images.CommonImageBundle;
import org.roda.wui.common.client.widgets.WUIButton;

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
public class AcqInfosEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT.create(CommonImageBundle.class);

  private final List<ChangeListener> listeners;

  private final DockPanel layout;

  private final WUIButton addAcqInfo;
  private final List<AcqInfoEditor> acqInfos;
  private final VerticalPanel acqInfosLayout;

  /**
   * Listener for acquisition infos
   * 
   */
  public interface AcqInfoListener extends ChangeListener {
    /**
     * Called when an acquisition info is removed
     * 
     * @param sender
     */
    public void onRemove(AcqInfoEditor sender);
  }

  public AcqInfosEditor() {
    layout = new DockPanel();
    addAcqInfo = new WUIButton(constants.acqInfosAdd(), WUIButton.Left.ROUND, WUIButton.Right.PLUS);
    acqInfosLayout = new VerticalPanel();

    layout.add(acqInfosLayout, DockPanel.CENTER);
    layout.add(addAcqInfo, DockPanel.SOUTH);

    acqInfos = new Vector<AcqInfoEditor>();
    listeners = new Vector<ChangeListener>();

    addAcqInfo.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        createAcqInfoEditor();
        updateLayout();
        onChange(layout);
      }

    });

    layout.addStyleName("wui-editor-acqinfos");
    acqInfosLayout.addStyleName("wui-editor-acqinfos-layout");
    addAcqInfo.addStyleName("wui-editor-acqinfos-add");
  }

  protected AcqInfoEditor createAcqInfoEditor() {
    AcqInfoEditor editor = new AcqInfoEditor();
    editor.addAcqInfoListener(new AcqInfoListener() {
      public void onRemove(AcqInfoEditor sender) {
        acqInfos.remove(sender);
        updateLayout();
        AcqInfosEditor.this.onChange(layout);
      }

      public void onChange(Widget sender) {
        AcqInfosEditor.this.onChange(sender);
      }
    });
    acqInfos.add(editor);
    return editor;
  }

  protected void updateLayout() {
    acqInfosLayout.clear();
    for (AcqInfoEditor acqInfoEditor : acqInfos) {
      acqInfosLayout.add(acqInfoEditor.getWidget());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    List<Acqinfo> acqinfos = new Vector<Acqinfo>();
    for (AcqInfoEditor acquiInfoEditor : acqInfos) {
      Acqinfo acqinfo = acquiInfoEditor.getAcqInfo();
      if (acqinfo != null) {
        acqinfos.add(acqinfo);
      }
    }
    return acqinfos.size() == 0 ? null : new Acqinfos(acqinfos.toArray(new Acqinfo[] {}));
  }

  public void setValue(EadCValue value) {
    if (value instanceof Acqinfos) {
      Acqinfos acqinfos = (Acqinfos) value;
      acqInfos.clear();
      for (int i = 0; i < acqinfos.getAcqinfos().length; i++) {
        Acqinfo acqinfo = acqinfos.getAcqinfos()[i];
        AcqInfoEditor acqInfoEditor = createAcqInfoEditor();
        acqInfoEditor.setAcqInfo(acqinfo);
      }
      updateLayout();
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (acqInfos.size() == 0);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    boolean valid = true;
    for (AcqInfoEditor acquiInfoEditor : acqInfos) {
      Acqinfo acqinfo = acquiInfoEditor.getAcqInfo();
      if (acqinfo == null) {
        valid = false;
        acquiInfoEditor.getWidget().addStyleName("unvalid");
      } else {
        acquiInfoEditor.getWidget().removeStyleName("unvalid");
      }
    }
    return valid;
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    VerticalPanel panel = new VerticalPanel();
    Acqinfos acqinfos = (Acqinfos) value;
    for (int i = 0; i < acqinfos.getAcqinfos().length; i++) {
      Acqinfo acqinfo = acqinfos.getAcqinfos()[i];
      P p = acqinfo.getP();
      Label sectionLabel = new Label(constants.acqInfosTitle());
      FlexTable sectionPanel = new FlexTable();
      int row = 0;
      row = addToReadonlyWidget(constants.acqInfosText(), p.getText(), sectionPanel, row);
      row = addToReadonlyWidget(constants.acqInfosDate(), p.getDate(), sectionPanel, row);
      row = addToReadonlyWidget(constants.acqInfosNum(), p.getNum(), sectionPanel, row);
      row = addToReadonlyWidget(constants.acqInfosCorpname(), p.getCorpname(), sectionPanel, row);
      panel.add(sectionLabel);
      panel.add(sectionPanel);
      sectionLabel.addStyleName("wui-editor-acqinfos-readonly-label");
      sectionPanel.addStyleName("wui-editor-acqinfos-readonly-acqinfo");
    }
    panel.addStyleName("wui-editor-acqinfos-readonly");
    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-acqinfos-readonly-column0");
      column1.addStyleName("wui-editor-acqinfos-readonly-column1");
      row++;
    }
    return row;
  }

  /**
   * Editor for acquisition info
   * 
   */
  public class AcqInfoEditor {

    private final List<AcqInfoListener> listeners;

    private final HorizontalPanel layout;

    private final FlexTable subLayout;
    private final Label textLabel;
    private final TextBox textBox;
    private final Label dateLabel;
    private final TextBox dateBox;
    private final Label numLabel;
    private final TextBox numBox;
    private final Label corpnameLabel;
    private final TextBox corpnameBox;
    private final Image remove;

    public AcqInfoEditor() {
      layout = new HorizontalPanel();
      subLayout = new FlexTable();

      textLabel = new Label(constants.acqInfosText());
      textBox = new TextBox();
      dateLabel = new Label(constants.acqInfosDate());
      dateBox = new TextBox();
      numLabel = new Label(constants.acqInfosNum());
      numBox = new TextBox();
      corpnameLabel = new Label(constants.acqInfosCorpname());
      corpnameBox = new TextBox();

      subLayout.setWidget(0, 0, textLabel);
      subLayout.setWidget(0, 1, textBox);
      subLayout.setWidget(1, 0, dateLabel);
      subLayout.setWidget(1, 1, dateBox);
      subLayout.setWidget(2, 0, numLabel);
      subLayout.setWidget(2, 1, numBox);
      subLayout.setWidget(3, 0, corpnameLabel);
      subLayout.setWidget(3, 1, corpnameBox);

      remove = commonImageBundle.minus().createImage();

      layout.add(subLayout);
      layout.add(remove);

      listeners = new Vector<AcqInfoListener>();

      remove.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          onRemove();
        }

      });

      ChangeListener changeListener = new ChangeListener() {

        public void onChange(Widget sender) {
          AcqInfoEditor.this.onChange(sender);
        }

      };

      textBox.addChangeListener(changeListener);
      dateBox.addChangeListener(changeListener);
      numBox.addChangeListener(changeListener);
      corpnameBox.addChangeListener(changeListener);

      layout.setCellWidth(subLayout, "100%");
      layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
      layout.setCellHorizontalAlignment(remove, HasAlignment.ALIGN_CENTER);
      layout.addStyleName("wui-editor-acqinfo");
      subLayout.addStyleName("wui-editor-acqinfo-center");
      remove.addStyleName("wui-editor-acqinfo-remove");
      textLabel.addStyleName("wui-editor-acqinfo-text-label");
      textBox.addStyleName("wui-editor-acqinfo-text-box");
      dateLabel.addStyleName("wui-editor-acqinfo-date-label");
      dateBox.addStyleName("wui-editor-acqinfo-date-box");
      numLabel.addStyleName("wui-editor-acqinfo-num-label");
      numBox.addStyleName("wui-editor-acqinfo-num-box");
      corpnameLabel.addStyleName("wui-editor-acqinfo-corpname-label");
      corpnameBox.addStyleName("wui-editor-acqinfo-corpname-box");
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
     * Set the acquisition info value
     * 
     * @param acqinfo
     */
    public void setAcqInfo(Acqinfo acqinfo) {
      if (acqinfo.getP() != null) {
        P p = acqinfo.getP();
        if (p.getText() != null && !p.getText().isEmpty()) {
          textBox.setText(p.getText());
        }
        if (p.getDate() != null && !p.getDate().isEmpty()) {
          dateBox.setText(p.getDate());
        }
        if (p.getNum() != null && !p.getNum().isEmpty()) {
          numBox.setText(p.getNum());
        }
        if (p.getCorpname() != null && !p.getCorpname().isEmpty()) {
          corpnameBox.setText(p.getCorpname());
        }
      }
    }

    /**
     * Get the acquisition info value
     * 
     * @return
     */
    public Acqinfo getAcqInfo() {
      Acqinfo acqinfo = null;
      if (!textBox.getText().isEmpty() || !dateBox.getText().isEmpty() || !numBox.getText().isEmpty()
        || !corpnameBox.getText().isEmpty()) {
        P p = new P();
        if (!textBox.getText().isEmpty()) {
          p.setText(textBox.getText());
        }
        if (!dateBox.getText().isEmpty()) {
          p.setDate(dateBox.getText());
        }
        if (!numBox.getText().isEmpty()) {
          p.setNum(numBox.getText());
        }
        if (!corpnameBox.getText().isEmpty()) {
          p.setCorpname(corpnameBox.getText());
        }
        acqinfo = new Acqinfo();
        acqinfo.setP(p);
      }
      return acqinfo;
    }

    /**
     * Add a acquisition info listener
     * 
     * @param listener
     */
    public void addAcqInfoListener(AcqInfoListener listener) {
      listeners.add(listener);
    }

    /**
     * Remove a acquisition info listener
     * 
     * @param listener
     */
    public void removeAcqInfoListener(AcqInfoListener listener) {
      listeners.remove(listener);
    }

    protected void onChange(Widget sender) {
      for (AcqInfoListener listener : listeners) {
        listener.onChange(sender);
      }
    }

    protected void onRemove() {
      for (AcqInfoListener listener : listeners) {
        listener.onRemove(this);
      }
    }
  }
}
