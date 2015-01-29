/**
 * Scheduler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Scheduler extends java.rmi.Remote {
    public int getTaskCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException;
    public pt.gov.dgarq.roda.core.data.Task getTask(java.lang.String taskName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskException;
    public pt.gov.dgarq.roda.core.data.Task addTask(pt.gov.dgarq.roda.core.data.Task task) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException;
    public pt.gov.dgarq.roda.core.data.Task[] getTasks(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException;
    public void removeTask(java.lang.String taskName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskException;
    public pt.gov.dgarq.roda.core.data.Task resumeTask(java.lang.String taskName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskException;
    public pt.gov.dgarq.roda.core.data.Task pauseTask(java.lang.String taskName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskException;
    public int getTaskInstanceCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException;
    public pt.gov.dgarq.roda.core.data.TaskInstance[] getTaskInstances(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException;
    public pt.gov.dgarq.roda.core.data.TaskInstance getTaskInstance(java.lang.String taskInstanceID) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskInstanceException;
    public pt.gov.dgarq.roda.core.data.Task modifyTask(pt.gov.dgarq.roda.core.data.Task task) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SchedulerException, pt.gov.dgarq.roda.core.common.NoSuchTaskException;
}
