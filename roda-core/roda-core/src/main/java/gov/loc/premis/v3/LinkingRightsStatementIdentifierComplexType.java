/*
 * XML Type:  linkingRightsStatementIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML linkingRightsStatementIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface LinkingRightsStatementIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(LinkingRightsStatementIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("linkingrightsstatementidentifiercomplextypef018type");
    
    /**
     * Gets the "linkingRightsStatementIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingRightsStatementIdentifierType();
    
    /**
     * Sets the "linkingRightsStatementIdentifierType" element
     */
    void setLinkingRightsStatementIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingRightsStatementIdentifierType);
    
    /**
     * Appends and returns a new empty "linkingRightsStatementIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingRightsStatementIdentifierType();
    
    /**
     * Gets the "linkingRightsStatementIdentifierValue" element
     */
    java.lang.String getLinkingRightsStatementIdentifierValue();
    
    /**
     * Gets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetLinkingRightsStatementIdentifierValue();
    
    /**
     * Sets the "linkingRightsStatementIdentifierValue" element
     */
    void setLinkingRightsStatementIdentifierValue(java.lang.String linkingRightsStatementIdentifierValue);
    
    /**
     * Sets (as xml) the "linkingRightsStatementIdentifierValue" element
     */
    void xsetLinkingRightsStatementIdentifierValue(org.apache.xmlbeans.XmlString linkingRightsStatementIdentifierValue);
    
    /**
     * Gets the "LinkPermissionStatementXmlID" attribute
     */
    java.lang.String getLinkPermissionStatementXmlID();
    
    /**
     * Gets (as xml) the "LinkPermissionStatementXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetLinkPermissionStatementXmlID();
    
    /**
     * True if has "LinkPermissionStatementXmlID" attribute
     */
    boolean isSetLinkPermissionStatementXmlID();
    
    /**
     * Sets the "LinkPermissionStatementXmlID" attribute
     */
    void setLinkPermissionStatementXmlID(java.lang.String linkPermissionStatementXmlID);
    
    /**
     * Sets (as xml) the "LinkPermissionStatementXmlID" attribute
     */
    void xsetLinkPermissionStatementXmlID(org.apache.xmlbeans.XmlIDREF linkPermissionStatementXmlID);
    
    /**
     * Unsets the "LinkPermissionStatementXmlID" attribute
     */
    void unsetLinkPermissionStatementXmlID();
    
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
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
