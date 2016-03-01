/*
 * XML Type:  licenseInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML licenseInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface LicenseInformationComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(LicenseInformationComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("licenseinformationcomplextype04aatype");
    
    /**
     * Gets array of all "licenseDocumentationIdentifier" elements
     */
    gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[] getLicenseDocumentationIdentifierArray();
    
    /**
     * Gets ith "licenseDocumentationIdentifier" element
     */
    gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType getLicenseDocumentationIdentifierArray(int i);
    
    /**
     * Returns number of "licenseDocumentationIdentifier" element
     */
    int sizeOfLicenseDocumentationIdentifierArray();
    
    /**
     * Sets array of all "licenseDocumentationIdentifier" element
     */
    void setLicenseDocumentationIdentifierArray(gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[] licenseDocumentationIdentifierArray);
    
    /**
     * Sets ith "licenseDocumentationIdentifier" element
     */
    void setLicenseDocumentationIdentifierArray(int i, gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType licenseDocumentationIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "licenseDocumentationIdentifier" element
     */
    gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType insertNewLicenseDocumentationIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "licenseDocumentationIdentifier" element
     */
    gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType addNewLicenseDocumentationIdentifier();
    
    /**
     * Removes the ith "licenseDocumentationIdentifier" element
     */
    void removeLicenseDocumentationIdentifier(int i);
    
    /**
     * Gets the "licenseTerms" element
     */
    java.lang.String getLicenseTerms();
    
    /**
     * Gets (as xml) the "licenseTerms" element
     */
    org.apache.xmlbeans.XmlString xgetLicenseTerms();
    
    /**
     * True if has "licenseTerms" element
     */
    boolean isSetLicenseTerms();
    
    /**
     * Sets the "licenseTerms" element
     */
    void setLicenseTerms(java.lang.String licenseTerms);
    
    /**
     * Sets (as xml) the "licenseTerms" element
     */
    void xsetLicenseTerms(org.apache.xmlbeans.XmlString licenseTerms);
    
    /**
     * Unsets the "licenseTerms" element
     */
    void unsetLicenseTerms();
    
    /**
     * Gets array of all "licenseNote" elements
     */
    java.lang.String[] getLicenseNoteArray();
    
    /**
     * Gets ith "licenseNote" element
     */
    java.lang.String getLicenseNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "licenseNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetLicenseNoteArray();
    
    /**
     * Gets (as xml) ith "licenseNote" element
     */
    org.apache.xmlbeans.XmlString xgetLicenseNoteArray(int i);
    
    /**
     * Returns number of "licenseNote" element
     */
    int sizeOfLicenseNoteArray();
    
    /**
     * Sets array of all "licenseNote" element
     */
    void setLicenseNoteArray(java.lang.String[] licenseNoteArray);
    
    /**
     * Sets ith "licenseNote" element
     */
    void setLicenseNoteArray(int i, java.lang.String licenseNote);
    
    /**
     * Sets (as xml) array of all "licenseNote" element
     */
    void xsetLicenseNoteArray(org.apache.xmlbeans.XmlString[] licenseNoteArray);
    
    /**
     * Sets (as xml) ith "licenseNote" element
     */
    void xsetLicenseNoteArray(int i, org.apache.xmlbeans.XmlString licenseNote);
    
    /**
     * Inserts the value as the ith "licenseNote" element
     */
    void insertLicenseNote(int i, java.lang.String licenseNote);
    
    /**
     * Appends the value as the last "licenseNote" element
     */
    void addLicenseNote(java.lang.String licenseNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "licenseNote" element
     */
    org.apache.xmlbeans.XmlString insertNewLicenseNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "licenseNote" element
     */
    org.apache.xmlbeans.XmlString addNewLicenseNote();
    
    /**
     * Removes the ith "licenseNote" element
     */
    void removeLicenseNote(int i);
    
    /**
     * Gets the "licenseApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getLicenseApplicableDates();
    
    /**
     * True if has "licenseApplicableDates" element
     */
    boolean isSetLicenseApplicableDates();
    
    /**
     * Sets the "licenseApplicableDates" element
     */
    void setLicenseApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType licenseApplicableDates);
    
    /**
     * Appends and returns a new empty "licenseApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewLicenseApplicableDates();
    
    /**
     * Unsets the "licenseApplicableDates" element
     */
    void unsetLicenseApplicableDates();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.LicenseInformationComplexType newInstance() {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.LicenseInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.LicenseInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
