/*
 * XML Type:  relatedEventIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML relatedEventIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RelatedEventIdentifierComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RelatedEventIdentifierComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("relatedeventidentifiercomplextype4437type");
    
    /**
     * Gets the "relatedEventIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelatedEventIdentifierType();
    
    /**
     * Sets the "relatedEventIdentifierType" element
     */
    void setRelatedEventIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedEventIdentifierType);
    
    /**
     * Appends and returns a new empty "relatedEventIdentifierType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelatedEventIdentifierType();
    
    /**
     * Gets the "relatedEventIdentifierValue" element
     */
    java.lang.String getRelatedEventIdentifierValue();
    
    /**
     * Gets (as xml) the "relatedEventIdentifierValue" element
     */
    org.apache.xmlbeans.XmlString xgetRelatedEventIdentifierValue();
    
    /**
     * Sets the "relatedEventIdentifierValue" element
     */
    void setRelatedEventIdentifierValue(java.lang.String relatedEventIdentifierValue);
    
    /**
     * Sets (as xml) the "relatedEventIdentifierValue" element
     */
    void xsetRelatedEventIdentifierValue(org.apache.xmlbeans.XmlString relatedEventIdentifierValue);
    
    /**
     * Gets the "relatedEventSequence" element
     */
    java.math.BigInteger getRelatedEventSequence();
    
    /**
     * Gets (as xml) the "relatedEventSequence" element
     */
    org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedEventSequence();
    
    /**
     * True if has "relatedEventSequence" element
     */
    boolean isSetRelatedEventSequence();
    
    /**
     * Sets the "relatedEventSequence" element
     */
    void setRelatedEventSequence(java.math.BigInteger relatedEventSequence);
    
    /**
     * Sets (as xml) the "relatedEventSequence" element
     */
    void xsetRelatedEventSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedEventSequence);
    
    /**
     * Unsets the "relatedEventSequence" element
     */
    void unsetRelatedEventSequence();
    
    /**
     * Gets the "RelEventXmlID" attribute
     */
    java.lang.String getRelEventXmlID();
    
    /**
     * Gets (as xml) the "RelEventXmlID" attribute
     */
    org.apache.xmlbeans.XmlIDREF xgetRelEventXmlID();
    
    /**
     * True if has "RelEventXmlID" attribute
     */
    boolean isSetRelEventXmlID();
    
    /**
     * Sets the "RelEventXmlID" attribute
     */
    void setRelEventXmlID(java.lang.String relEventXmlID);
    
    /**
     * Sets (as xml) the "RelEventXmlID" attribute
     */
    void xsetRelEventXmlID(org.apache.xmlbeans.XmlIDREF relEventXmlID);
    
    /**
     * Unsets the "RelEventXmlID" attribute
     */
    void unsetRelEventXmlID();
    
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
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType newInstance() {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelatedEventIdentifierComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelatedEventIdentifierComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
