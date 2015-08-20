/**
 * Login.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface Login extends java.rmi.Remote {
    public java.lang.String[] getGuestCredentials() throws java.rmi.RemoteException;
    public pt.gov.dgarq.roda.core.data.v2.User getGuestUser() throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoginException;
    public pt.gov.dgarq.roda.core.data.v2.User getAuthenticatedUserCAS(java.lang.String proxyTicket) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.LoginException;
}
