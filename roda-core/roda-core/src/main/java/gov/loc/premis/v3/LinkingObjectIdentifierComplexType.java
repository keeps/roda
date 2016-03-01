/*
 * XML Type:  linkingObjectIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML linkingObjectIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface LinkingObjectIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(LinkingObjectIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("linkingobjectidentifiercomplextypeb7ddtype");
    
    /**
     * Gets the "linkingObjectIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingObjectIdentifierType();
    
    /**
     * Sets the "linkingObjectIdentifierType" element
     */
    void setLinkingObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingObjectIdentifierType);
    
    /**
     * Appends and returns a new empty "linkingObjectIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectIdentifierType();
    
    /**
     * Gets the "linkingObjectIdentifierValue" element
     */
    java.lang.String getLinkingObjectIdentifierValue();
    
    /**
     * Gets (as xml) the "linkingObjectIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetLinkingObjectIdentifierValue();
    
    /**
     * Sets the "linkingObjectIdentifierValue" element
     */
    void setLinkingObjectIdentifierValue(java.lang.String linkingObjectIdentifierValue);
    
    /**
     * Sets (as xml) the "linkingObjectIdentifierValue" element
     */
    void xsetLinkingObjectIdentifierValue(org.apache.xmlbeans.XmlString linkingObjectIdentifierValue);
    
    /**
     * Gets array of all "linkingObjectRole" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getLinkingObjectRoleArray();
    
    /**
     * Gets ith "linkingObjectRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority getLinkingObjectRoleArray(int i);
    
    /**
     * Returns number of "linkingObjectRole" element
     */
    int sizeOfLinkingObjectRoleArray();
    
    /**
     * Sets array of all "linkingObjectRole" element
     */
    void setLinkingObjectRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingObjectRoleArray);
    
    /**
     * Sets ith "linkingObjectRole" element
     */
    void setLinkingObjectRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingObjectRole);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewLinkingObjectRole(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectRole();
    
    /**
     * Removes the ith "linkingObjectRole" element
     */
    void removeLinkingObjectRole(int i);
    
    /**
     * Gets the "LinkObjectXmlID" attribute
     */
    java.lang.String getLinkObjectXmlID();
    
    /**
     * Gets (as xml) the "LinkObjectXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetLinkObjectXmlID();
    
    /**
     * True if has "LinkObjectXmlID" attribute
     */
    boolean isSetLinkObjectXmlID();
    
    /**
     * Sets the "LinkObjectXmlID" attribute
     */
    void setLinkObjectXmlID(java.lang.String linkObjectXmlID);
    
    /**
     * Sets (as xml) the "LinkObjectXmlID" attribute
     */
    void xsetLinkObjectXmlID(org.apache.xmlbeans.XmlIDREF linkObjectXmlID);
    
    /**
     * Unsets the "LinkObjectXmlID" attribute
     */
    void unsetLinkObjectXmlID();
    
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
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LinkingObjectIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LinkingObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
