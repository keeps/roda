/**
 * SchedulerServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class SchedulerServiceLocator extends org.apache.axis.client.Service implements pt.gov.dgarq.roda.core.stubs.SchedulerService {

    public SchedulerServiceLocator() {
    }


    public SchedulerServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SchedulerServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Scheduler
    private java.lang.String Scheduler_address = "http://localhost:8080/roda-core/services/Scheduler";

    public java.lang.String getSchedulerAddress() {
        return Scheduler_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SchedulerWSDDServiceName = "Scheduler";

    public java.lang.String getSchedulerWSDDServiceName() {
        return SchedulerWSDDServiceName;
    }

    public void setSchedulerWSDDServiceName(java.lang.String name) {
        SchedulerWSDDServiceName = name;
    }

    public pt.gov.dgarq.roda.core.stubs.Scheduler getScheduler() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Scheduler_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getScheduler(endpoint);
    }

    public pt.gov.dgarq.roda.core.stubs.Scheduler getScheduler(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            pt.gov.dgarq.roda.core.stubs.SchedulerSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.SchedulerSoapBindingStub(portAddress, this);
            _stub.setPortName(getSchedulerWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSchedulerEndpointAddress(java.lang.String address) {
        Scheduler_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (pt.gov.dgarq.roda.core.stubs.Scheduler.class.isAssignableFrom(serviceEndpointInterface)) {
                pt.gov.dgarq.roda.core.stubs.SchedulerSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.SchedulerSoapBindingStub(new java.net.URL(Scheduler_address), this);
                _stub.setPortName(getSchedulerWSDDServiceName());
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
        if ("Scheduler".equals(inputPortName)) {
            return getScheduler();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "SchedulerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "Scheduler"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Scheduler".equals(portName)) {
            setSchedulerEndpointAddress(address);
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
