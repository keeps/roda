/*
 * XML Type:  linkingEnvironmentIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML linkingEnvironmentIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface LinkingEnvironmentIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(LinkingEnvironmentIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("linkingenvironmentidentifiercomplextype6c93type");
    
    /**
     * Gets the "linkingEnvironmentIdentifierType" element
     */
    java.lang.String getLinkingEnvironmentIdentifierType();
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierType();
    
    /**
     * Sets the "linkingEnvironmentIdentifierType" element
     */
    void setLinkingEnvironmentIdentifierType(java.lang.String linkingEnvironmentIdentifierType);
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierType" element
     */
    void xsetLinkingEnvironmentIdentifierType(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierType);
    
    /**
     * Gets the "linkingEnvironmentIdentifierValue" element
     */
    java.lang.String getLinkingEnvironmentIdentifierValue();
    
    /**
     * Gets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetLinkingEnvironmentIdentifierValue();
    
    /**
     * Sets the "linkingEnvironmentIdentifierValue" element
     */
    void setLinkingEnvironmentIdentifierValue(java.lang.String linkingEnvironmentIdentifierValue);
    
    /**
     * Sets (as xml) the "linkingEnvironmentIdentifierValue" element
     */
    void xsetLinkingEnvironmentIdentifierValue(org.apache.xmlbeans.XmlString linkingEnvironmentIdentifierValue);
    
    /**
     * Gets array of all "linkingEnvironmentRole" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getLinkingEnvironmentRoleArray();
    
    /**
     * Gets ith "linkingEnvironmentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingEnvironmentRoleArray(int i);
    
    /**
     * Returns number of "linkingEnvironmentRole" element
     */
    int sizeOfLinkingEnvironmentRoleArray();
    
    /**
     * Sets array of all "linkingEnvironmentRole" element
     */
    void setLinkingEnvironmentRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingEnvironmentRoleArray);
    
    /**
     * Sets ith "linkingEnvironmentRole" element
     */
    void setLinkingEnvironmentRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingEnvironmentRole);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEnvironmentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewLinkingEnvironmentRole(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEnvironmentRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingEnvironmentRole();
    
    /**
     * Removes the ith "linkingEnvironmentRole" element
     */
    void removeLinkingEnvironmentRole(int i);
    
    /**
     * Gets the "LinkEventXmlID" attribute
     */
    java.lang.String getLinkEventXmlID();
    
    /**
     * Gets (as xml) the "LinkEventXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetLinkEventXmlID();
    
    /**
     * True if has "LinkEventXmlID" attribute
     */
    boolean isSetLinkEventXmlID();
    
    /**
     * Sets the "LinkEventXmlID" attribute
     */
    void setLinkEventXmlID(java.lang.String linkEventXmlID);
    
    /**
     * Sets (as xml) the "LinkEventXmlID" attribute
     */
    void xsetLinkEventXmlID(org.apache.xmlbeans.XmlIDREF linkEventXmlID);
    
    /**
     * Unsets the "LinkEventXmlID" attribute
     */
    void unsetLinkEventXmlID();
    
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
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
