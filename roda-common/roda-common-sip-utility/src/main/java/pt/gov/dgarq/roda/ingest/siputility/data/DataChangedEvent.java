package pt.gov.dgarq.roda.ingest.siputility.data;

import java.util.EventObject;

/**
 * {@link DataChangedEvent} is used to notify interested parties that state has
 * changed in the event source {@link Object}.
 * 
 * @author Rui Castro
 */
public class DataChangedEvent extends EventObject {

	private EventObject causeEvent = null;

	/**
	 * Creates a new {@link DataChangedEvent} for the given {@link Object}.
	 * 
	 * @param source
	 *            the source.
	 */
	public DataChangedEvent(Object source) {
		super(source);
	}

	/**
	 * Creates a new {@link DataChangedEvent} for the given {@link Object} that
	 * was caused by another {@link EventObject}.
	 * 
	 * @param source
	 *            the source.
	 * @param causeEvent
	 *            the cause event.
	 */
	public DataChangedEvent(Object source, EventObject causeEvent) {
		super(source);
		setCauseEvent(causeEvent);
	}

	/**
	 * @return the causeEvent
	 */
	public EventObject getCauseEvent() {
		return causeEvent;
	}

	/**
	 * @param causeEvent
	 *            the causeEvent to set
	 */
	private void setCauseEvent(EventObject causeEvent) {
		this.causeEvent = causeEvent;
	}

}
