/**
 * SynchronousConverter.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.migrator.stubs;

public interface SynchronousConverter extends java.rmi.Remote {
    public pt.gov.dgarq.roda.migrator.common.data.ConversionResult convert(pt.gov.dgarq.roda.core.data.RepresentationObject in0) throws java.rmi.RemoteException, pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException, pt.gov.dgarq.roda.migrator.common.ConverterException, pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException, pt.gov.dgarq.roda.migrator.common.InvalidRepresentationException, pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
    public pt.gov.dgarq.roda.core.data.AgentPreservationObject getAgent() throws java.rmi.RemoteException, pt.gov.dgarq.roda.migrator.common.ConverterException;
}
