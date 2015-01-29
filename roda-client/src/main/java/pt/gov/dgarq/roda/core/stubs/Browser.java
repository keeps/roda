/**
 * Browser.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Browser extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.SimpleDescriptionObject getSimpleDescriptionObject(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public java.lang.String[] getDOPIDs() throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getSimpleDescriptionObjectCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationObject[] getDORepresentations(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationObject getDOOriginalRepresentation(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleDescriptionObject[] getSimpleDescriptionObjects(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleRepresentationObject getSimpleRepresentationObject(java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getSimpleRepresentationObjectCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleRepresentationObject[] getSimpleRepresentationObjects(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject getSimpleRepresentationPreservationObject(java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.DescriptionObject getDescriptionObject(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationObject getRepresentationObject(java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationFile getRepresentationFile(java.lang.String roPID, java.lang.String fileID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException, pt.gov.dgarq.roda.core.common.NoSuchRepresentationFileException;
    public pt.gov.dgarq.roda.core.data.RepresentationPreservationObject getROPreservationObject(java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public java.lang.String[] getDOAncestorPIDs(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RODAObject getRODAObject(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationPreservationObject[] getDOPreservationObjects(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getSimpleRepresentationPreservationObjectCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleRepresentationPreservationObject[] getSimpleRepresentationPreservationObjects(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationPreservationObject getRepresentationPreservationObject(java.lang.String poPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject getSimpleEventPreservationObject(java.lang.String roPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getSimpleEventPreservationObjectCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.SimpleEventPreservationObject[] getSimpleEventPreservationObjects(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.EventPreservationObject getEventPreservationObject(java.lang.String poPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.EventPreservationObject[] getPreservationEvents(java.lang.String poPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.AgentPreservationObject getAgentPreservationObject(java.lang.String poPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RODAObjectPermissions getRODAObjectPermissions(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RODAObjectUserPermissions getRODAObjectUserPermissions(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public boolean hasModifyPermission(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public boolean hasRemovePermission(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public boolean hasGrantPermission(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.Producers getProducers(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getRODAObjectCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RepresentationObject getDONormalizedRepresentation(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.BrowserException;
    public int getSimpleDescriptionObjectIndex(java.lang.String pid, pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
    public pt.gov.dgarq.roda.core.data.RODAObject[] getRODAObjects(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.BrowserException;
}
