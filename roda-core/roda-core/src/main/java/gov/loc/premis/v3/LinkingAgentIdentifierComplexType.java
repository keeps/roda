/*
 * XML Type:  linkingAgentIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML linkingAgentIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface LinkingAgentIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(LinkingAgentIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("linkingagentidentifiercomplextype49c5type");
    
    /**
     * Gets the "linkingAgentIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingAgentIdentifierType();
    
    /**
     * Sets the "linkingAgentIdentifierType" element
     */
    void setLinkingAgentIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingAgentIdentifierType);
    
    /**
     * Appends and returns a new empty "linkingAgentIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentIdentifierType();
    
    /**
     * Gets the "linkingAgentIdentifierValue" element
     */
    java.lang.String getLinkingAgentIdentifierValue();
    
    /**
     * Gets (as xml) the "linkingAgentIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetLinkingAgentIdentifierValue();
    
    /**
     * Sets the "linkingAgentIdentifierValue" element
     */
    void setLinkingAgentIdentifierValue(java.lang.String linkingAgentIdentifierValue);
    
    /**
     * Sets (as xml) the "linkingAgentIdentifierValue" element
     */
    void xsetLinkingAgentIdentifierValue(org.apache.xmlbeans.XmlString linkingAgentIdentifierValue);
    
    /**
     * Gets array of all "linkingAgentRole" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getLinkingAgentRoleArray();
    
    /**
     * Gets ith "linkingAgentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingAgentRoleArray(int i);
    
    /**
     * Returns number of "linkingAgentRole" element
     */
    int sizeOfLinkingAgentRoleArray();
    
    /**
     * Sets array of all "linkingAgentRole" element
     */
    void setLinkingAgentRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingAgentRoleArray);
    
    /**
     * Sets ith "linkingAgentRole" element
     */
    void setLinkingAgentRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingAgentRole);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewLinkingAgentRole(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentRole();
    
    /**
     * Removes the ith "linkingAgentRole" element
     */
    void removeLinkingAgentRole(int i);
    
    /**
     * Gets the "LinkAgentXmlID" attribute
     */
    java.lang.String getLinkAgentXmlID();
    
    /**
     * Gets (as xml) the "LinkAgentXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetLinkAgentXmlID();
    
    /**
     * True if has "LinkAgentXmlID" attribute
     */
    boolean isSetLinkAgentXmlID();
    
    /**
     * Sets the "LinkAgentXmlID" attribute
     */
    void setLinkAgentXmlID(java.lang.String linkAgentXmlID);
    
    /**
     * Sets (as xml) the "LinkAgentXmlID" attribute
     */
    void xsetLinkAgentXmlID(org.apache.xmlbeans.XmlIDREF linkAgentXmlID);
    
    /**
     * Unsets the "LinkAgentXmlID" attribute
     */
    void unsetLinkAgentXmlID();
    
    /**
     * Gets the "simpleLink" attribute
     */
    java.lang.String getSimpleLink();
    
    /**
     * Gets (as xml) the "simpleLink" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetSimpleLink();
    
    /**
     * True if has "simpleLink" attribute
     */
    boolean isSetSimpleLink();
    
    /**
     * Sets the "simpleLink" attribute
     */
    void setSimpleLink(java.lang.String simpleLink);
    
    /**
     * Sets (as xml) the "simpleLink" attribute
     */
    void xsetSimpleLink(org.apache.xmlbeans.XmlAnyURI simpleLink);
    
    /**
     * Unsets the "simpleLink" attribute
     */
    void unsetSimpleLink();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingAgentIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingAgentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
