/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.Text;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;
import pt.gov.dgarq.roda.wui.management.editor.client.MaterialSpecsEditor.MaterialSpecListener;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class DateEditor implements MetadataElementEditor {

	private final DatePicker datePicker;

	public DateEditor() {
		datePicker = new DatePicker(true);
		datePicker.addStyleName("wui-editor-date");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#getValue()
	 */
	public EadCValue getValue() {
		Text ret;
		String date = datePicker.getISODate();
		if (date == null) {
			ret = null;
		} else {
			ret = new Text(date);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#setValue(pt.gov.dgarq.roda.core.data.eadc.EadCValue)
	 */
	public void setValue(EadCValue value) {
		if (value != null && value instanceof Text) {
			Text text = (Text) value;
			datePicker.setISODate(text.getText());

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#getWidget()
	 */
	public Widget getWidget() {
		return datePicker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#isEmpty()
	 */
	public boolean isEmpty() {
		return datePicker.getISODate() == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#addChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		datePicker.addChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.SourcesChangeEvents#removeChangeListener(com.google.gwt.user.client.ui.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		datePicker.removeChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.gov.dgarq.roda.office.management.editor.client.MetadataElementEditor#isValid()
	 */
	public boolean isValid() {
		return datePicker.isValid();
	}
}
