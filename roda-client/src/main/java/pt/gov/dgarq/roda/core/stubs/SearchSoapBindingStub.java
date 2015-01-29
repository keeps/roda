/**
 * SearchSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package pt.gov.dgarq.roda.core.stubs;

public class SearchSoapBindingStub extends org.apache.axis.client.Stub implements pt.gov.dgarq.roda.core.stubs.Search {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[2];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("basicSearch");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "query"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "firstResultIndex"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "maxResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "snippetsMax"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fieldMaxLength"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchResult"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.SearchResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "basicSearchReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.SearchException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "SearchException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("advancedSearch");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "searchParameters"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchParameter"), pt.gov.dgarq.roda.core.data.SearchParameter[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "firstResultIndex"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "maxResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "snippetsMax"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fieldMaxLength"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchResult"));
        oper.setReturnClass(pt.gov.dgarq.roda.core.data.SearchResult.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "advancedSearchReturn"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "fault"),
                      "pt.gov.dgarq.roda.core.common.SearchException",
                      new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "SearchException"), 
                      true
                     ));
        _operations[1] = oper;

    }

    public SearchSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public SearchSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public SearchSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://common.core.roda.dgarq.gov.pt", "SearchException");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.common.SearchException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "DescriptionObject");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.DescriptionObject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "RODAObject");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.RODAObject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.SearchParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchResult");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.SearchResult.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchResultObject");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.SearchResultObject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SimpleDescriptionObject");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.SimpleDescriptionObject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Acqinfo");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Acqinfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Acqinfos");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Acqinfos.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Archref");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Archref.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Archrefs");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Archrefs.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTable");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTable.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableBody");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableBody.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableGroup");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableGroup.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableHead");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableHead.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableRow");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableRow.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "BioghistChronitem");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "BioghistChronlist");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ControlAccess");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ControlAccess.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ControlAccesses");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ControlAccesses.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "DescriptionLevel");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Index");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Index.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Indexentry");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Indexentry.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ItemList");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ItemList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "LangmaterialLanguages");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Materialspec");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Materialspec.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Materialspecs");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Materialspecs.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Note");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Note.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Notes");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Notes.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "P");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.P.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "PhysdescElement");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.PhysdescElement.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "PhysdescGenreform");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.PhysdescGenreform.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ProcessInfo");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ProcessInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Relatedmaterial");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Relatedmaterial.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Relatedmaterials");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Relatedmaterials.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Unitid");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Unitid.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://filter.adapter.data.core.roda.dgarq.gov.pt", "ClassificationSchemeFilterParameter");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter.class;
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

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns1_SearchResultObject");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.SearchResultObject[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://data.core.roda.dgarq.gov.pt", "SearchResultObject");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Acqinfo");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Acqinfo[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Acqinfo");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Archref");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Archref[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Archref");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_ArrangementTableGroup");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableGroup[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableGroup");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_ArrangementTableRow");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ArrangementTableRow[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ArrangementTableRow");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_BioghistChronitem");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "BioghistChronitem");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_ControlAccess");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.ControlAccess[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "ControlAccess");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Indexentry");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Indexentry[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Indexentry");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Materialspec");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Materialspec[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Materialspec");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Note");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Note[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Note");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_P");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.P[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "P");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Relatedmaterial");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Relatedmaterial[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Relatedmaterial");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "ArrayOf_tns2_Unitid");
            cachedSerQNames.add(qName);
            cls = pt.gov.dgarq.roda.core.data.eadc.Unitid[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://eadc.data.core.roda.dgarq.gov.pt", "Unitid");
            qName2 = new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

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

    public pt.gov.dgarq.roda.core.data.SearchResult basicSearch(java.lang.String query, int firstResultIndex, int maxResults, int snippetsMax, int fieldMaxLength) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SearchException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "basicSearch"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {query, new java.lang.Integer(firstResultIndex), new java.lang.Integer(maxResults), new java.lang.Integer(snippetsMax), new java.lang.Integer(fieldMaxLength)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.SearchResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.SearchResult) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.SearchResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.SearchException) {
              throw (pt.gov.dgarq.roda.core.common.SearchException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public pt.gov.dgarq.roda.core.data.SearchResult advancedSearch(pt.gov.dgarq.roda.core.data.SearchParameter[] searchParameters, int firstResultIndex, int maxResults, int snippetsMax, int fieldMaxLength) throws java.rmi.RemoteException, pt.gov.dgarq.roda.core.common.SearchException {
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
        _call.setOperationName(new javax.xml.namespace.QName("http://services.core.roda.dgarq.gov.pt", "advancedSearch"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {searchParameters, new java.lang.Integer(firstResultIndex), new java.lang.Integer(maxResults), new java.lang.Integer(snippetsMax), new java.lang.Integer(fieldMaxLength)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (pt.gov.dgarq.roda.core.data.SearchResult) _resp;
            } catch (java.lang.Exception _exception) {
                return (pt.gov.dgarq.roda.core.data.SearchResult) org.apache.axis.utils.JavaUtils.convert(_resp, pt.gov.dgarq.roda.core.data.SearchResult.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof pt.gov.dgarq.roda.core.common.SearchException) {
              throw (pt.gov.dgarq.roda.core.common.SearchException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
