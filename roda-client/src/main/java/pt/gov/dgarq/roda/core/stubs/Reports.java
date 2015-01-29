/**
 * Reports.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Reports extends java.rmi.Remote {
    public int getReportsCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.ReportException;
    public pt.gov.dgarq.roda.core.data.Report[] getReports(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.ReportException;
    public pt.gov.dgarq.roda.core.data.Report getReport(java.lang.String reportID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchReportException, pt.gov.dgarq.roda.core.common.ReportException;
}
