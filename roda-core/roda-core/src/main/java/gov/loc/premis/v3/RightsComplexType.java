/*
 * XML Type:  rightsComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML rightsComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RightsComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RightsComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("rightscomplextype09d6type");
    
    /**
     * Gets array of all "rightsStatement" elements
     */
    gov.loc.premis.v3.RightsStatementComplexType[] getRightsStatementArray();
    
    /**
     * Gets ith "rightsStatement" element
     */
    gov.loc.premis.v3.RightsStatementComplexType getRightsStatementArray(int i);
    
    /**
     * Returns number of "rightsStatement" element
     */
    int sizeOfRightsStatementArray();
    
    /**
     * Sets array of all "rightsStatement" element
     */
    void setRightsStatementArray(gov.loc.premis.v3.RightsStatementComplexType[] rightsStatementArray);
    
    /**
     * Sets ith "rightsStatement" element
     */
    void setRightsStatementArray(int i, gov.loc.premis.v3.RightsStatementComplexType rightsStatement);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsStatement" element
     */
    gov.loc.premis.v3.RightsStatementComplexType insertNewRightsStatement(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsStatement" element
     */
    gov.loc.premis.v3.RightsStatementComplexType addNewRightsStatement();
    
    /**
     * Removes the ith "rightsStatement" element
     */
    void removeRightsStatement(int i);
    
    /**
     * Gets array of all "rightsExtension" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getRightsExtensionArray();
    
    /**
     * Gets ith "rightsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType getRightsExtensionArray(int i);
    
    /**
     * Returns number of "rightsExtension" element
     */
    int sizeOfRightsExtensionArray();
    
    /**
     * Sets array of all "rightsExtension" element
     */
    void setRightsExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] rightsExtensionArray);
    
    /**
     * Sets ith "rightsExtension" element
     */
    void setRightsExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType rightsExtension);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewRightsExtension(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewRightsExtension();
    
    /**
     * Removes the ith "rightsExtension" element
     */
    void removeRightsExtension(int i);
    
    /**
     * Gets the "xmlID" attribute
     */
    java.lang.String getXmlID();
    
    /**
     * Gets (as xml) the "xmlID" attribute
     */
    org.apache.xmlbeans.XmlID xgetXmlID();
    
    /**
     * True if has "xmlID" attribute
     */
    boolean isSetXmlID();
    
    /**
     * Sets the "xmlID" attribute
     */
    void setXmlID(java.lang.String xmlID);
    
    /**
     * Sets (as xml) the "xmlID" attribute
     */
    void xsetXmlID(org.apache.xmlbeans.XmlID xmlID);
    
    /**
     * Unsets the "xmlID" attribute
     */
    void unsetXmlID();
    
    /**
     * Gets the "version" attribute
     */
    gov.loc.premis.v3.Version3.Enum getVersion();
    
    /**
     * Gets (as xml) the "version" attribute
     */
    gov.loc.premis.v3.Version3 xgetVersion();
    
    /**
     * True if has "version" attribute
     */
    boolean isSetVersion();
    
    /**
     * Sets the "version" attribute
     */
    void setVersion(gov.loc.premis.v3.Version3.Enum version);
    
    /**
     * Sets (as xml) the "version" attribute
     */
    void xsetVersion(gov.loc.premis.v3.Version3 version);
    
    /**
     * Unsets the "version" attribute
     */
    void unsetVersion();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.RightsComplexType newInstance() {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RightsComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RightsComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
