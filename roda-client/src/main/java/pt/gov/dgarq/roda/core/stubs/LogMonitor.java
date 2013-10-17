/**
 * LogMonitor.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface LogMonitor extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.LogEntry[] getLogEntries(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoggerException;
    public int getLogEntriesCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoggerException;
}
