/*
 * XML Type:  rightsGrantedComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsGrantedComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML rightsGrantedComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RightsGrantedComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RightsGrantedComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("rightsgrantedcomplextype3e71type");
    
    /**
     * Gets the "act" element
     */
    gov.loc.premis.v3.StringPlusAuthority getAct();
    
    /**
     * Sets the "act" element
     */
    void setAct(gov.loc.premis.v3.StringPlusAuthority act);
    
    /**
     * Appends and returns a new empty "act" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewAct();
    
    /**
     * Gets array of all "restriction" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getRestrictionArray();
    
    /**
     * Gets ith "restriction" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRestrictionArray(int i);
    
    /**
     * Returns number of "restriction" element
     */
    int sizeOfRestrictionArray();
    
    /**
     * Sets array of all "restriction" element
     */
    void setRestrictionArray(gov.loc.premis.v3.StringPlusAuthority[] restrictionArray);
    
    /**
     * Sets ith "restriction" element
     */
    void setRestrictionArray(int i, gov.loc.premis.v3.StringPlusAuthority restriction);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "restriction" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewRestriction(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "restriction" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRestriction();
    
    /**
     * Removes the ith "restriction" element
     */
    void removeRestriction(int i);
    
    /**
     * Gets the "termOfGrant" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getTermOfGrant();
    
    /**
     * True if has "termOfGrant" element
     */
    boolean isSetTermOfGrant();
    
    /**
     * Sets the "termOfGrant" element
     */
    void setTermOfGrant(gov.loc.premis.v3.StartAndEndDateComplexType termOfGrant);
    
    /**
     * Appends and returns a new empty "termOfGrant" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfGrant();
    
    /**
     * Unsets the "termOfGrant" element
     */
    void unsetTermOfGrant();
    
    /**
     * Gets the "termOfRestriction" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType getTermOfRestriction();
    
    /**
     * True if has "termOfRestriction" element
     */
    boolean isSetTermOfRestriction();
    
    /**
     * Sets the "termOfRestriction" element
     */
    void setTermOfRestriction(gov.loc.premis.v3.StartAndEndDateComplexType termOfRestriction);
    
    /**
     * Appends and returns a new empty "termOfRestriction" element
     */
    gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfRestriction();
    
    /**
     * Unsets the "termOfRestriction" element
     */
    void unsetTermOfRestriction();
    
    /**
     * Gets array of all "rightsGrantedNote" elements
     */
    java.lang.String[] getRightsGrantedNoteArray();
    
    /**
     * Gets ith "rightsGrantedNote" element
     */
    java.lang.String getRightsGrantedNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "rightsGrantedNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetRightsGrantedNoteArray();
    
    /**
     * Gets (as xml) ith "rightsGrantedNote" element
     */
    org.apache.xmlbeans.XmlString xgetRightsGrantedNoteArray(int i);
    
    /**
     * Returns number of "rightsGrantedNote" element
     */
    int sizeOfRightsGrantedNoteArray();
    
    /**
     * Sets array of all "rightsGrantedNote" element
     */
    void setRightsGrantedNoteArray(java.lang.String[] rightsGrantedNoteArray);
    
    /**
     * Sets ith "rightsGrantedNote" element
     */
    void setRightsGrantedNoteArray(int i, java.lang.String rightsGrantedNote);
    
    /**
     * Sets (as xml) array of all "rightsGrantedNote" element
     */
    void xsetRightsGrantedNoteArray(org.apache.xmlbeans.XmlString[] rightsGrantedNoteArray);
    
    /**
     * Sets (as xml) ith "rightsGrantedNote" element
     */
    void xsetRightsGrantedNoteArray(int i, org.apache.xmlbeans.XmlString rightsGrantedNote);
    
    /**
     * Inserts the value as the ith "rightsGrantedNote" element
     */
    void insertRightsGrantedNote(int i, java.lang.String rightsGrantedNote);
    
    /**
     * Appends the value as the last "rightsGrantedNote" element
     */
    void addRightsGrantedNote(java.lang.String rightsGrantedNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsGrantedNote" element
     */
    org.apache.xmlbeans.XmlString insertNewRightsGrantedNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsGrantedNote" element
     */
    org.apache.xmlbeans.XmlString addNewRightsGrantedNote();
    
    /**
     * Removes the ith "rightsGrantedNote" element
     */
    void removeRightsGrantedNote(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.RightsGrantedComplexType newInstance() {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsGrantedComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsGrantedComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
