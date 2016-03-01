/*
 * XML Type:  otherRightsInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML otherRightsInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface OtherRightsInformationComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(OtherRightsInformationComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("otherrightsinformationcomplextype51a4type");
    
    /**
     * Gets array of all "otherRightsDocumentationIdentifier" elements
     */
    gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[] getOtherRightsDocumentationIdentifierArray();
    
    /**
     * Gets ith "otherRightsDocumentationIdentifier" element
     */
    gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType getOtherRightsDocumentationIdentifierArray(int i);
    
    /**
     * Returns number of "otherRightsDocumentationIdentifier" element
     */
    int sizeOfOtherRightsDocumentationIdentifierArray();
    
    /**
     * Sets array of all "otherRightsDocumentationIdentifier" element
     */
    void setOtherRightsDocumentationIdentifierArray(gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[] otherRightsDocumentationIdentifierArray);
    
    /**
     * Sets ith "otherRightsDocumentationIdentifier" element
     */
    void setOtherRightsDocumentationIdentifierArray(int i, gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType otherRightsDocumentationIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "otherRightsDocumentationIdentifier" element
     */
    gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType insertNewOtherRightsDocumentationIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "otherRightsDocumentationIdentifier" element
     */
    gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType addNewOtherRightsDocumentationIdentifier();
    
    /**
     * Removes the ith "otherRightsDocumentationIdentifier" element
     */
    void removeOtherRightsDocumentationIdentifier(int i);
    
    /**
     * Gets the "otherRightsBasis" element
     */
    gov.loc.premis.v3.StringPlusAuthority getOtherRightsBasis();
    
    /**
     * Sets the "otherRightsBasis" element
     */
    void setOtherRightsBasis(gov.loc.premis.v3.StringPlusAuthority otherRightsBasis);
    
    /**
     * Appends and returns a new empty "otherRightsBasis" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsBasis();
    
    /**
     * Gets the "otherRightsApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getOtherRightsApplicableDates();
    
    /**
     * True if has "otherRightsApplicableDates" element
     */
    boolean isSetOtherRightsApplicableDates();
    
    /**
     * Sets the "otherRightsApplicableDates" element
     */
    void setOtherRightsApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType otherRightsApplicableDates);
    
    /**
     * Appends and returns a new empty "otherRightsApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewOtherRightsApplicableDates();
    
    /**
     * Unsets the "otherRightsApplicableDates" element
     */
    void unsetOtherRightsApplicableDates();
    
    /**
     * Gets array of all "otherRightsNote" elements
     */
    java.lang.String[] getOtherRightsNoteArray();
    
    /**
     * Gets ith "otherRightsNote" element
     */
    java.lang.String getOtherRightsNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "otherRightsNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetOtherRightsNoteArray();
    
    /**
     * Gets (as xml) ith "otherRightsNote" element
     */
    org.apache.xmlbeans.XmlString xgetOtherRightsNoteArray(int i);
    
    /**
     * Returns number of "otherRightsNote" element
     */
    int sizeOfOtherRightsNoteArray();
    
    /**
     * Sets array of all "otherRightsNote" element
     */
    void setOtherRightsNoteArray(java.lang.String[] otherRightsNoteArray);
    
    /**
     * Sets ith "otherRightsNote" element
     */
    void setOtherRightsNoteArray(int i, java.lang.String otherRightsNote);
    
    /**
     * Sets (as xml) array of all "otherRightsNote" element
     */
    void xsetOtherRightsNoteArray(org.apache.xmlbeans.XmlString[] otherRightsNoteArray);
    
    /**
     * Sets (as xml) ith "otherRightsNote" element
     */
    void xsetOtherRightsNoteArray(int i, org.apache.xmlbeans.XmlString otherRightsNote);
    
    /**
     * Inserts the value as the ith "otherRightsNote" element
     */
    void insertOtherRightsNote(int i, java.lang.String otherRightsNote);
    
    /**
     * Appends the value as the last "otherRightsNote" element
     */
    void addOtherRightsNote(java.lang.String otherRightsNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "otherRightsNote" element
     */
    org.apache.xmlbeans.XmlString insertNewOtherRightsNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "otherRightsNote" element
     */
    org.apache.xmlbeans.XmlString addNewOtherRightsNote();
    
    /**
     * Removes the ith "otherRightsNote" element
     */
    void removeOtherRightsNote(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.OtherRightsInformationComplexType newInstance() {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.OtherRightsInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.OtherRightsInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
