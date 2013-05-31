/**
 * SchedulerService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface SchedulerService extends javax.xml.rpc.Service {
    public java.lang.String getSchedulerAddress();

    public pt.gov.dgarq.roda.core.stubs.Scheduler getScheduler() throws javax.xml.rpc.ServiceException;

    public pt.gov.dgarq.roda.core.stubs.Scheduler getScheduler(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
