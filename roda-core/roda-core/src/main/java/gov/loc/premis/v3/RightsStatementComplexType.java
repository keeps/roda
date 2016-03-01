/*
 * XML Type:  rightsStatementComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML rightsStatementComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface RightsStatementComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(RightsStatementComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("rightsstatementcomplextype9525type");
    
    /**
     * Gets the "rightsStatementIdentifier" element
     */
    gov.loc.premis.v3.RightsStatementIdentifierComplexType getRightsStatementIdentifier();
    
    /**
     * Sets the "rightsStatementIdentifier" element
     */
    void setRightsStatementIdentifier(gov.loc.premis.v3.RightsStatementIdentifierComplexType rightsStatementIdentifier);
    
    /**
     * Appends and returns a new empty "rightsStatementIdentifier" element
     */
    gov.loc.premis.v3.RightsStatementIdentifierComplexType addNewRightsStatementIdentifier();
    
    /**
     * Gets the "rightsBasis" element
     */
    gov.loc.premis.v3.StringPlusAuthority getRightsBasis();
    
    /**
     * Sets the "rightsBasis" element
     */
    void setRightsBasis(gov.loc.premis.v3.StringPlusAuthority rightsBasis);
    
    /**
     * Appends and returns a new empty "rightsBasis" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewRightsBasis();
    
    /**
     * Gets the "copyrightInformation" element
     */
    gov.loc.premis.v3.CopyrightInformationComplexType getCopyrightInformation();
    
    /**
     * True if has "copyrightInformation" element
     */
    boolean isSetCopyrightInformation();
    
    /**
     * Sets the "copyrightInformation" element
     */
    void setCopyrightInformation(gov.loc.premis.v3.CopyrightInformationComplexType copyrightInformation);
    
    /**
     * Appends and returns a new empty "copyrightInformation" element
     */
    gov.loc.premis.v3.CopyrightInformationComplexType addNewCopyrightInformation();
    
    /**
     * Unsets the "copyrightInformation" element
     */
    void unsetCopyrightInformation();
    
    /**
     * Gets the "licenseInformation" element
     */
    gov.loc.premis.v3.LicenseInformationComplexType getLicenseInformation();
    
    /**
     * True if has "licenseInformation" element
     */
    boolean isSetLicenseInformation();
    
    /**
     * Sets the "licenseInformation" element
     */
    void setLicenseInformation(gov.loc.premis.v3.LicenseInformationComplexType licenseInformation);
    
    /**
     * Appends and returns a new empty "licenseInformation" element
     */
    gov.loc.premis.v3.LicenseInformationComplexType addNewLicenseInformation();
    
    /**
     * Unsets the "licenseInformation" element
     */
    void unsetLicenseInformation();
    
    /**
     * Gets array of all "statuteInformation" elements
     */
    gov.loc.premis.v3.StatuteInformationComplexType[] getStatuteInformationArray();
    
    /**
     * Gets ith "statuteInformation" element
     */
    gov.loc.premis.v3.StatuteInformationComplexType getStatuteInformationArray(int i);
    
    /**
     * Returns number of "statuteInformation" element
     */
    int sizeOfStatuteInformationArray();
    
    /**
     * Sets array of all "statuteInformation" element
     */
    void setStatuteInformationArray(gov.loc.premis.v3.StatuteInformationComplexType[] statuteInformationArray);
    
    /**
     * Sets ith "statuteInformation" element
     */
    void setStatuteInformationArray(int i, gov.loc.premis.v3.StatuteInformationComplexType statuteInformation);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteInformation" element
     */
    gov.loc.premis.v3.StatuteInformationComplexType insertNewStatuteInformation(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteInformation" element
     */
    gov.loc.premis.v3.StatuteInformationComplexType addNewStatuteInformation();
    
    /**
     * Removes the ith "statuteInformation" element
     */
    void removeStatuteInformation(int i);
    
    /**
     * Gets the "otherRightsInformation" element
     */
    gov.loc.premis.v3.OtherRightsInformationComplexType getOtherRightsInformation();
    
    /**
     * True if has "otherRightsInformation" element
     */
    boolean isSetOtherRightsInformation();
    
    /**
     * Sets the "otherRightsInformation" element
     */
    void setOtherRightsInformation(gov.loc.premis.v3.OtherRightsInformationComplexType otherRightsInformation);
    
    /**
     * Appends and returns a new empty "otherRightsInformation" element
     */
    gov.loc.premis.v3.OtherRightsInformationComplexType addNewOtherRightsInformation();
    
    /**
     * Unsets the "otherRightsInformation" element
     */
    void unsetOtherRightsInformation();
    
    /**
     * Gets array of all "rightsGranted" elements
     */
    gov.loc.premis.v3.RightsGrantedComplexType[] getRightsGrantedArray();
    
    /**
     * Gets ith "rightsGranted" element
     */
    gov.loc.premis.v3.RightsGrantedComplexType getRightsGrantedArray(int i);
    
    /**
     * Returns number of "rightsGranted" element
     */
    int sizeOfRightsGrantedArray();
    
    /**
     * Sets array of all "rightsGranted" element
     */
    void setRightsGrantedArray(gov.loc.premis.v3.RightsGrantedComplexType[] rightsGrantedArray);
    
    /**
     * Sets ith "rightsGranted" element
     */
    void setRightsGrantedArray(int i, gov.loc.premis.v3.RightsGrantedComplexType rightsGranted);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsGranted" element
     */
    gov.loc.premis.v3.RightsGrantedComplexType insertNewRightsGranted(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsGranted" element
     */
    gov.loc.premis.v3.RightsGrantedComplexType addNewRightsGranted();
    
    /**
     * Removes the ith "rightsGranted" element
     */
    void removeRightsGranted(int i);
    
    /**
     * Gets array of all "linkingObjectIdentifier" elements
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] getLinkingObjectIdentifierArray();
    
    /**
     * Gets ith "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType getLinkingObjectIdentifierArray(int i);
    
    /**
     * Returns number of "linkingObjectIdentifier" element
     */
    int sizeOfLinkingObjectIdentifierArray();
    
    /**
     * Sets array of all "linkingObjectIdentifier" element
     */
    void setLinkingObjectIdentifierArray(gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] linkingObjectIdentifierArray);
    
    /**
     * Sets ith "linkingObjectIdentifier" element
     */
    void setLinkingObjectIdentifierArray(int i, gov.loc.premis.v3.LinkingObjectIdentifierComplexType linkingObjectIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType insertNewLinkingObjectIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectIdentifier" element
     */
    gov.loc.premis.v3.LinkingObjectIdentifierComplexType addNewLinkingObjectIdentifier();
    
    /**
     * Removes the ith "linkingObjectIdentifier" element
     */
    void removeLinkingObjectIdentifier(int i);
    
    /**
     * Gets array of all "linkingAgentIdentifier" elements
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] getLinkingAgentIdentifierArray();
    
    /**
     * Gets ith "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType getLinkingAgentIdentifierArray(int i);
    
    /**
     * Returns number of "linkingAgentIdentifier" element
     */
    int sizeOfLinkingAgentIdentifierArray();
    
    /**
     * Sets array of all "linkingAgentIdentifier" element
     */
    void setLinkingAgentIdentifierArray(gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] linkingAgentIdentifierArray);
    
    /**
     * Sets ith "linkingAgentIdentifier" element
     */
    void setLinkingAgentIdentifierArray(int i, gov.loc.premis.v3.LinkingAgentIdentifierComplexType linkingAgentIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType insertNewLinkingAgentIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentIdentifier" element
     */
    gov.loc.premis.v3.LinkingAgentIdentifierComplexType addNewLinkingAgentIdentifier();
    
    /**
     * Removes the ith "linkingAgentIdentifier" element
     */
    void removeLinkingAgentIdentifier(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.RightsStatementComplexType newInstance() {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.RightsStatementComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsStatementComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.RightsStatementComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.RightsStatementComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
