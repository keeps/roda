/**
 * Login.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Login extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.User getAuthenticatedUser(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoginException;
    public pt.gov.dgarq.roda.core.data.User getGuestUser() throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoginException;
    public java.lang.String[] getGuestCredentials() throws java.rmi.RemoteException;
}
