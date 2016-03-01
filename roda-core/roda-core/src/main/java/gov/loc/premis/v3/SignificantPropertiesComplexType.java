/*
 * XML Type:  significantPropertiesComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML significantPropertiesComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface SignificantPropertiesComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SignificantPropertiesComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("significantpropertiescomplextype1553type");
    
    /**
     * Gets the "significantPropertiesType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getSignificantPropertiesType();
    
    /**
     * True if has "significantPropertiesType" element
     */
    boolean isSetSignificantPropertiesType();
    
    /**
     * Sets the "significantPropertiesType" element
     */
    void setSignificantPropertiesType(gov.loc.premis.v3.StringPlusAuthority significantPropertiesType);
    
    /**
     * Appends and returns a new empty "significantPropertiesType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewSignificantPropertiesType();
    
    /**
     * Unsets the "significantPropertiesType" element
     */
    void unsetSignificantPropertiesType();
    
    /**
     * Gets the "significantPropertiesValue" element
     */
    java.lang.String getSignificantPropertiesValue();
    
    /**
     * Gets (as xml) the "significantPropertiesValue" element
     */
    org.apache.xmlbeans.XmlString xgetSignificantPropertiesValue();
    
    /**
     * True if has "significantPropertiesValue" element
     */
    boolean isSetSignificantPropertiesValue();
    
    /**
     * Sets the "significantPropertiesValue" element
     */
    void setSignificantPropertiesValue(java.lang.String significantPropertiesValue);
    
    /**
     * Sets (as xml) the "significantPropertiesValue" element
     */
    void xsetSignificantPropertiesValue(org.apache.xmlbeans.XmlString significantPropertiesValue);
    
    /**
     * Unsets the "significantPropertiesValue" element
     */
    void unsetSignificantPropertiesValue();
    
    /**
     * Gets array of all "significantPropertiesExtension" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getSignificantPropertiesExtensionArray();
    
    /**
     * Gets ith "significantPropertiesExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType getSignificantPropertiesExtensionArray(int i);
    
    /**
     * Returns number of "significantPropertiesExtension" element
     */
    int sizeOfSignificantPropertiesExtensionArray();
    
    /**
     * Sets array of all "significantPropertiesExtension" element
     */
    void setSignificantPropertiesExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] significantPropertiesExtensionArray);
    
    /**
     * Sets ith "significantPropertiesExtension" element
     */
    void setSignificantPropertiesExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType significantPropertiesExtension);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "significantPropertiesExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewSignificantPropertiesExtension(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "significantPropertiesExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewSignificantPropertiesExtension();
    
    /**
     * Removes the ith "significantPropertiesExtension" element
     */
    void removeSignificantPropertiesExtension(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.SignificantPropertiesComplexType newInstance() {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.SignificantPropertiesComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.SignificantPropertiesComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
