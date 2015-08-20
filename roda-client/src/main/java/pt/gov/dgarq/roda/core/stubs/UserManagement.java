/**
 * UserManagement.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface UserManagement extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.v2.Group modifyGroup(pt.gov.dgarq.roda.core.data.v2.Group modifiedGroup) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchGroupException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.User modifyUser(pt.gov.dgarq.roda.core.data.v2.User modifiedUser) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
    public boolean removeUser(java.lang.String username) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public void setUserPassword(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public pt.gov.dgarq.roda.core.data.v2.User addUser(pt.gov.dgarq.roda.core.data.v2.User user) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserManagementException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
    public pt.gov.dgarq.roda.core.data.v2.Group addGroup(pt.gov.dgarq.roda.core.data.v2.Group group) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserManagementException;
    public void removeGroup(java.lang.String groupname) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException;
}
