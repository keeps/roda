/*
 * XML Type:  stringPlusAuthority
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StringPlusAuthority
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML stringPlusAuthority(@http://www.loc.gov/premis/v3).
 *
 * This is an atomic type that is a restriction of gov.loc.premis.v3.StringPlusAuthority.
 */
public interface StringPlusAuthority extends org.apache.xmlbeans.XmlString
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(StringPlusAuthority.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("stringplusauthorityad31type");
    
    /**
     * Gets the "authority" attribute
     */
    java.lang.String getAuthority();
    
    /**
     * Gets (as xml) the "authority" attribute
     */
    org.apache.xmlbeans.XmlString xgetAuthority();
    
    /**
     * True if has "authority" attribute
     */
    boolean isSetAuthority();
    
    /**
     * Sets the "authority" attribute
     */
    void setAuthority(java.lang.String authority);
    
    /**
     * Sets (as xml) the "authority" attribute
     */
    void xsetAuthority(org.apache.xmlbeans.XmlString authority);
    
    /**
     * Unsets the "authority" attribute
     */
    void unsetAuthority();
    
    /**
     * Gets the "authorityURI" attribute
     */
    java.lang.String getAuthorityURI();
    
    /**
     * Gets (as xml) the "authorityURI" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetAuthorityURI();
    
    /**
     * True if has "authorityURI" attribute
     */
    boolean isSetAuthorityURI();
    
    /**
     * Sets the "authorityURI" attribute
     */
    void setAuthorityURI(java.lang.String authorityURI);
    
    /**
     * Sets (as xml) the "authorityURI" attribute
     */
    void xsetAuthorityURI(org.apache.xmlbeans.XmlAnyURI authorityURI);
    
    /**
     * Unsets the "authorityURI" attribute
     */
    void unsetAuthorityURI();
    
    /**
     * Gets the "valueURI" attribute
     */
    java.lang.String getValueURI();
    
    /**
     * Gets (as xml) the "valueURI" attribute
     */
    org.apache.xmlbeans.XmlAnyURI xgetValueURI();
    
    /**
     * True if has "valueURI" attribute
     */
    boolean isSetValueURI();
    
    /**
     * Sets the "valueURI" attribute
     */
    void setValueURI(java.lang.String valueURI);
    
    /**
     * Sets (as xml) the "valueURI" attribute
     */
    void xsetValueURI(org.apache.xmlbeans.XmlAnyURI valueURI);
    
    /**
     * Unsets the "valueURI" attribute
     */
    void unsetValueURI();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.StringPlusAuthority newInstance() {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.StringPlusAuthority parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.StringPlusAuthority parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.StringPlusAuthority parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.StringPlusAuthority) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
