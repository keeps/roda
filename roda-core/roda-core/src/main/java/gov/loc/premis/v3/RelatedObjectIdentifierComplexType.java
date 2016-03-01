/*
 * XML Type:  relatedObjectIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML relatedObjectIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RelatedObjectIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RelatedObjectIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("relatedobjectidentifiercomplextypec160type");
    
    /**
     * Gets the "relatedObjectIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelatedObjectIdentifierType();
    
    /**
     * Sets the "relatedObjectIdentifierType" element
     */
    void setRelatedObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedObjectIdentifierType);
    
    /**
     * Appends and returns a new empty "relatedObjectIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelatedObjectIdentifierType();
    
    /**
     * Gets the "relatedObjectIdentifierValue" element
     */
    java.lang.String getRelatedObjectIdentifierValue();
    
    /**
     * Gets (as xml) the "relatedObjectIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetRelatedObjectIdentifierValue();
    
    /**
     * Sets the "relatedObjectIdentifierValue" element
     */
    void setRelatedObjectIdentifierValue(java.lang.String relatedObjectIdentifierValue);
    
    /**
     * Sets (as xml) the "relatedObjectIdentifierValue" element
     */
    void xsetRelatedObjectIdentifierValue(org.apache.xmlbeans.XmlString relatedObjectIdentifierValue);
    
    /**
     * Gets the "relatedObjectSequence" element
     */
    java.math.BigInteger getRelatedObjectSequence();
    
    /**
     * Gets (as xml) the "relatedObjectSequence" element
     */
    org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedObjectSequence();
    
    /**
     * True if has "relatedObjectSequence" element
     */
    boolean isSetRelatedObjectSequence();
    
    /**
     * Sets the "relatedObjectSequence" element
     */
    void setRelatedObjectSequence(java.math.BigInteger relatedObjectSequence);
    
    /**
     * Sets (as xml) the "relatedObjectSequence" element
     */
    void xsetRelatedObjectSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedObjectSequence);
    
    /**
     * Unsets the "relatedObjectSequence" element
     */
    void unsetRelatedObjectSequence();
    
    /**
     * Gets the "RelObjectXmlID" attribute
     */
    java.lang.String getRelObjectXmlID();
    
    /**
     * Gets (as xml) the "RelObjectXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetRelObjectXmlID();
    
    /**
     * True if has "RelObjectXmlID" attribute
     */
    boolean isSetRelObjectXmlID();
    
    /**
     * Sets the "RelObjectXmlID" attribute
     */
    void setRelObjectXmlID(java.lang.String relObjectXmlID);
    
    /**
     * Sets (as xml) the "RelObjectXmlID" attribute
     */
    void xsetRelObjectXmlID(org.apache.xmlbeans.XmlIDREF relObjectXmlID);
    
    /**
     * Unsets the "RelObjectXmlID" attribute
     */
    void unsetRelObjectXmlID();
    
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
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelatedObjectIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelatedObjectIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
