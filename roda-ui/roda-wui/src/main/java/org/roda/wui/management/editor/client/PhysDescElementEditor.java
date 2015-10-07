/**
 * 
 */
package org.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import org.roda.core.data.eadc.EadCValue;
import org.roda.core.data.eadc.PhysdescElement;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class PhysDescElementEditor implements MetadataElementEditor {

  private final HorizontalPanel layout;

  private final TextBox value;

  private final ControlledVocabularyEditor unitEditor;

  private final List<ChangeListener> listeners;

  public PhysDescElementEditor(String field) {
    layout = new HorizontalPanel();
    value = new TextBox();
    unitEditor = new ControlledVocabularyEditor(field + "_unit");
    listeners = new Vector<ChangeListener>();

    layout.add(value);
    layout.add(unitEditor.getWidget());

    value.addKeyboardListener(new KeyboardListener() {

      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
      }

      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        PhysDescElementEditor.this.onChange(sender);
      }

    });

    unitEditor.addChangeListener(new ChangeListener() {

      public void onChange(Widget sender) {
        PhysDescElementEditor.this.onChange(sender);
      }

    });

    layout.addStyleName("wui-editor-physdesc");
    value.addStyleName("physdesc-value");
    unitEditor.getWidget().addStyleName("physdesc-unit");
  }

  public EadCValue getValue() {
    PhysdescElement element = (value.getText().length() == 0) ? null : new PhysdescElement(value.getText(),
      unitEditor.getSelected());

    return element;
  }

  public void setValue(EadCValue value) {
    if (value != null && value instanceof PhysdescElement) {
      PhysdescElement element = (PhysdescElement) value;
      if (element == null) {
        this.value.setText("");
      } else {
        this.value.setText(element.getValue());
        this.unitEditor.setSelected(element.getUnit());
      }
    }

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

  public Widget getWidget() {
    return layout;
  }

  public boolean isEmpty() {
    return value.getText().length() == 0;
  }

  public boolean isValid() {
    return true;
  }
}
