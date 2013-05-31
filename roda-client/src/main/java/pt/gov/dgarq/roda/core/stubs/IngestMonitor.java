/**
 * IngestMonitor.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface IngestMonitor extends java.rmi.Remote {
    public int getSIPsCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestMonitorException;
    public pt.gov.dgarq.roda.core.data.SIPState[] getSIPs(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IngestMonitorException;
    public java.lang.String[] getPossibleStates() throws java.rmi.RemoteException;
}
