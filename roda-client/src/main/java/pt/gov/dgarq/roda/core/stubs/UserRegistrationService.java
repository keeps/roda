/**
 * UserRegistrationService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface UserRegistrationService extends javax.xml.rpc.Service {
    public java.lang.String getUserRegistrationAddress();

    public pt.gov.dgarq.roda.core.stubs.UserRegistration getUserRegistration() throws javax.xml.rpc.ServiceException;

    public pt.gov.dgarq.roda.core.stubs.UserRegistration getUserRegistration(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
