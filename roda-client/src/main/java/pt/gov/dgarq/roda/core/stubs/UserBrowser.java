/**
 * UserBrowser.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface UserBrowser extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.v2.User[] getUsers(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.Group[] getGroups(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public java.lang.String[] getRoles() throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public java.lang.String[] getUserDirectRoles(java.lang.String userName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public int getUserCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public int getGroupCount(pt.gov.dgarq.roda.core.data.adapter.filter.Filter filter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.Group getGroup(java.lang.String groupName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public java.lang.String[] getUserNames(pt.gov.dgarq.roda.core.data.adapter.ContentAdapter contentAdapter) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.User getUser(java.lang.String name) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.User[] getUsersInGroup(java.lang.String groupName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public java.lang.String[] getGroupDirectRoles(java.lang.String groupName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserManagementException;
}
