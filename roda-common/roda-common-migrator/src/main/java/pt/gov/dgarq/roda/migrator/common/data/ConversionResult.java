package pt.gov.dgarq.roda.migrator.common.data;

import java.io.Serializable;

import pt.gov.dgarq.roda.core.data.AgentPreservationObject;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationObject;

/**
 * @author Rui Castro
 */
public class ConversionResult implements Serializable {
	private static final long serialVersionUID = -8286621668029986456L;

	private RepresentationObject representation = null;
	private EventPreservationObject migrationEvent = null;
	private AgentPreservationObject migrationAgent = null;

	/**
	 * Constructs new {@link ConversionResult}.
	 */
	public ConversionResult() {
	}

	/**
	 * Constructs new {@link ConversionResult}.
	 */
	public ConversionResult(RepresentationObject rObject,
			EventPreservationObject event, AgentPreservationObject agent) {
		setRepresentation(rObject);
		setMigrationEvent(event);
		setMigrationAgent(agent);
	}

	@Override
	public String toString() {
		return "ConversionResult(" + getRepresentation() + ", "
				+ getMigrationEvent() + "," + getMigrationAgent() + ")";
	}

	/**
	 * @return the representation
	 */
	public RepresentationObject getRepresentation() {
		return representation;
	}

	/**
	 * @param representation
	 *            the representation to set
	 */
	public void setRepresentation(RepresentationObject representation) {
		this.representation = representation;
	}

	/**
	 * @return the migrationEvent
	 */
	public EventPreservationObject getMigrationEvent() {
		return migrationEvent;
	}

	/**
	 * @param migrationEvent
	 *            the migrationEvent to set
	 */
	public void setMigrationEvent(EventPreservationObject migrationEvent) {
		this.migrationEvent = migrationEvent;
	}

	/**
	 * @return the migrationAgent
	 */
	public AgentPreservationObject getMigrationAgent() {
		return migrationAgent;
	}

	/**
	 * @param migrationAgent
	 *            the migrationAgent to set
	 */
	public void setMigrationAgent(AgentPreservationObject migrationAgent) {
		this.migrationAgent = migrationAgent;
	}

}
