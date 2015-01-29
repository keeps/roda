/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class TextEditor implements MetadataElementEditor {

	public static enum Size {
		LINE, AREA, BIGAREA
	}

	private final TextBoxBase layout;

	private final List<ChangeListener> listeners;

	public TextEditor(Size size) {
		if (size.equals(Size.LINE)) {
			layout = new TextBox();
		} else {
			layout = new TextArea();
		}

		if (size.equals(Size.LINE)) {
			layout.addStyleName("wui-editor-text-line");
		} else if (size.equals(Size.AREA)) {
			layout.addStyleName("wui-editor-text-area");
		} else if (size.equals(Size.BIGAREA)) {
			layout.addStyleName("wui-editor-text-bigarea");
		}
		listeners = new Vector<ChangeListener>();
		layout.addKeyboardListener(new KeyboardListener() {

			public void onKeyDown(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyPress(Widget sender, char keyCode, int modifiers) {
			}

			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				TextEditor.this.onChange(layout);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#getValue()
	 */
	public EadCValue getValue() {
		return layout.getText().length() == 0 ? null : new Text(layout
				.getText());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#setValue(pt.gov.dgarq.roda.core.data.eadc.EadCValue)
	 */
	public void setValue(EadCValue value) {
		if (value != null && value instanceof Text) {
			Text text = (Text) value;
			layout.setText(text.getText());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#getWidget()
	 */
	public Widget getWidget() {
		return layout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#isEmpty()
	 */
	public boolean isEmpty() {
		return layout.getText().length() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#removeChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	protected void onChange(Widget sender) {
		for(ChangeListener listener : listeners) {
			listener.onChange(sender);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#isValid()
	 */
	public boolean isValid() {
		return true;
	}
}
