/*
 * XML Type:  preservationLevelComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML preservationLevelComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface PreservationLevelComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(PreservationLevelComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("preservationlevelcomplextypec195type");
    
    /**
     * Gets the "preservationLevelType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getPreservationLevelType();
    
    /**
     * True if has "preservationLevelType" element
     */
    boolean isSetPreservationLevelType();
    
    /**
     * Sets the "preservationLevelType" element
     */
    void setPreservationLevelType(gov.loc.premis.v3.StringPlusAuthority preservationLevelType);
    
    /**
     * Appends and returns a new empty "preservationLevelType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelType();
    
    /**
     * Unsets the "preservationLevelType" element
     */
    void unsetPreservationLevelType();
    
    /**
     * Gets the "preservationLevelValue" element
     */
    gov.loc.premis.v3.StringPlusAuthority getPreservationLevelValue();
    
    /**
     * Sets the "preservationLevelValue" element
     */
    void setPreservationLevelValue(gov.loc.premis.v3.StringPlusAuthority preservationLevelValue);
    
    /**
     * Appends and returns a new empty "preservationLevelValue" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelValue();
    
    /**
     * Gets the "preservationLevelRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority getPreservationLevelRole();
    
    /**
     * True if has "preservationLevelRole" element
     */
    boolean isSetPreservationLevelRole();
    
    /**
     * Sets the "preservationLevelRole" element
     */
    void setPreservationLevelRole(gov.loc.premis.v3.StringPlusAuthority preservationLevelRole);
    
    /**
     * Appends and returns a new empty "preservationLevelRole" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelRole();
    
    /**
     * Unsets the "preservationLevelRole" element
     */
    void unsetPreservationLevelRole();
    
    /**
     * Gets array of all "preservationLevelRationale" elements
     */
    java.lang.String[] getPreservationLevelRationaleArray();
    
    /**
     * Gets ith "preservationLevelRationale" element
     */
    java.lang.String getPreservationLevelRationaleArray(int i);
    
    /**
     * Gets (as xml) array of all "preservationLevelRationale" elements
     */
    org.apache.xmlbeans.XmlString[] xgetPreservationLevelRationaleArray();
    
    /**
     * Gets (as xml) ith "preservationLevelRationale" element
     */
    org.apache.xmlbeans.XmlString xgetPreservationLevelRationaleArray(int i);
    
    /**
     * Returns number of "preservationLevelRationale" element
     */
    int sizeOfPreservationLevelRationaleArray();
    
    /**
     * Sets array of all "preservationLevelRationale" element
     */
    void setPreservationLevelRationaleArray(java.lang.String[] preservationLevelRationaleArray);
    
    /**
     * Sets ith "preservationLevelRationale" element
     */
    void setPreservationLevelRationaleArray(int i, java.lang.String preservationLevelRationale);
    
    /**
     * Sets (as xml) array of all "preservationLevelRationale" element
     */
    void xsetPreservationLevelRationaleArray(org.apache.xmlbeans.XmlString[] preservationLevelRationaleArray);
    
    /**
     * Sets (as xml) ith "preservationLevelRationale" element
     */
    void xsetPreservationLevelRationaleArray(int i, org.apache.xmlbeans.XmlString preservationLevelRationale);
    
    /**
     * Inserts the value as the ith "preservationLevelRationale" element
     */
    void insertPreservationLevelRationale(int i, java.lang.String preservationLevelRationale);
    
    /**
     * Appends the value as the last "preservationLevelRationale" element
     */
    void addPreservationLevelRationale(java.lang.String preservationLevelRationale);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "preservationLevelRationale" element
     */
    org.apache.xmlbeans.XmlString insertNewPreservationLevelRationale(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "preservationLevelRationale" element
     */
    org.apache.xmlbeans.XmlString addNewPreservationLevelRationale();
    
    /**
     * Removes the ith "preservationLevelRationale" element
     */
    void removePreservationLevelRationale(int i);
    
    /**
     * Gets the "preservationLevelDateAssigned" element
     */
    java.lang.String getPreservationLevelDateAssigned();
    
    /**
     * Gets (as xml) the "preservationLevelDateAssigned" element
     */
    gov.loc.premis.v3.EdtfSimpleType xgetPreservationLevelDateAssigned();
    
    /**
     * True if has "preservationLevelDateAssigned" element
     */
    boolean isSetPreservationLevelDateAssigned();
    
    /**
     * Sets the "preservationLevelDateAssigned" element
     */
    void setPreservationLevelDateAssigned(java.lang.String preservationLevelDateAssigned);
    
    /**
     * Sets (as xml) the "preservationLevelDateAssigned" element
     */
    void xsetPreservationLevelDateAssigned(gov.loc.premis.v3.EdtfSimpleType preservationLevelDateAssigned);
    
    /**
     * Unsets the "preservationLevelDateAssigned" element
     */
    void unsetPreservationLevelDateAssigned();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.PreservationLevelComplexType newInstance() {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.PreservationLevelComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.PreservationLevelComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
