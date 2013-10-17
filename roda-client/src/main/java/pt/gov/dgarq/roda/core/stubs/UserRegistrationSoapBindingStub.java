/**
 * UserRegistrationSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class UserRegistrationSoapBindingStub extends org.apache.axis.client.Stub implements pt.gov.dgarq.roda.core.stubs.UserRegistration {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("registerUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "user"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"), pt.gov.dgarq.roda.core.data.User.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "registerUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.UserAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserAlreadyExistsException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault1"),
                      "pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "EmailAlreadyExistsException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("modifyUnconfirmedEmail");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "email"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyUnconfirmedEmailReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault3"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("confirmUserEmail");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "email"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "emailConfirmationToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "confirmUserEmailReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault5"),
                      "pt.gov.dgarq.roda.core.common.InvalidTokenException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "InvalidTokenException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("resetUserPassword");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "password"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "resetPasswordToken"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "resetUserPasswordReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault5"),
                      "pt.gov.dgarq.roda.core.common.InvalidTokenException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "InvalidTokenException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault3"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUnconfirmedUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "getUnconfirmedUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault3"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("requestPasswordReset");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "username"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "email"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "User"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.User.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "requestPasswordResetReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault2"),
                      "pt.gov.dgarq.roda.core.common.UserRegistrationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "UserRegistrationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault4"),
                      "pt.gov.dgarq.roda.core.common.NoSuchUserException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "NoSuchUserException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault3"),
                      "pt.gov.dgarq.roda.core.common.IllegalOperationException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException"), 
                      true
                     ));
        _operations[5] = oper;

    }

    public UserRegistrationSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public UserRegistrationSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public UserRegistrationSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "IllegalOperationException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.IllegalOperationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "InvalidTokenException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.InvalidTokenException.class;
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

    public pt.gov.dgarq.roda.core.data.User registerUser(pt.gov.dgarq.roda.core.data.User user, java.lang.String password) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserAlreadyExistsException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "registerUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {user, password});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) {
              throw (pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User modifyUnconfirmedEmail(java.lang.String username, java.lang.String email) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "modifyUnconfirmedEmail"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, email});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User confirmUserEmail(java.lang.String username, java.lang.String email, java.lang.String emailConfirmationToken) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.InvalidTokenException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "confirmUserEmail"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, email, emailConfirmationToken});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.InvalidTokenException) {
              throw (pt.gov.dgarq.roda.core.common.InvalidTokenException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User resetUserPassword(java.lang.String username, java.lang.String password, java.lang.String resetPasswordToken) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.InvalidTokenException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "resetUserPassword"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, password, resetPasswordToken});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.InvalidTokenException) {
              throw (pt.gov.dgarq.roda.core.common.InvalidTokenException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User getUnconfirmedUser(java.lang.String username) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "getUnconfirmedUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.User requestPasswordReset(java.lang.String username, java.lang.String email) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.UserRegistrationException, pt.gov.dgarq.roda.core.common.NoSuchUserException, pt.gov.dgarq.roda.core.common.IllegalOperationException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "requestPasswordReset"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {username, email});

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
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.UserRegistrationException) {
              throw (pt.gov.dgarq.roda.core.common.UserRegistrationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.NoSuchUserException) {
              throw (pt.gov.dgarq.roda.core.common.NoSuchUserException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.IllegalOperationException) {
              throw (pt.gov.dgarq.roda.core.common.IllegalOperationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
