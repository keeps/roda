package org.roda.legacy.aip;

import org.roda.index.filter.Filter;
import org.roda.legacy.aip.data.RepresentationFile;
import org.roda.legacy.aip.data.RepresentationObject;
import org.roda.legacy.aip.metadata.descriptive.SimpleRepresentationObject;
import org.roda.legacy.aip.metadata.preservation.AgentPreservationObject;
import org.roda.legacy.aip.metadata.preservation.EventPreservationObject;
import org.roda.legacy.aip.metadata.preservation.RepresentationPreservationObject;
import org.roda.legacy.aip.metadata.preservation.SimpleEventPreservationObject;
import org.roda.legacy.aip.metadata.preservation.SimpleRepresentationPreservationObject;
import org.roda.legacy.aip.permissions.Producers;
import org.roda.legacy.aip.permissions.RODAObjectPermissions;
import org.roda.legacy.aip.permissions.RODAObjectUserPermissions;
import org.roda.legacy.exception.IndexSearchException;
import org.roda.legacy.exception.IndexWriteException;
import org.roda.legacy.old.adapter.ContentAdapter;
import org.roda.storage.StorageActionException;

import pt.gov.dgarq.roda.core.data.v2.RODAObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public interface AIPLegacyMethods {
	
	void setIndexClient(Object indexClient) throws UnsupportedOperationException;

	SimpleDescriptionObject getSimpleDescriptionObject(String id)
			throws IndexSearchException;

	SimpleRepresentationObject getSimpleRepresentationObject(String roID)
			throws IndexSearchException;

	Producers getProducers(String id) throws IndexSearchException;

	void setProducers(String id, Producers producers)
			throws StorageActionException, IndexSearchException,
			IndexWriteException;

	void removeDescriptionObject(String id) throws StorageActionException,
			IndexWriteException;

	boolean hasModifyPermission(String id, String username) throws IndexSearchException;

	boolean hasRemovePermission(String id, String username) throws IndexSearchException;

	boolean hasGrantPermission(String id, String username) throws IndexSearchException;

	RODAObject getRODAObject(String id) throws StorageActionException;

	int getRODAObjectCount(Filter filter);

	RODAObject[] getRODAObjects(ContentAdapter contentAdapter);

	int getSimpleDescriptionObjectCount(Filter filter);

	SimpleDescriptionObject[] getSimpleDescriptionObjects(
			ContentAdapter contentAdapter);

	int getSimpleDescriptionObjectIndex(String id, ContentAdapter contentAdapter);

	String[] getDOIDs();

	String[] getDOAncestorIDs(String id);

	RepresentationObject[] getDORepresentations(String doID);

	RepresentationObject getDOOriginalRepresentation(String doID);

	RepresentationObject getDONormalizedRepresentation(String doID);

	RepresentationPreservationObject[] getDOPreservationObjects(String doID);

	int getSimpleRepresentationObjectCount(Filter filter);

	SimpleRepresentationObject[] getSimpleRepresentationObjects(
			ContentAdapter contentAdapter);

	RepresentationObject getRepresentationObject(String roID);

	RepresentationFile getRepresentationFile(String roID, String fileID);

	RepresentationPreservationObject getROPreservationObject(String roID);

	SimpleRepresentationPreservationObject getSimpleRepresentationPreservationObject(
			String roID);

	int getSimpleRepresentationPreservationObjectCount(Filter filter);

	SimpleRepresentationPreservationObject[] getSimpleRepresentationPreservationObjects(
			ContentAdapter contentAdapter);

	RepresentationPreservationObject getRepresentationPreservationObject(
			String poID);

	SimpleEventPreservationObject getSimpleEventPreservationObject(String roID);

	int getSimpleEventPreservationObjectCount(Filter filter);

	SimpleEventPreservationObject[] getSimpleEventPreservationObjects(
			ContentAdapter contentAdapter);

	EventPreservationObject getEventPreservationObject(String poID);

	EventPreservationObject[] getPreservationEvents(String poID);

	AgentPreservationObject getAgentPreservationObject(String poID);

	RODAObjectPermissions getRODAObjectPermissions(String id);

	RODAObjectUserPermissions getRODAObjectUserPermissions(String id);

	String[] getDOPossibleLevels(String doID);

	RODAObjectPermissions setRODAObjectPermissions(
			RODAObjectPermissions permissions, boolean applyToDescendants);

	String[] removeObjects(String[] ids);

	String createRepresentationObject(RepresentationObject rObject);

	String createRepresentationPreservationObject(
			RepresentationPreservationObject rpo, String doID);

	String setDONormalizedRepresentation(String doID, String roID);

	void createDerivationRelationship(String rpoID, String derivationEventID);

	String registerIngestEvent(String[] doIDs, String[] roIDs, String[] poIDs,
			String agentName, String details);

	String registerEvent(String rpoID, EventPreservationObject eventPO,
			AgentPreservationObject agent);

	String registerDerivationEvent(String originalRepresentationID,
			String derivedRepresentationID, EventPreservationObject eventPO,
			AgentPreservationObject agentPO, boolean markObjectsActive);

}
