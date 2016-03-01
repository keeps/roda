/*
 * XML Type:  creatingApplicationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML creatingApplicationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface CreatingApplicationComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(CreatingApplicationComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("creatingapplicationcomplextype6e24type");
    
    /**
     * Gets the "creatingApplicationName" element
     */
    gov.loc.premis.v3.StringPlusAuthority getCreatingApplicationName();
    
    /**
     * True if has "creatingApplicationName" element
     */
    boolean isSetCreatingApplicationName();
    
    /**
     * Sets the "creatingApplicationName" element
     */
    void setCreatingApplicationName(gov.loc.premis.v3.StringPlusAuthority creatingApplicationName);
    
    /**
     * Appends and returns a new empty "creatingApplicationName" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewCreatingApplicationName();
    
    /**
     * Unsets the "creatingApplicationName" element
     */
    void unsetCreatingApplicationName();
    
    /**
     * Gets the "creatingApplicationVersion" element
     */
    java.lang.String getCreatingApplicationVersion();
    
    /**
     * Gets (as xml) the "creatingApplicationVersion" element
     */
    org.apache.xmlbeans.XmlString xgetCreatingApplicationVersion();
    
    /**
     * True if has "creatingApplicationVersion" element
     */
    boolean isSetCreatingApplicationVersion();
    
    /**
     * Sets the "creatingApplicationVersion" element
     */
    void setCreatingApplicationVersion(java.lang.String creatingApplicationVersion);
    
    /**
     * Sets (as xml) the "creatingApplicationVersion" element
     */
    void xsetCreatingApplicationVersion(org.apache.xmlbeans.XmlString creatingApplicationVersion);
    
    /**
     * Unsets the "creatingApplicationVersion" element
     */
    void unsetCreatingApplicationVersion();
    
    /**
     * Gets the "dateCreatedByApplication" element
     */
    java.lang.String getDateCreatedByApplication();
    
    /**
     * Gets (as xml) the "dateCreatedByApplication" element
     */
    gov.loc.premis.v3.EdtfSimpleType xgetDateCreatedByApplication();
    
    /**
     * True if has "dateCreatedByApplication" element
     */
    boolean isSetDateCreatedByApplication();
    
    /**
     * Sets the "dateCreatedByApplication" element
     */
    void setDateCreatedByApplication(java.lang.String dateCreatedByApplication);
    
    /**
     * Sets (as xml) the "dateCreatedByApplication" element
     */
    void xsetDateCreatedByApplication(gov.loc.premis.v3.EdtfSimpleType dateCreatedByApplication);
    
    /**
     * Unsets the "dateCreatedByApplication" element
     */
    void unsetDateCreatedByApplication();
    
    /**
     * Gets array of all "creatingApplicationExtension" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getCreatingApplicationExtensionArray();
    
    /**
     * Gets ith "creatingApplicationExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType getCreatingApplicationExtensionArray(int i);
    
    /**
     * Returns number of "creatingApplicationExtension" element
     */
    int sizeOfCreatingApplicationExtensionArray();
    
    /**
     * Sets array of all "creatingApplicationExtension" element
     */
    void setCreatingApplicationExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] creatingApplicationExtensionArray);
    
    /**
     * Sets ith "creatingApplicationExtension" element
     */
    void setCreatingApplicationExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType creatingApplicationExtension);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "creatingApplicationExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewCreatingApplicationExtension(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "creatingApplicationExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewCreatingApplicationExtension();
    
    /**
     * Removes the ith "creatingApplicationExtension" element
     */
    void removeCreatingApplicationExtension(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.CreatingApplicationComplexType newInstance() {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CreatingApplicationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CreatingApplicationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
