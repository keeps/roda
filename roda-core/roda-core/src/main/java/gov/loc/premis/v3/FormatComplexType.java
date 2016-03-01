/*
 * XML Type:  formatComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML formatComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface FormatComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(FormatComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("formatcomplextypedff6type");
    
    /**
     * Gets the "formatDesignation" element
     */
    gov.loc.premis.v3.FormatDesignationComplexType getFormatDesignation();
    
    /**
     * True if has "formatDesignation" element
     */
    boolean isSetFormatDesignation();
    
    /**
     * Sets the "formatDesignation" element
     */
    void setFormatDesignation(gov.loc.premis.v3.FormatDesignationComplexType formatDesignation);
    
    /**
     * Appends and returns a new empty "formatDesignation" element
     */
    gov.loc.premis.v3.FormatDesignationComplexType addNewFormatDesignation();
    
    /**
     * Unsets the "formatDesignation" element
     */
    void unsetFormatDesignation();
    
    /**
     * Gets the "formatRegistry" element
     */
    gov.loc.premis.v3.FormatRegistryComplexType getFormatRegistry();
    
    /**
     * True if has "formatRegistry" element
     */
    boolean isSetFormatRegistry();
    
    /**
     * Sets the "formatRegistry" element
     */
    void setFormatRegistry(gov.loc.premis.v3.FormatRegistryComplexType formatRegistry);
    
    /**
     * Appends and returns a new empty "formatRegistry" element
     */
    gov.loc.premis.v3.FormatRegistryComplexType addNewFormatRegistry();
    
    /**
     * Unsets the "formatRegistry" element
     */
    void unsetFormatRegistry();
    
    /**
     * Gets array of all "formatNote" elements
     */
    java.lang.String[] getFormatNoteArray();
    
    /**
     * Gets ith "formatNote" element
     */
    java.lang.String getFormatNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "formatNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetFormatNoteArray();
    
    /**
     * Gets (as xml) ith "formatNote" element
     */
    org.apache.xmlbeans.XmlString xgetFormatNoteArray(int i);
    
    /**
     * Returns number of "formatNote" element
     */
    int sizeOfFormatNoteArray();
    
    /**
     * Sets array of all "formatNote" element
     */
    void setFormatNoteArray(java.lang.String[] formatNoteArray);
    
    /**
     * Sets ith "formatNote" element
     */
    void setFormatNoteArray(int i, java.lang.String formatNote);
    
    /**
     * Sets (as xml) array of all "formatNote" element
     */
    void xsetFormatNoteArray(org.apache.xmlbeans.XmlString[] formatNoteArray);
    
    /**
     * Sets (as xml) ith "formatNote" element
     */
    void xsetFormatNoteArray(int i, org.apache.xmlbeans.XmlString formatNote);
    
    /**
     * Inserts the value as the ith "formatNote" element
     */
    void insertFormatNote(int i, java.lang.String formatNote);
    
    /**
     * Appends the value as the last "formatNote" element
     */
    void addFormatNote(java.lang.String formatNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "formatNote" element
     */
    org.apache.xmlbeans.XmlString insertNewFormatNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "formatNote" element
     */
    org.apache.xmlbeans.XmlString addNewFormatNote();
    
    /**
     * Removes the ith "formatNote" element
     */
    void removeFormatNote(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.FormatComplexType newInstance() {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.FormatComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.FormatComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.FormatComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.FormatComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.FormatComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
