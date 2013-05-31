package pt.gov.dgarq.roda.ingest.siputility.data;

import java.util.EventListener;

/**
 * This interface must be implemented by every class interested in receiving
 * events about changes in a data source.
 * 
 * @author Rui Castro
 */
public interface DataChangeListener extends EventListener {

	/**
	 * Called when the data source has changed.
	 * 
	 * @param evtDataChanged
	 *            the event.
	 */
	public void dataChanged(DataChangedEvent evtDataChanged);

}
