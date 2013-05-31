/**
 * StatisticsMonitorServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class StatisticsMonitorServiceLocator extends org.apache.axis.client.Service implements pt.gov.dgarq.roda.core.stubs.StatisticsMonitorService {

    public StatisticsMonitorServiceLocator() {
    }


    public StatisticsMonitorServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StatisticsMonitorServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StatisticsMonitor
    private java.lang.String StatisticsMonitor_address = "http://localhost:8080/roda-core/services/StatisticsMonitor";

    public java.lang.String getStatisticsMonitorAddress() {
        return StatisticsMonitor_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StatisticsMonitorWSDDServiceName = "StatisticsMonitor";

    public java.lang.String getStatisticsMonitorWSDDServiceName() {
        return StatisticsMonitorWSDDServiceName;
    }

    public void setStatisticsMonitorWSDDServiceName(java.lang.String name) {
        StatisticsMonitorWSDDServiceName = name;
    }

    public pt.gov.dgarq.roda.core.stubs.StatisticsMonitor getStatisticsMonitor() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StatisticsMonitor_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStatisticsMonitor(endpoint);
    }

    public pt.gov.dgarq.roda.core.stubs.StatisticsMonitor getStatisticsMonitor(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            pt.gov.dgarq.roda.core.stubs.StatisticsMonitorSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.StatisticsMonitorSoapBindingStub(portAddress, this);
            _stub.setPortName(getStatisticsMonitorWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStatisticsMonitorEndpointAddress(java.lang.String address) {
        StatisticsMonitor_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (pt.gov.dgarq.roda.core.stubs.StatisticsMonitor.class.isAssignableFrom(serviceEndpointInterface)) {
                pt.gov.dgarq.roda.core.stubs.StatisticsMonitorSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.StatisticsMonitorSoapBindingStub(new java.net.URL(StatisticsMonitor_address), this);
                _stub.setPortName(getStatisticsMonitorWSDDServiceName());
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
        if ("StatisticsMonitor".equals(inputPortName)) {
            return getStatisticsMonitor();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "StatisticsMonitorService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "StatisticsMonitor"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("StatisticsMonitor".equals(portName)) {
            setStatisticsMonitorEndpointAddress(address);
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
