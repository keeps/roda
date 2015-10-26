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

import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.PhysdescGenreform;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class PhysDescGenreformEditor implements MetadataElementEditor {

  private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
    .create(MetadataEditorConstants.class);

  private final List<ChangeListener> listeners;

  private final FlexTable layout;

  private final Label idLabel;
  private final TextBox idBox;
  private final Label sourceLabel;
  private final TextBox sourceBox;
  private final Label nameLabel;
  private final TextBox nameBox;
  private final Label descriptionLabel;
  private final TextBox descriptionBox;

  public PhysDescGenreformEditor() {
    layout = new FlexTable();

    idLabel = new Label(constants.physDescGenreformID());
    idBox = new TextBox();
    sourceLabel = new Label(constants.physDescGenreformSource());
    sourceBox = new TextBox();
    nameLabel = new Label(constants.physDescGenreformName());
    nameBox = new TextBox();
    descriptionLabel = new Label(constants.physDescGenreformDescription());
    descriptionBox = new TextBox();

    ChangeListener changeListener = new ChangeListener() {

      public void onChange(Widget sender) {
        PhysDescGenreformEditor.this.onChange(sender);
      }

    };

    idBox.addChangeListener(changeListener);
    sourceBox.addChangeListener(changeListener);
    nameBox.addChangeListener(changeListener);
    descriptionBox.addChangeListener(changeListener);

    layout.setWidget(0, 0, idLabel);
    layout.setWidget(0, 1, idBox);
    layout.setWidget(1, 0, sourceLabel);
    layout.setWidget(1, 1, sourceBox);
    layout.setWidget(2, 0, nameLabel);
    layout.setWidget(2, 1, nameBox);
    layout.setWidget(3, 0, descriptionLabel);
    layout.setWidget(3, 1, descriptionBox);

    listeners = new Vector<ChangeListener>();

    layout.addStyleName("wui-editor-genreform");
    idLabel.addStyleName("wui-editor-genreform-id-label");
    idBox.addStyleName("wui-editor-genreform-id-box");
    sourceLabel.addStyleName("wui-editor-genreform-source-label");
    sourceBox.addStyleName("wui-editor-genreform-source-box");
    nameLabel.addStyleName("wui-editor-genreform-name-label");
    nameBox.addStyleName("wui-editor-genreform-name-box");
    descriptionLabel.addStyleName("wui-editor-genreform-description-label");
    descriptionBox.addStyleName("wui-editor-genreform-description-box");
  }

  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  public EadCValue getValue() {
    return new PhysdescGenreform(sourceBox.getText(), idBox.getText(), nameBox.getText(), descriptionBox.getText());
  }

  public void setValue(EadCValue value) {
    if (value != null && value instanceof PhysdescGenreform) {
      PhysdescGenreform physdescGenreform = (PhysdescGenreform) value;
      if (physdescGenreform.getAttributeAuthfilenumber() != null) {
        idBox.setText(physdescGenreform.getAttributeAuthfilenumber());
      }
      if (physdescGenreform.getAttributeSource() != null) {
        sourceBox.setText(physdescGenreform.getAttributeSource());
      }
      if (physdescGenreform.getAttributeNormal() != null) {
        nameBox.setText(physdescGenreform.getAttributeNormal());
      }
      if (physdescGenreform.getText() != null) {
        descriptionBox.setText(physdescGenreform.getText());
      }
    }
  }

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return (!idBox.getText().equals("") && !sourceBox.getText().equals("") && !nameBox.getText().equals("") && !descriptionBox
      .getText().equals(""));
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

  public boolean isValid() {
    return (!idBox.getText().equals("") && !sourceBox.getText().equals("") && !nameBox.getText().equals("") && !descriptionBox
      .getText().equals(""));
  }

  public static Widget getReadonlyWidget(EadCValue value) {
    FlexTable panel = new FlexTable();
    PhysdescGenreform physdescGenreform = (PhysdescGenreform) value;
    int row = 0;
    row = addToReadonlyWidget(constants.physDescGenreformID(), physdescGenreform.getAttributeAuthfilenumber(), panel,
      row);
    row = addToReadonlyWidget(constants.physDescGenreformSource(), physdescGenreform.getAttributeSource(), panel, row);
    row = addToReadonlyWidget(constants.physDescGenreformName(), physdescGenreform.getAttributeNormal(), panel, row);
    row = addToReadonlyWidget(constants.physDescGenreformDescription(), physdescGenreform.getText(), panel, row);

    panel.addStyleName("wui-editor-genreform-readonly");
    return panel;
  }

  private static int addToReadonlyWidget(String label, String value, FlexTable panel, int row) {
    if (value != null && !value.isEmpty()) {
      Label column0 = new Label(label);
      Label column1 = new Label(value);
      panel.setWidget(row, 0, column0);
      panel.setWidget(row, 1, column1);
      panel.getColumnFormatter().setWidth(1, "100%");
      column0.addStyleName("wui-editor-genreform-readonly-column0");
      column1.addStyleName("wui-editor-genreform-readonly-column1");
      row++;
    }
    return row;
  }
}
