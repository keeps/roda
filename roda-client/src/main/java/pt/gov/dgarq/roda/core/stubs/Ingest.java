/**
 * Ingest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Ingest extends java.rmi.Remote {
    public java.lang.String createDetachedDescriptionObject(pt.gov.dgarq.roda.core.data.DescriptionObject dObject) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String createRepresentationPreservationObject(pt.gov.dgarq.roda.core.data.RepresentationPreservationObject rpo, java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.IngestException, pt.gov.dgarq.roda.core.common.RepresentationAlreadyPreservedException;
    public java.lang.String setDONormalizedRepresentation(java.lang.String doPID, java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String registerEvent(java.lang.String rpoPID, pt.gov.dgarq.roda.core.data.EventPreservationObject eventPO, pt.gov.dgarq.roda.core.data.AgentPreservationObject agent) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String registerDerivationEvent(java.lang.String originalRepresentationPID, java.lang.String derivedRepresentationPID, pt.gov.dgarq.roda.core.data.EventPreservationObject eventPO, pt.gov.dgarq.roda.core.data.AgentPreservationObject agentPO, boolean markObjectsActive) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String createRepresentationObject(pt.gov.dgarq.roda.core.data.RepresentationObject rObject) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.IngestException;
    public void createDerivationRelationship(java.lang.String rpoPID, java.lang.String derivationEventPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String registerIngestEvent(java.lang.String[] doPIDs, java.lang.String[] roPIDs, java.lang.String[] poPIDs, java.lang.String agentName, java.lang.String details) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String createDescriptionObject(pt.gov.dgarq.roda.core.data.DescriptionObject dObject) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException, pt.gov.dgarq.roda.core.common.IngestException;
    public java.lang.String[] removeObjects(java.lang.String[] pids) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestException;
    public void removeDescriptionObject(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.IngestException;
}
