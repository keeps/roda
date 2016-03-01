/*
 * XML Type:  agentComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML agentComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface AgentComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(AgentComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("agentcomplextype9692type");
    
    /**
     * Gets array of all "agentIdentifier" elements
     */
    gov.loc.premis.v3.AgentIdentifierComplexType[] getAgentIdentifierArray();
    
    /**
     * Gets ith "agentIdentifier" element
     */
    gov.loc.premis.v3.AgentIdentifierComplexType getAgentIdentifierArray(int i);
    
    /**
     * Returns number of "agentIdentifier" element
     */
    int sizeOfAgentIdentifierArray();
    
    /**
     * Sets array of all "agentIdentifier" element
     */
    void setAgentIdentifierArray(gov.loc.premis.v3.AgentIdentifierComplexType[] agentIdentifierArray);
    
    /**
     * Sets ith "agentIdentifier" element
     */
    void setAgentIdentifierArray(int i, gov.loc.premis.v3.AgentIdentifierComplexType agentIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentIdentifier" element
     */
    gov.loc.premis.v3.AgentIdentifierComplexType insertNewAgentIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentIdentifier" element
     */
    gov.loc.premis.v3.AgentIdentifierComplexType addNewAgentIdentifier();
    
    /**
     * Removes the ith "agentIdentifier" element
     */
    void removeAgentIdentifier(int i);
    
    /**
     * Gets array of all "agentName" elements
     */
    gov.loc.premis.v3.StringPlusAuthority[] getAgentNameArray();
    
    /**
     * Gets ith "agentName" element
     */
    gov.loc.premis.v3.StringPlusAuthority getAgentNameArray(int i);
    
    /**
     * Returns number of "agentName" element
     */
    int sizeOfAgentNameArray();
    
    /**
     * Sets array of all "agentName" element
     */
    void setAgentNameArray(gov.loc.premis.v3.StringPlusAuthority[] agentNameArray);
    
    /**
     * Sets ith "agentName" element
     */
    void setAgentNameArray(int i, gov.loc.premis.v3.StringPlusAuthority agentName);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentName" element
     */
    gov.loc.premis.v3.StringPlusAuthority insertNewAgentName(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentName" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewAgentName();
    
    /**
     * Removes the ith "agentName" element
     */
    void removeAgentName(int i);
    
    /**
     * Gets the "agentType" element
     */
    gov.loc.premis.v3.StringPlusAuthority getAgentType();
    
    /**
     * True if has "agentType" element
     */
    boolean isSetAgentType();
    
    /**
     * Sets the "agentType" element
     */
    void setAgentType(gov.loc.premis.v3.StringPlusAuthority agentType);
    
    /**
     * Appends and returns a new empty "agentType" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewAgentType();
    
    /**
     * Unsets the "agentType" element
     */
    void unsetAgentType();
    
    /**
     * Gets the "agentVersion" element
     */
    java.lang.String getAgentVersion();
    
    /**
     * Gets (as xml) the "agentVersion" element
     */
    org.apache.xmlbeans.XmlString xgetAgentVersion();
    
    /**
     * True if has "agentVersion" element
     */
    boolean isSetAgentVersion();
    
    /**
     * Sets the "agentVersion" element
     */
    void setAgentVersion(java.lang.String agentVersion);
    
    /**
     * Sets (as xml) the "agentVersion" element
     */
    void xsetAgentVersion(org.apache.xmlbeans.XmlString agentVersion);
    
    /**
     * Unsets the "agentVersion" element
     */
    void unsetAgentVersion();
    
    /**
     * Gets array of all "agentNote" elements
     */
    java.lang.String[] getAgentNoteArray();
    
    /**
     * Gets ith "agentNote" element
     */
    java.lang.String getAgentNoteArray(int i);
    
    /**
     * Gets (as xml) array of all "agentNote" elements
     */
    org.apache.xmlbeans.XmlString[] xgetAgentNoteArray();
    
    /**
     * Gets (as xml) ith "agentNote" element
     */
    org.apache.xmlbeans.XmlString xgetAgentNoteArray(int i);
    
    /**
     * Returns number of "agentNote" element
     */
    int sizeOfAgentNoteArray();
    
    /**
     * Sets array of all "agentNote" element
     */
    void setAgentNoteArray(java.lang.String[] agentNoteArray);
    
    /**
     * Sets ith "agentNote" element
     */
    void setAgentNoteArray(int i, java.lang.String agentNote);
    
    /**
     * Sets (as xml) array of all "agentNote" element
     */
    void xsetAgentNoteArray(org.apache.xmlbeans.XmlString[] agentNoteArray);
    
    /**
     * Sets (as xml) ith "agentNote" element
     */
    void xsetAgentNoteArray(int i, org.apache.xmlbeans.XmlString agentNote);
    
    /**
     * Inserts the value as the ith "agentNote" element
     */
    void insertAgentNote(int i, java.lang.String agentNote);
    
    /**
     * Appends the value as the last "agentNote" element
     */
    void addAgentNote(java.lang.String agentNote);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentNote" element
     */
    org.apache.xmlbeans.XmlString insertNewAgentNote(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentNote" element
     */
    org.apache.xmlbeans.XmlString addNewAgentNote();
    
    /**
     * Removes the ith "agentNote" element
     */
    void removeAgentNote(int i);
    
    /**
     * Gets array of all "agentExtension" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getAgentExtensionArray();
    
    /**
     * Gets ith "agentExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType getAgentExtensionArray(int i);
    
    /**
     * Returns number of "agentExtension" element
     */
    int sizeOfAgentExtensionArray();
    
    /**
     * Sets array of all "agentExtension" element
     */
    void setAgentExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] agentExtensionArray);
    
    /**
     * Sets ith "agentExtension" element
     */
    void setAgentExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType agentExtension);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewAgentExtension(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewAgentExtension();
    
    /**
     * Removes the ith "agentExtension" element
     */
    void removeAgentExtension(int i);
    
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
     * Gets array of all "linkingEnvironmentIdentifier" elements
     */
    gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[] getLinkingEnvironmentIdentifierArray();
    
    /**
     * Gets ith "linkingEnvironmentIdentifier" element
     */
    gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType getLinkingEnvironmentIdentifierArray(int i);
    
    /**
     * Returns number of "linkingEnvironmentIdentifier" element
     */
    int sizeOfLinkingEnvironmentIdentifierArray();
    
    /**
     * Sets array of all "linkingEnvironmentIdentifier" element
     */
    void setLinkingEnvironmentIdentifierArray(gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[] linkingEnvironmentIdentifierArray);
    
    /**
     * Sets ith "linkingEnvironmentIdentifier" element
     */
    void setLinkingEnvironmentIdentifierArray(int i, gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType linkingEnvironmentIdentifier);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEnvironmentIdentifier" element
     */
    gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType insertNewLinkingEnvironmentIdentifier(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEnvironmentIdentifier" element
     */
    gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType addNewLinkingEnvironmentIdentifier();
    
    /**
     * Removes the ith "linkingEnvironmentIdentifier" element
     */
    void removeLinkingEnvironmentIdentifier(int i);
    
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
        public static gov.loc.premis.v3.AgentComplexType newInstance() {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.AgentComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.AgentComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.AgentComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.AgentComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.AgentComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
