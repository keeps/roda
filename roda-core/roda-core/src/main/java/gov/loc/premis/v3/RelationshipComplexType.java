/*
 * XML Type:  relationshipComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelationshipComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML relationshipComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RelationshipComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RelationshipComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("relationshipcomplextypee677type");
    
    /**
     * Gets the "relationshipType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelationshipType();
    
    /**
     * Sets the "relationshipType" element
     */
    void setRelationshipType(gov.loc.premis.v3.StringPlusAuthority relationshipType);
    
    /**
     * Appends and returns a new empty "relationshipType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelationshipType();
    
    /**
     * Gets the "relationshipSubType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelationshipSubType();
    
    /**
     * Sets the "relationshipSubType" element
     */
    void setRelationshipSubType(gov.loc.premis.v3.StringPlusAuthority relationshipSubType);
    
    /**
     * Appends and returns a new empty "relationshipSubType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelationshipSubType();
    
    /**
     * Gets array of all "relatedObjectIdentifier" elements
     */
    gov.loc.premis.v3.RelatedObjectIdentifierComplexType[] getRelatedObjectIdentifierArray();
    
    /**
     * Gets ith "relatedObjectIdentifier" element
     */
    gov.loc.premis.v3.RelatedObjectIdentifierComplexType getRelatedObjectIdentifierArray(int i);
    
    /**
     * Returns number of "relatedObjectIdentifier" element
     */
    int sizeOfRelatedObjectIdentifierArray();
    
    /**
     * Sets array of all "relatedObjectIdentifier" element
     */
    void setRelatedObjectIdentifierArray(gov.loc.premis.v3.RelatedObjectIdentifierComplexType[] relatedObjectIdentifierArray);
    
    /**
     * Sets ith "relatedObjectIdentifier" element
     */
    void setRelatedObjectIdentifierArray(int i, gov.loc.premis.v3.RelatedObjectIdentifierComplexType relatedObjectIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedObjectIdentifier" element
     */
    gov.loc.premis.v3.RelatedObjectIdentifierComplexType insertNewRelatedObjectIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedObjectIdentifier" element
     */
    gov.loc.premis.v3.RelatedObjectIdentifierComplexType addNewRelatedObjectIdentifier();
    
    /**
     * Removes the ith "relatedObjectIdentifier" element
     */
    void removeRelatedObjectIdentifier(int i);
    
    /**
     * Gets array of all "relatedEventIdentifier" elements
     */
    gov.loc.premis.v3.RelatedEventIdentifierComplexType[] getRelatedEventIdentifierArray();
    
    /**
     * Gets ith "relatedEventIdentifier" element
     */
    gov.loc.premis.v3.RelatedEventIdentifierComplexType getRelatedEventIdentifierArray(int i);
    
    /**
     * Returns number of "relatedEventIdentifier" element
     */
    int sizeOfRelatedEventIdentifierArray();
    
    /**
     * Sets array of all "relatedEventIdentifier" element
     */
    void setRelatedEventIdentifierArray(gov.loc.premis.v3.RelatedEventIdentifierComplexType[] relatedEventIdentifierArray);
    
    /**
     * Sets ith "relatedEventIdentifier" element
     */
    void setRelatedEventIdentifierArray(int i, gov.loc.premis.v3.RelatedEventIdentifierComplexType relatedEventIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedEventIdentifier" element
     */
    gov.loc.premis.v3.RelatedEventIdentifierComplexType insertNewRelatedEventIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedEventIdentifier" element
     */
    gov.loc.premis.v3.RelatedEventIdentifierComplexType addNewRelatedEventIdentifier();
    
    /**
     * Removes the ith "relatedEventIdentifier" element
     */
    void removeRelatedEventIdentifier(int i);
    
    /**
     * Gets array of all "relatedEnvironmentPurpose" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getRelatedEnvironmentPurposeArray();
    
    /**
     * Gets ith "relatedEnvironmentPurpose" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentPurposeArray(int i);
    
    /**
     * Returns number of "relatedEnvironmentPurpose" element
     */
    int sizeOfRelatedEnvironmentPurposeArray();
    
    /**
     * Sets array of all "relatedEnvironmentPurpose" element
     */
    void setRelatedEnvironmentPurposeArray(gov.loc.premis.v3.StringPlusAuthority[] relatedEnvironmentPurposeArray);
    
    /**
     * Sets ith "relatedEnvironmentPurpose" element
     */
    void setRelatedEnvironmentPurposeArray(int i, gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentPurpose);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relatedEnvironmentPurpose" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewRelatedEnvironmentPurpose(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relatedEnvironmentPurpose" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelatedEnvironmentPurpose();
    
    /**
     * Removes the ith "relatedEnvironmentPurpose" element
     */
    void removeRelatedEnvironmentPurpose(int i);
    
    /**
     * Gets the "relatedEnvironmentCharacteristic" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRelatedEnvironmentCharacteristic();
    
    /**
     * True if has "relatedEnvironmentCharacteristic" element
     */
    boolean isSetRelatedEnvironmentCharacteristic();
    
    /**
     * Sets the "relatedEnvironmentCharacteristic" element
     */
    void setRelatedEnvironmentCharacteristic(gov.loc.premis.v3.StringPlusAuthority relatedEnvironmentCharacteristic);
    
    /**
     * Appends and returns a new empty "relatedEnvironmentCharacteristic" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRelatedEnvironmentCharacteristic();
    
    /**
     * Unsets the "relatedEnvironmentCharacteristic" element
     */
    void unsetRelatedEnvironmentCharacteristic();
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.RelationshipComplexType newInstance() {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RelationshipComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelationshipComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RelationshipComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RelationshipComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
