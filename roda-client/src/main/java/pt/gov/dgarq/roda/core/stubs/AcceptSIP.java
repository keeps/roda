/**
 * AcceptSIP.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public interface AcceptSIP extends java.rmi.Remote {
    public pt.gov.dgarq.roda.core.data.SIPState acceptSIP(java.lang.String sipID, boolean accept, java.lang.String reason) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchSIPException, pt.gov.dgarq.roda.core.common.AcceptSIPException, pt.gov.dgarq.roda.core.common.IllegalOperationException;
}
