/*
 * XML Type:  copyrightInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML copyrightInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface CopyrightInformationComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(CopyrightInformationComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("copyrightinformationcomplextypedd24type");
    
    /**
     * Gets the "copyrightStatus" element
     */
    gov.loc.premis.v3.StringPlusAuthority getCopyrightStatus();
    
    /**
     * Sets the "copyrightStatus" element
     */
    void setCopyrightStatus(gov.loc.premis.v3.StringPlusAuthority copyrightStatus);
    
    /**
     * Appends and returns a new empty "copyrightStatus" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewCopyrightStatus();
    
    /**
     * Gets the "copyrightJurisdiction" element
     */
    gov.loc.premis.v3.CountryCode getCopyrightJurisdiction();
    
    /**
     * Sets the "copyrightJurisdiction" element
     */
    void setCopyrightJurisdiction(gov.loc.premis.v3.CountryCode copyrightJurisdiction);
    
    /**
     * Appends and returns a new empty "copyrightJurisdiction" element
     */
    gov.loc.premis.v3.CountryCode addNewCopyrightJurisdiction();
    
    /**
     * Gets the "copyrightStatusDeterminationDate" element
     */
    java.lang.String getCopyrightStatusDeterminationDate();
    
    /**
     * Gets (as xml) the "copyrightStatusDeterminationDate" element
     */
    gov.loc.premis.v3.EdtfSimpleType xgetCopyrightStatusDeterminationDate();
    
    /**
     * True if has "copyrightStatusDeterminationDate" element
     */
    boolean isSetCopyrightStatusDeterminationDate();
    
    /**
     * Sets the "copyrightStatusDeterminationDate" element
     */
    void setCopyrightStatusDeterminationDate(java.lang.String copyrightStatusDeterminationDate);
    
    /**
     * Sets (as xml) the "copyrightStatusDeterminationDate" element
     */
    void xsetCopyrightStatusDeterminationDate(gov.loc.premis.v3.EdtfSimpleType copyrightStatusDeterminationDate);
    
    /**
     * Unsets the "copyrightStatusDeterminationDate" element
     */
    void unsetCopyrightStatusDeterminationDate();
    
    /**
     * Gets array of all "copyrightNote" elements
     */
    java.lang.String[] getCopyrightNoteArray();
    
    /**
     * Gets ith "copyrightNote" element
     */
    java.lang.String getCopyrightNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "copyrightNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetCopyrightNoteArray();
    
    /**
     * Gets (as xml) ith "copyrightNote" element
     */
    org.apache.xmlbeans.XmlString xgetCopyrightNoteArray(int i);
    
    /**
     * Returns number of "copyrightNote" element
     */
    int sizeOfCopyrightNoteArray();
    
    /**
     * Sets array of all "copyrightNote" element
     */
    void setCopyrightNoteArray(java.lang.String[] copyrightNoteArray);
    
    /**
     * Sets ith "copyrightNote" element
     */
    void setCopyrightNoteArray(int i, java.lang.String copyrightNote);
    
    /**
     * Sets (as xml) array of all "copyrightNote" element
     */
    void xsetCopyrightNoteArray(org.apache.xmlbeans.XmlString[] copyrightNoteArray);
    
    /**
     * Sets (as xml) ith "copyrightNote" element
     */
    void xsetCopyrightNoteArray(int i, org.apache.xmlbeans.XmlString copyrightNote);
    
    /**
     * Inserts the value as the ith "copyrightNote" element
     */
    void insertCopyrightNote(int i, java.lang.String copyrightNote);
    
    /**
     * Appends the value as the last "copyrightNote" element
     */
    void addCopyrightNote(java.lang.String copyrightNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "copyrightNote" element
     */
    org.apache.xmlbeans.XmlString insertNewCopyrightNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "copyrightNote" element
     */
    org.apache.xmlbeans.XmlString addNewCopyrightNote();
    
    /**
     * Removes the ith "copyrightNote" element
     */
    void removeCopyrightNote(int i);
    
    /**
     * Gets array of all "copyrightDocumentationIdentifier" elements
     */
    gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[] getCopyrightDocumentationIdentifierArray();
    
    /**
     * Gets ith "copyrightDocumentationIdentifier" element
     */
    gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType getCopyrightDocumentationIdentifierArray(int i);
    
    /**
     * Returns number of "copyrightDocumentationIdentifier" element
     */
    int sizeOfCopyrightDocumentationIdentifierArray();
    
    /**
     * Sets array of all "copyrightDocumentationIdentifier" element
     */
    void setCopyrightDocumentationIdentifierArray(gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[] copyrightDocumentationIdentifierArray);
    
    /**
     * Sets ith "copyrightDocumentationIdentifier" element
     */
    void setCopyrightDocumentationIdentifierArray(int i, gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType copyrightDocumentationIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "copyrightDocumentationIdentifier" element
     */
    gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType insertNewCopyrightDocumentationIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "copyrightDocumentationIdentifier" element
     */
    gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType addNewCopyrightDocumentationIdentifier();
    
    /**
     * Removes the ith "copyrightDocumentationIdentifier" element
     */
    void removeCopyrightDocumentationIdentifier(int i);
    
    /**
     * Gets the "copyrightApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getCopyrightApplicableDates();
    
    /**
     * True if has "copyrightApplicableDates" element
     */
    boolean isSetCopyrightApplicableDates();
    
    /**
     * Sets the "copyrightApplicableDates" element
     */
    void setCopyrightApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType copyrightApplicableDates);
    
    /**
     * Appends and returns a new empty "copyrightApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewCopyrightApplicableDates();
    
    /**
     * Unsets the "copyrightApplicableDates" element
     */
    void unsetCopyrightApplicableDates();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.CopyrightInformationComplexType newInstance() {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.CopyrightInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.CopyrightInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
