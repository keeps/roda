/**
 * UserRegistration.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface UserRegistration extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.User registerUser(pt.gov.dgarq.roda.core.data.User user, java.lang.String password) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
    public pt.gov.dgarq.roda.core.data.User getUnconfirmedUser(java.lang.String username) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException;
    public pt.gov.dgarq.roda.core.data.User modifyUnconfirmedEmail(java.lang.String username, java.lang.String email) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException;
    public pt.gov.dgarq.roda.core.data.User confirmUserEmail(java.lang.String username, java.lang.String email, java.lang.String emailConfirmationToken) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.InvalidTokenException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException;
    public pt.gov.dgarq.roda.core.data.User requestPasswordReset(java.lang.String username, java.lang.String email) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException;
    public pt.gov.dgarq.roda.core.data.User resetUserPassword(java.lang.String username, java.lang.String password, java.lang.String resetPasswordToken) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.InvalidTokenException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException;
}
