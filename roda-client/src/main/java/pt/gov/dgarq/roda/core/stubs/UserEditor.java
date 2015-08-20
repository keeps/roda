/**
 * UserEditor.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface UserEditor extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.v2.User modifyUser(pt.gov.dgarq.roda.core.data.v2.User modifiedUser, java.lang.String newPassword) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserEditorException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
}
