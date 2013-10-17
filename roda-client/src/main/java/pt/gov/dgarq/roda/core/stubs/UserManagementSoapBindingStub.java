/**
 * UserManagementSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class UserManagementSoapBindingStub extends org.apache.axis.client.Stub implements pt.gov.dgarq.roda.core.stubs.UserManagement {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[7];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "group"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "Group"), pt.gov.dgarq.roda.core.data.Group.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "Group"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.Group.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "addGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault1"),
                      "pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "GroupAlreadyExistsException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "groupname"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("modifyGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifiedGroup"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "Group"), pt.gov.dgarq.roda.core.data.Group.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "Group"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.Group.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault3"),
                      "pt.gov.dgarq.roda.core.common.NoSuchGroupException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchGroupException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"), pt.gov.dgarq.roda.core.data.User.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "addUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.UserAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserAlreadyExistsException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault5"),
                      "pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "EmailAlreadyExistsException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("modifyUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifiedUser"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"), pt.gov.dgarq.roda.core.data.User.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault6"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault5"),
                      "pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "EmailAlreadyExistsException"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "removeUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault6"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setUserPassword");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault6"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserManagementException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException"), 
                      true
                     ));
        _operations[6] = oper;

    }

    public UserManagementSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public UserManagementSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public UserManagementSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "EmailAlreadyExistsException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "GroupAlreadyExistsException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.IllegalOperationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchGroupException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.NoSuchGroupException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.NoSuchUserException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "RODAException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.RODAException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "RODAServiceException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.RODAServiceException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserAlreadyExistsException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.UserAlreadyExistsException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserManagementException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.UserManagementException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.UserRegistrationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "Group");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.Group.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "RODAMember");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.RODAMember.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.User.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "FilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.FilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "LikeFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "OneOfManyFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "ProducerFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "RangeFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "RegexFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "SimpleFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_xsd_string");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public pt.gov.dgarq.roda.core.data.Group addGroup(pt.gov.dgarq.roda.core.data.Group group) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserManagementException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "addGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {group});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.Group) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.Group) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.Group.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException) {
              throw (pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeGroup(java.lang.String groupname) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "removeGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {groupname});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.Group modifyGroup(pt.gov.dgarq.roda.core.data.Group modifiedGroup) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchGroupException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {modifiedGroup});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.Group) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.Group) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.Group.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchGroupException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchGroupException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User addUser(pt.gov.dgarq.roda.core.data.User user) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserManagementException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "addUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {user});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.User) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.User) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.User.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserAlreadyExistsException) {
              throw (pt.gov.dgarq.roda.core.common.UserAlreadyExistsException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) {
              throw (pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User modifyUser(pt.gov.dgarq.roda.core.data.User modifiedUser) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {modifiedUser});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.User) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.User) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.User.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) {
              throw (pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean removeUser(java.lang.String username) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "removeUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void setUserPassword(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException, pt.gov.dgarq.roda.core.common.UserManagementException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "setUserPassword"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, password});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserManagementException) {
              throw (pt.gov.dgarq.roda.core.common.UserManagementException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
