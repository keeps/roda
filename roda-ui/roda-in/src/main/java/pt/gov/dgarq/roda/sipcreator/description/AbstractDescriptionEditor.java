/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.description;

import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangeListener;
import pt.gov.dgarq.roda.ingest.siputility.data.DataChangedEvent;

/**
 * @author Luis Faria
 * 
 */
public abstract class AbstractDescriptionEditor implements DescriptionEditor {

	protected static final int WIDTH = 500;

	private final List<DataChangeListener> listeners;

	protected AbstractDescriptionEditor() {
		listeners = new ArrayList<DataChangeListener>();
	}

	/**
	 * Add data changed listener
	 * 
	 * @param listener
	 */
	public void addDataChangedListener(DataChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove data changed listener
	 * 
	 * @param listener
	 */
	public void removeDataChangedListener(DataChangeListener listener) {
		listeners.remove(listener);
	}

	protected void onDataChanged(EadCValue value) {
		for (DataChangeListener listener : listeners) {
			listener.dataChanged(new DataChangedEvent(value));
		}
	}

}
