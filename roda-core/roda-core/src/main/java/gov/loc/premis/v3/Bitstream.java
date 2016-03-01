/*
 * XML Type:  bitstream
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.Bitstream
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML bitstream(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface Bitstream extends gov.loc.premis.v3.ObjectComplexType
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(Bitstream.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("bitstream3bfctype");
    
    /**
     * Gets array of all "objectIdentifier" elements
     */
    gov.loc.premis.v3.ObjectIdentifierComplexType[] getObjectIdentifierArray();
    
    /**
     * Gets ith "objectIdentifier" element
     */
    gov.loc.premis.v3.ObjectIdentifierComplexType getObjectIdentifierArray(int i);
    
    /**
     * Returns number of "objectIdentifier" element
     */
    int sizeOfObjectIdentifierArray();
    
    /**
     * Sets array of all "objectIdentifier" element
     */
    void setObjectIdentifierArray(gov.loc.premis.v3.ObjectIdentifierComplexType[] objectIdentifierArray);
    
    /**
     * Sets ith "objectIdentifier" element
     */
    void setObjectIdentifierArray(int i, gov.loc.premis.v3.ObjectIdentifierComplexType objectIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectIdentifier" element
     */
    gov.loc.premis.v3.ObjectIdentifierComplexType insertNewObjectIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectIdentifier" element
     */
    gov.loc.premis.v3.ObjectIdentifierComplexType addNewObjectIdentifier();
    
    /**
     * Removes the ith "objectIdentifier" element
     */
    void removeObjectIdentifier(int i);
    
    /**
     * Gets array of all "significantProperties" elements
     */
    gov.loc.premis.v3.SignificantPropertiesComplexType[] getSignificantPropertiesArray();
    
    /**
     * Gets ith "significantProperties" element
     */
    gov.loc.premis.v3.SignificantPropertiesComplexType getSignificantPropertiesArray(int i);
    
    /**
     * Returns number of "significantProperties" element
     */
    int sizeOfSignificantPropertiesArray();
    
    /**
     * Sets array of all "significantProperties" element
     */
    void setSignificantPropertiesArray(gov.loc.premis.v3.SignificantPropertiesComplexType[] significantPropertiesArray);
    
    /**
     * Sets ith "significantProperties" element
     */
    void setSignificantPropertiesArray(int i, gov.loc.premis.v3.SignificantPropertiesComplexType significantProperties);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "significantProperties" element
     */
    gov.loc.premis.v3.SignificantPropertiesComplexType insertNewSignificantProperties(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "significantProperties" element
     */
    gov.loc.premis.v3.SignificantPropertiesComplexType addNewSignificantProperties();
    
    /**
     * Removes the ith "significantProperties" element
     */
    void removeSignificantProperties(int i);
    
    /**
     * Gets array of all "objectCharacteristics" elements
     */
    gov.loc.premis.v3.ObjectCharacteristicsComplexType[] getObjectCharacteristicsArray();
    
    /**
     * Gets ith "objectCharacteristics" element
     */
    gov.loc.premis.v3.ObjectCharacteristicsComplexType getObjectCharacteristicsArray(int i);
    
    /**
     * Returns number of "objectCharacteristics" element
     */
    int sizeOfObjectCharacteristicsArray();
    
    /**
     * Sets array of all "objectCharacteristics" element
     */
    void setObjectCharacteristicsArray(gov.loc.premis.v3.ObjectCharacteristicsComplexType[] objectCharacteristicsArray);
    
    /**
     * Sets ith "objectCharacteristics" element
     */
    void setObjectCharacteristicsArray(int i, gov.loc.premis.v3.ObjectCharacteristicsComplexType objectCharacteristics);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectCharacteristics" element
     */
    gov.loc.premis.v3.ObjectCharacteristicsComplexType insertNewObjectCharacteristics(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectCharacteristics" element
     */
    gov.loc.premis.v3.ObjectCharacteristicsComplexType addNewObjectCharacteristics();
    
    /**
     * Removes the ith "objectCharacteristics" element
     */
    void removeObjectCharacteristics(int i);
    
    /**
     * Gets array of all "storage" elements
     */
    gov.loc.premis.v3.StorageComplexType[] getStorageArray();
    
    /**
     * Gets ith "storage" element
     */
    gov.loc.premis.v3.StorageComplexType getStorageArray(int i);
    
    /**
     * Returns number of "storage" element
     */
    int sizeOfStorageArray();
    
    /**
     * Sets array of all "storage" element
     */
    void setStorageArray(gov.loc.premis.v3.StorageComplexType[] storageArray);
    
    /**
     * Sets ith "storage" element
     */
    void setStorageArray(int i, gov.loc.premis.v3.StorageComplexType storage);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "storage" element
     */
    gov.loc.premis.v3.StorageComplexType insertNewStorage(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "storage" element
     */
    gov.loc.premis.v3.StorageComplexType addNewStorage();
    
    /**
     * Removes the ith "storage" element
     */
    void removeStorage(int i);
    
    /**
     * Gets array of all "signatureInformation" elements
     */
    gov.loc.premis.v3.SignatureInformationComplexType[] getSignatureInformationArray();
    
    /**
     * Gets ith "signatureInformation" element
     */
    gov.loc.premis.v3.SignatureInformationComplexType getSignatureInformationArray(int i);
    
    /**
     * Returns number of "signatureInformation" element
     */
    int sizeOfSignatureInformationArray();
    
    /**
     * Sets array of all "signatureInformation" element
     */
    void setSignatureInformationArray(gov.loc.premis.v3.SignatureInformationComplexType[] signatureInformationArray);
    
    /**
     * Sets ith "signatureInformation" element
     */
    void setSignatureInformationArray(int i, gov.loc.premis.v3.SignatureInformationComplexType signatureInformation);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "signatureInformation" element
     */
    gov.loc.premis.v3.SignatureInformationComplexType insertNewSignatureInformation(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "signatureInformation" element
     */
    gov.loc.premis.v3.SignatureInformationComplexType addNewSignatureInformation();
    
    /**
     * Removes the ith "signatureInformation" element
     */
    void removeSignatureInformation(int i);
    
    /**
     * Gets array of all "relationship" elements
     */
    gov.loc.premis.v3.RelationshipComplexType[] getRelationshipArray();
    
    /**
     * Gets ith "relationship" element
     */
    gov.loc.premis.v3.RelationshipComplexType getRelationshipArray(int i);
    
    /**
     * Returns number of "relationship" element
     */
    int sizeOfRelationshipArray();
    
    /**
     * Sets array of all "relationship" element
     */
    void setRelationshipArray(gov.loc.premis.v3.RelationshipComplexType[] relationshipArray);
    
    /**
     * Sets ith "relationship" element
     */
    void setRelationshipArray(int i, gov.loc.premis.v3.RelationshipComplexType relationship);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "relationship" element
     */
    gov.loc.premis.v3.RelationshipComplexType insertNewRelationship(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "relationship" element
     */
    gov.loc.premis.v3.RelationshipComplexType addNewRelationship();
    
    /**
     * Removes the ith "relationship" element
     */
    void removeRelationship(int i);
    
    /**
     * Gets array of all "linkingEventIdentifier" elements
     */
    gov.loc.premis.v3.LinkingEventIdentifierComplexType[] getLinkingEventIdentifierArray();
    
    /**
     * Gets ith "linkingEventIdentifier" element
     */
    gov.loc.premis.v3.LinkingEventIdentifierComplexType getLinkingEventIdentifierArray(int i);
    
    /**
     * Returns number of "linkingEventIdentifier" element
     */
    int sizeOfLinkingEventIdentifierArray();
    
    /**
     * Sets array of all "linkingEventIdentifier" element
     */
    void setLinkingEventIdentifierArray(gov.loc.premis.v3.LinkingEventIdentifierComplexType[] linkingEventIdentifierArray);
    
    /**
     * Sets ith "linkingEventIdentifier" element
     */
    void setLinkingEventIdentifierArray(int i, gov.loc.premis.v3.LinkingEventIdentifierComplexType linkingEventIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEventIdentifier" element
     */
    gov.loc.premis.v3.LinkingEventIdentifierComplexType insertNewLinkingEventIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEventIdentifier" element
     */
    gov.loc.premis.v3.LinkingEventIdentifierComplexType addNewLinkingEventIdentifier();
    
    /**
     * Removes the ith "linkingEventIdentifier" element
     */
    void removeLinkingEventIdentifier(int i);
    
    /**
     * Gets array of all "linkingRightsStatementIdentifier" elements
     */
    gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] getLinkingRightsStatementIdentifierArray();
    
    /**
     * Gets ith "linkingRightsStatementIdentifier" element
     */
    gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType getLinkingRightsStatementIdentifierArray(int i);
    
    /**
     * Returns number of "linkingRightsStatementIdentifier" element
     */
    int sizeOfLinkingRightsStatementIdentifierArray();
    
    /**
     * Sets array of all "linkingRightsStatementIdentifier" element
     */
    void setLinkingRightsStatementIdentifierArray(gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] linkingRightsStatementIdentifierArray);
    
    /**
     * Sets ith "linkingRightsStatementIdentifier" element
     */
    void setLinkingRightsStatementIdentifierArray(int i, gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType linkingRightsStatementIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingRightsStatementIdentifier" element
     */
    gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType insertNewLinkingRightsStatementIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingRightsStatementIdentifier" element
     */
    gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType addNewLinkingRightsStatementIdentifier();
    
    /**
     * Removes the ith "linkingRightsStatementIdentifier" element
     */
    void removeLinkingRightsStatementIdentifier(int i);
    
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
        public static gov.loc.premis.v3.Bitstream newInstance() {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.Bitstream newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.Bitstream parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.Bitstream parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.Bitstream parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.Bitstream parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.Bitstream parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.Bitstream parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.Bitstream parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.Bitstream) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
