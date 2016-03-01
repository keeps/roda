/*
 * XML Type:  eventComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML eventComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface EventComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(EventComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("eventcomplextypecd27type");
    
    /**
     * Gets the "eventIdentifier" element
     */
    gov.loc.premis.v3.EventIdentifierComplexType getEventIdentifier();
    
    /**
     * Sets the "eventIdentifier" element
     */
    void setEventIdentifier(gov.loc.premis.v3.EventIdentifierComplexType eventIdentifier);
    
    /**
     * Appends and returns a new empty "eventIdentifier" element
     */
    gov.loc.premis.v3.EventIdentifierComplexType addNewEventIdentifier();
    
    /**
     * Gets the "eventType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getEventType();
    
    /**
     * Sets the "eventType" element
     */
    void setEventType(gov.loc.premis.v3.StringPlusAuthority eventType);
    
    /**
     * Appends and returns a new empty "eventType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewEventType();
    
    /**
     * Gets the "eventDateTime" element
     */
    java.lang.String getEventDateTime();
    
    /**
     * Gets (as xml) the "eventDateTime" element
     */
    org.apache.xmlbeans.XmlString xgetEventDateTime();
    
    /**
     * Sets the "eventDateTime" element
     */
    void setEventDateTime(java.lang.String eventDateTime);
    
    /**
     * Sets (as xml) the "eventDateTime" element
     */
    void xsetEventDateTime(org.apache.xmlbeans.XmlString eventDateTime);
    
    /**
     * Gets array of all "eventDetailInformation" elements
     */
    gov.loc.premis.v3.EventDetailInformationComplexType[] getEventDetailInformationArray();
    
    /**
     * Gets ith "eventDetailInformation" element
     */
    gov.loc.premis.v3.EventDetailInformationComplexType getEventDetailInformationArray(int i);
    
    /**
     * Returns number of "eventDetailInformation" element
     */
    int sizeOfEventDetailInformationArray();
    
    /**
     * Sets array of all "eventDetailInformation" element
     */
    void setEventDetailInformationArray(gov.loc.premis.v3.EventDetailInformationComplexType[] eventDetailInformationArray);
    
    /**
     * Sets ith "eventDetailInformation" element
     */
    void setEventDetailInformationArray(int i, gov.loc.premis.v3.EventDetailInformationComplexType eventDetailInformation);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventDetailInformation" element
     */
    gov.loc.premis.v3.EventDetailInformationComplexType insertNewEventDetailInformation(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventDetailInformation" element
     */
    gov.loc.premis.v3.EventDetailInformationComplexType addNewEventDetailInformation();
    
    /**
     * Removes the ith "eventDetailInformation" element
     */
    void removeEventDetailInformation(int i);
    
    /**
     * Gets array of all "eventOutcomeInformation" elements
     */
    gov.loc.premis.v3.EventOutcomeInformationComplexType[] getEventOutcomeInformationArray();
    
    /**
     * Gets ith "eventOutcomeInformation" element
     */
    gov.loc.premis.v3.EventOutcomeInformationComplexType getEventOutcomeInformationArray(int i);
    
    /**
     * Returns number of "eventOutcomeInformation" element
     */
    int sizeOfEventOutcomeInformationArray();
    
    /**
     * Sets array of all "eventOutcomeInformation" element
     */
    void setEventOutcomeInformationArray(gov.loc.premis.v3.EventOutcomeInformationComplexType[] eventOutcomeInformationArray);
    
    /**
     * Sets ith "eventOutcomeInformation" element
     */
    void setEventOutcomeInformationArray(int i, gov.loc.premis.v3.EventOutcomeInformationComplexType eventOutcomeInformation);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventOutcomeInformation" element
     */
    gov.loc.premis.v3.EventOutcomeInformationComplexType insertNewEventOutcomeInformation(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventOutcomeInformation" element
     */
    gov.loc.premis.v3.EventOutcomeInformationComplexType addNewEventOutcomeInformation();
    
    /**
     * Removes the ith "eventOutcomeInformation" element
     */
    void removeEventOutcomeInformation(int i);
    
    /**
     * Gets array of all "linkingAgentIdentifier" elements
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] getLinkingAgentIdentifierArray();
    
    /**
     * Gets ith "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType getLinkingAgentIdentifierArray(int i);
    
    /**
     * Returns number of "linkingAgentIdentifier" element
     */
    int sizeOfLinkingAgentIdentifierArray();
    
    /**
     * Sets array of all "linkingAgentIdentifier" element
     */
    void setLinkingAgentIdentifierArray(gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] linkingAgentIdentifierArray);
    
    /**
     * Sets ith "linkingAgentIdentifier" element
     */
    void setLinkingAgentIdentifierArray(int i, gov.loc.premis.v3.LinkingAgentIdentifierComplexType linkingAgentIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType insertNewLinkingAgentIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType addNewLinkingAgentIdentifier();
    
    /**
     * Removes the ith "linkingAgentIdentifier" element
     */
    void removeLinkingAgentIdentifier(int i);
    
    /**
     * Gets array of all "linkingObjectIdentifier" elements
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] getLinkingObjectIdentifierArray();
    
    /**
     * Gets ith "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType getLinkingObjectIdentifierArray(int i);
    
    /**
     * Returns number of "linkingObjectIdentifier" element
     */
    int sizeOfLinkingObjectIdentifierArray();
    
    /**
     * Sets array of all "linkingObjectIdentifier" element
     */
    void setLinkingObjectIdentifierArray(gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] linkingObjectIdentifierArray);
    
    /**
     * Sets ith "linkingObjectIdentifier" element
     */
    void setLinkingObjectIdentifierArray(int i, gov.loc.premis.v3.LinkingObjectIdentifierComplexType linkingObjectIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType insertNewLinkingObjectIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType addNewLinkingObjectIdentifier();
    
    /**
     * Removes the ith "linkingObjectIdentifier" element
     */
    void removeLinkingObjectIdentifier(int i);
    
    /**
     * Gets the "xmlID" attribute
     */
    java.lang.String getXmlID();
    
    /**
     * Gets (as xml) the "xmlID" attribute
     */
    org.apache.xmlbeans.XmlID xgetXmlID();
    
    /**
     * True if has "xmlID" attribute
     */
    boolean isSetXmlID();
    
    /**
     * Sets the "xmlID" attribute
     */
    void setXmlID(java.lang.String xmlID);
    
    /**
     * Sets (as xml) the "xmlID" attribute
     */
    void xsetXmlID(org.apache.xmlbeans.XmlID xmlID);
    
    /**
     * Unsets the "xmlID" attribute
     */
    void unsetXmlID();
    
    /**
     * Gets the "version" attribute
     */
    gov.loc.premis.v3.Version3.Enum getVersion();
    
    /**
     * Gets (as xml) the "version" attribute
     */
    gov.loc.premis.v3.Version3 xgetVersion();
    
    /**
     * True if has "version" attribute
     */
    boolean isSetVersion();
    
    /**
     * Sets the "version" attribute
     */
    void setVersion(gov.loc.premis.v3.Version3.Enum version);
    
    /**
     * Sets (as xml) the "version" attribute
     */
    void xsetVersion(gov.loc.premis.v3.Version3 version);
    
    /**
     * Unsets the "version" attribute
     */
    void unsetVersion();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.EventComplexType newInstance() {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.EventComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.EventComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.EventComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.EventComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.EventComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.EventComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
