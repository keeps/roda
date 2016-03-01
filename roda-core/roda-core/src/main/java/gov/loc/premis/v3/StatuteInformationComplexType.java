/*
 * XML Type:  statuteInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML statuteInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface StatuteInformationComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(StatuteInformationComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("statuteinformationcomplextype8a79type");
    
    /**
     * Gets the "statuteJurisdiction" element
     */
    gov.loc.premis.v3.CountryCode getStatuteJurisdiction();
    
    /**
     * Sets the "statuteJurisdiction" element
     */
    void setStatuteJurisdiction(gov.loc.premis.v3.CountryCode statuteJurisdiction);
    
    /**
     * Appends and returns a new empty "statuteJurisdiction" element
     */
    gov.loc.premis.v3.CountryCode addNewStatuteJurisdiction();
    
    /**
     * Gets the "statuteCitation" element
     */
    gov.loc.premis.v3.StringPlusAuthority getStatuteCitation();
    
    /**
     * Sets the "statuteCitation" element
     */
    void setStatuteCitation(gov.loc.premis.v3.StringPlusAuthority statuteCitation);
    
    /**
     * Appends and returns a new empty "statuteCitation" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewStatuteCitation();
    
    /**
     * Gets the "statuteInformationDeterminationDate" element
     */
    java.lang.String getStatuteInformationDeterminationDate();
    
    /**
     * Gets (as xml) the "statuteInformationDeterminationDate" element
     */
    gov.loc.premis.v3.EdtfSimpleType xgetStatuteInformationDeterminationDate();
    
    /**
     * True if has "statuteInformationDeterminationDate" element
     */
    boolean isSetStatuteInformationDeterminationDate();
    
    /**
     * Sets the "statuteInformationDeterminationDate" element
     */
    void setStatuteInformationDeterminationDate(java.lang.String statuteInformationDeterminationDate);
    
    /**
     * Sets (as xml) the "statuteInformationDeterminationDate" element
     */
    void xsetStatuteInformationDeterminationDate(gov.loc.premis.v3.EdtfSimpleType statuteInformationDeterminationDate);
    
    /**
     * Unsets the "statuteInformationDeterminationDate" element
     */
    void unsetStatuteInformationDeterminationDate();
    
    /**
     * Gets array of all "statuteNote" elements
     */
    java.lang.String[] getStatuteNoteArray();
    
    /**
     * Gets ith "statuteNote" element
     */
    java.lang.String getStatuteNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "statuteNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetStatuteNoteArray();
    
    /**
     * Gets (as xml) ith "statuteNote" element
     */
    org.apache.xmlbeans.XmlString xgetStatuteNoteArray(int i);
    
    /**
     * Returns number of "statuteNote" element
     */
    int sizeOfStatuteNoteArray();
    
    /**
     * Sets array of all "statuteNote" element
     */
    void setStatuteNoteArray(java.lang.String[] statuteNoteArray);
    
    /**
     * Sets ith "statuteNote" element
     */
    void setStatuteNoteArray(int i, java.lang.String statuteNote);
    
    /**
     * Sets (as xml) array of all "statuteNote" element
     */
    void xsetStatuteNoteArray(org.apache.xmlbeans.XmlString[] statuteNoteArray);
    
    /**
     * Sets (as xml) ith "statuteNote" element
     */
    void xsetStatuteNoteArray(int i, org.apache.xmlbeans.XmlString statuteNote);
    
    /**
     * Inserts the value as the ith "statuteNote" element
     */
    void insertStatuteNote(int i, java.lang.String statuteNote);
    
    /**
     * Appends the value as the last "statuteNote" element
     */
    void addStatuteNote(java.lang.String statuteNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteNote" element
     */
    org.apache.xmlbeans.XmlString insertNewStatuteNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteNote" element
     */
    org.apache.xmlbeans.XmlString addNewStatuteNote();
    
    /**
     * Removes the ith "statuteNote" element
     */
    void removeStatuteNote(int i);
    
    /**
     * Gets array of all "statuteDocumentationIdentifier" elements
     */
    gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[] getStatuteDocumentationIdentifierArray();
    
    /**
     * Gets ith "statuteDocumentationIdentifier" element
     */
    gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType getStatuteDocumentationIdentifierArray(int i);
    
    /**
     * Returns number of "statuteDocumentationIdentifier" element
     */
    int sizeOfStatuteDocumentationIdentifierArray();
    
    /**
     * Sets array of all "statuteDocumentationIdentifier" element
     */
    void setStatuteDocumentationIdentifierArray(gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[] statuteDocumentationIdentifierArray);
    
    /**
     * Sets ith "statuteDocumentationIdentifier" element
     */
    void setStatuteDocumentationIdentifierArray(int i, gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType statuteDocumentationIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteDocumentationIdentifier" element
     */
    gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType insertNewStatuteDocumentationIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteDocumentationIdentifier" element
     */
    gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType addNewStatuteDocumentationIdentifier();
    
    /**
     * Removes the ith "statuteDocumentationIdentifier" element
     */
    void removeStatuteDocumentationIdentifier(int i);
    
    /**
     * Gets the "statuteApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getStatuteApplicableDates();
    
    /**
     * True if has "statuteApplicableDates" element
     */
    boolean isSetStatuteApplicableDates();
    
    /**
     * Sets the "statuteApplicableDates" element
     */
    void setStatuteApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType statuteApplicableDates);
    
    /**
     * Appends and returns a new empty "statuteApplicableDates" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewStatuteApplicableDates();
    
    /**
     * Unsets the "statuteApplicableDates" element
     */
    void unsetStatuteApplicableDates();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.StatuteInformationComplexType newInstance() {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.StatuteInformationComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.StatuteInformationComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
