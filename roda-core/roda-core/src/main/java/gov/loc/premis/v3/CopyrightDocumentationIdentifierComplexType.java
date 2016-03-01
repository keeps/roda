/*
 * XML Type:  copyrightDocumentationIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML copyrightDocumentationIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface CopyrightDocumentationIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(CopyrightDocumentationIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("copyrightdocumentationidentifiercomplextypeecfbtype");
    
    /**
     * Gets the "copyrightDocumentationIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationIdentifierType();
    
    /**
     * Sets the "copyrightDocumentationIdentifierType" element
     */
    void setCopyrightDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationIdentifierType);
    
    /**
     * Appends and returns a new empty "copyrightDocumentationIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationIdentifierType();
    
    /**
     * Gets the "copyrightDocumentationIdentifierValue" element
     */
    java.lang.String getCopyrightDocumentationIdentifierValue();
    
    /**
     * Gets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetCopyrightDocumentationIdentifierValue();
    
    /**
     * Sets the "copyrightDocumentationIdentifierValue" element
     */
    void setCopyrightDocumentationIdentifierValue(java.lang.String copyrightDocumentationIdentifierValue);
    
    /**
     * Sets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    void xsetCopyrightDocumentationIdentifierValue(org.apache.xmlbeans.XmlString copyrightDocumentationIdentifierValue);
    
    /**
     * Gets the "copyrightDocumentationRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationRole();
    
    /**
     * True if has "copyrightDocumentationRole" element
     */
    boolean isSetCopyrightDocumentationRole();
    
    /**
     * Sets the "copyrightDocumentationRole" element
     */
    void setCopyrightDocumentationRole(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationRole);
    
    /**
     * Appends and returns a new empty "copyrightDocumentationRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationRole();
    
    /**
     * Unsets the "copyrightDocumentationRole" element
     */
    void unsetCopyrightDocumentationRole();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
