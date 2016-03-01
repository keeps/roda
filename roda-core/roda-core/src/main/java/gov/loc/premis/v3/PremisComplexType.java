/*
 * XML Type:  premisComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PremisComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML premisComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface PremisComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(PremisComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("premiscomplextyped3d3type");
    
    /**
     * Gets array of all "object" elements
     */
    gov.loc.premis.v3.ObjectComplexType[] getObjectArray();
    
    /**
     * Gets ith "object" element
     */
    gov.loc.premis.v3.ObjectComplexType getObjectArray(int i);
    
    /**
     * Returns number of "object" element
     */
    int sizeOfObjectArray();
    
    /**
     * Sets array of all "object" element
     */
    void setObjectArray(gov.loc.premis.v3.ObjectComplexType[] objectArray);
    
    /**
     * Sets ith "object" element
     */
    void setObjectArray(int i, gov.loc.premis.v3.ObjectComplexType object);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "object" element
     */
    gov.loc.premis.v3.ObjectComplexType insertNewObject(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "object" element
     */
    gov.loc.premis.v3.ObjectComplexType addNewObject();
    
    /**
     * Removes the ith "object" element
     */
    void removeObject(int i);
    
    /**
     * Gets array of all "event" elements
     */
    gov.loc.premis.v3.EventComplexType[] getEventArray();
    
    /**
     * Gets ith "event" element
     */
    gov.loc.premis.v3.EventComplexType getEventArray(int i);
    
    /**
     * Returns number of "event" element
     */
    int sizeOfEventArray();
    
    /**
     * Sets array of all "event" element
     */
    void setEventArray(gov.loc.premis.v3.EventComplexType[] eventArray);
    
    /**
     * Sets ith "event" element
     */
    void setEventArray(int i, gov.loc.premis.v3.EventComplexType event);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "event" element
     */
    gov.loc.premis.v3.EventComplexType insertNewEvent(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "event" element
     */
    gov.loc.premis.v3.EventComplexType addNewEvent();
    
    /**
     * Removes the ith "event" element
     */
    void removeEvent(int i);
    
    /**
     * Gets array of all "agent" elements
     */
    gov.loc.premis.v3.AgentComplexType[] getAgentArray();
    
    /**
     * Gets ith "agent" element
     */
    gov.loc.premis.v3.AgentComplexType getAgentArray(int i);
    
    /**
     * Returns number of "agent" element
     */
    int sizeOfAgentArray();
    
    /**
     * Sets array of all "agent" element
     */
    void setAgentArray(gov.loc.premis.v3.AgentComplexType[] agentArray);
    
    /**
     * Sets ith "agent" element
     */
    void setAgentArray(int i, gov.loc.premis.v3.AgentComplexType agent);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agent" element
     */
    gov.loc.premis.v3.AgentComplexType insertNewAgent(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agent" element
     */
    gov.loc.premis.v3.AgentComplexType addNewAgent();
    
    /**
     * Removes the ith "agent" element
     */
    void removeAgent(int i);
    
    /**
     * Gets array of all "rights" elements
     */
    gov.loc.premis.v3.RightsComplexType[] getRightsArray();
    
    /**
     * Gets ith "rights" element
     */
    gov.loc.premis.v3.RightsComplexType getRightsArray(int i);
    
    /**
     * Returns number of "rights" element
     */
    int sizeOfRightsArray();
    
    /**
     * Sets array of all "rights" element
     */
    void setRightsArray(gov.loc.premis.v3.RightsComplexType[] rightsArray);
    
    /**
     * Sets ith "rights" element
     */
    void setRightsArray(int i, gov.loc.premis.v3.RightsComplexType rights);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rights" element
     */
    gov.loc.premis.v3.RightsComplexType insertNewRights(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rights" element
     */
    gov.loc.premis.v3.RightsComplexType addNewRights();
    
    /**
     * Removes the ith "rights" element
     */
    void removeRights(int i);
    
    /**
     * Gets the "version" attribute
     */
    gov.loc.premis.v3.Version3.Enum getVersion();
    
    /**
     * Gets (as xml) the "version" attribute
     */
    gov.loc.premis.v3.Version3 xgetVersion();
    
    /**
     * Sets the "version" attribute
     */
    void setVersion(gov.loc.premis.v3.Version3.Enum version);
    
    /**
     * Sets (as xml) the "version" attribute
     */
    void xsetVersion(gov.loc.premis.v3.Version3 version);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.PremisComplexType newInstance() {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.PremisComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.PremisComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.PremisComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.PremisComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.PremisComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
