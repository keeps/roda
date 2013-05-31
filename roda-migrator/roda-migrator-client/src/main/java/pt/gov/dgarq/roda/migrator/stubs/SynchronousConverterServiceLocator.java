/**
 * SynchronousConverterServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.migrator.stubs;

public class SynchronousConverterServiceLocator extends org.apache.axis.client.Service implements pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterService {

    public SynchronousConverterServiceLocator() {
    }


    public SynchronousConverterServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SynchronousConverterServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SynchronousConverter
    private java.lang.String SynchronousConverter_address = "http://localhost:8080/roda-migrator/services/SynchronousConverter";

    public java.lang.String getSynchronousConverterAddress() {
        return SynchronousConverter_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SynchronousConverterWSDDServiceName = "SynchronousConverter";

    public java.lang.String getSynchronousConverterWSDDServiceName() {
        return SynchronousConverterWSDDServiceName;
    }

    public void setSynchronousConverterWSDDServiceName(java.lang.String name) {
        SynchronousConverterWSDDServiceName = name;
    }

    public pt.gov.dgarq.roda.migrator.stubs.SynchronousConverter getSynchronousConverter() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SynchronousConverter_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSynchronousConverter(endpoint);
    }

    public pt.gov.dgarq.roda.migrator.stubs.SynchronousConverter getSynchronousConverter(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterSoapBindingStub _stub = new pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterSoapBindingStub(portAddress, this);
            _stub.setPortName(getSynchronousConverterWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSynchronousConverterEndpointAddress(java.lang.String address) {
        SynchronousConverter_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (pt.gov.dgarq.roda.migrator.stubs.SynchronousConverter.class.isAssignableFrom(serviceEndpointInterface)) {
                pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterSoapBindingStub _stub = new pt.gov.dgarq.roda.migrator.stubs.SynchronousConverterSoapBindingStub(new java.net.URL(SynchronousConverter_address), this);
                _stub.setPortName(getSynchronousConverterWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SynchronousConverter".equals(inputPortName)) {
            return getSynchronousConverter();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.migrator.roda.dgarq.gov.pt", "SynchronousConverterService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.migrator.roda.dgarq.gov.pt", "SynchronousConverter"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SynchronousConverter".equals(portName)) {
            setSynchronousConverterEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
