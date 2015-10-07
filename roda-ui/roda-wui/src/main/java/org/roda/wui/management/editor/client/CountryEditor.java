/**
 * 
 */
package org.roda.wui.management.editor.client;

import org.roda.core.data.DescriptionObject;
import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.Text;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class CountryEditor implements MetadataElementEditor {

  private final ControlledVocabularyEditor editor;

  public CountryEditor() {
    editor = new ControlledVocabularyEditor(DescriptionObject.COUNTRYCODE);
    editor.getWidget().addStyleName("wui-editor-country");
  }

  public EadCValue getValue() {
    return new Text(editor.getSelected());
  }

  public Widget getWidget() {
    return editor.getWidget();
  }

  public boolean isEmpty() {
    return false;
  }

  public void setValue(EadCValue value) {
    if (value instanceof Text) {
      Text text = (Text) value;
      editor.setSelected(text.getText());
    }
  }

  public void addChangeListener(ChangeListener listener) {
    editor.addChangeListener(listener);

  }

  public void removeChangeListener(ChangeListener listener) {
    editor.removeChangeListener(listener);
  }

  public boolean isValid() {
    return true;
  }
}
