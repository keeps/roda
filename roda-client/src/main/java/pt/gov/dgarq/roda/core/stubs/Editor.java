/**
 * Editor.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Editor extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.RODAObjectPermissions setRODAObjectPermissions(pt.gov.dgarq.roda.core.data.RODAObjectPermissions permissions, boolean applyToDescendants) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.EditorException;
    public java.lang.String createDescriptionObject(pt.gov.dgarq.roda.core.data.DescriptionObject dObject) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.InvalidDescriptionObjectException, pt.gov.dgarq.roda.core.common.EditorException;
    public pt.gov.dgarq.roda.core.data.DescriptionObject modifyDescriptionObject(pt.gov.dgarq.roda.core.data.DescriptionObject dObject) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.EditorException;
    public void removeDescriptionObject(java.lang.String pid) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.EditorException;
    public pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel[] getDOPossibleLevels(java.lang.String doPID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.EditorException;
    public void setProducers(java.lang.String doPID, pt.gov.dgarq.roda.core.data.Producers producers) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchRODAObjectException, pt.gov.dgarq.roda.core.common.EditorException;
}
