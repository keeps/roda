/**
 * PluginsServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class PluginsServiceLocator extends org.apache.axis.client.Service implements pt.gov.dgarq.roda.core.stubs.PluginsService {

    public PluginsServiceLocator() {
    }


    public PluginsServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public PluginsServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Plugins
    private java.lang.String Plugins_address = "http://localhost:8080/roda-core/services/Plugins";

    public java.lang.String getPluginsAddress() {
        return Plugins_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PluginsWSDDServiceName = "Plugins";

    public java.lang.String getPluginsWSDDServiceName() {
        return PluginsWSDDServiceName;
    }

    public void setPluginsWSDDServiceName(java.lang.String name) {
        PluginsWSDDServiceName = name;
    }

    public pt.gov.dgarq.roda.core.stubs.Plugins getPlugins() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Plugins_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPlugins(endpoint);
    }

    public pt.gov.dgarq.roda.core.stubs.Plugins getPlugins(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            pt.gov.dgarq.roda.core.stubs.PluginsSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.PluginsSoapBindingStub(portAddress, this);
            _stub.setPortName(getPluginsWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setPluginsEndpointAddress(java.lang.String address) {
        Plugins_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (pt.gov.dgarq.roda.core.stubs.Plugins.class.isAssignableFrom(serviceEndpointInterface)) {
                pt.gov.dgarq.roda.core.stubs.PluginsSoapBindingStub _stub = new pt.gov.dgarq.roda.core.stubs.PluginsSoapBindingStub(new java.net.URL(Plugins_address), this);
                _stub.setPortName(getPluginsWSDDServiceName());
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
        if ("Plugins".equals(inputPortName)) {
            return getPlugins();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "PluginsService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "Plugins"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Plugins".equals(portName)) {
            setPluginsEndpointAddress(address);
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
