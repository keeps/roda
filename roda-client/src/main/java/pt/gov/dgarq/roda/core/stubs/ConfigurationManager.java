/**
 * ConfigurationManager.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface ConfigurationManager extends java.rmi.Remote {
    public java.lang.String getProperty(java.lang.String propertyName) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.RODAServiceException;
    public java.lang.String[] getProperties(java.lang.String[] propertyNames) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.RODAServiceException;
}
