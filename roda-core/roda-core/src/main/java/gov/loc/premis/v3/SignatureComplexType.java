/*
 * XML Type:  signatureComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML signatureComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface SignatureComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(SignatureComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("signaturecomplextype09c5type");
    
    /**
     * Gets the "signatureEncoding" element
     */
    gov.loc.premis.v3.StringPlusAuthority getSignatureEncoding();
    
    /**
     * Sets the "signatureEncoding" element
     */
    void setSignatureEncoding(gov.loc.premis.v3.StringPlusAuthority signatureEncoding);
    
    /**
     * Appends and returns a new empty "signatureEncoding" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewSignatureEncoding();
    
    /**
     * Gets the "signer" element
     */
    gov.loc.premis.v3.StringPlusAuthority getSigner();
    
    /**
     * True if has "signer" element
     */
    boolean isSetSigner();
    
    /**
     * Sets the "signer" element
     */
    void setSigner(gov.loc.premis.v3.StringPlusAuthority signer);
    
    /**
     * Appends and returns a new empty "signer" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewSigner();
    
    /**
     * Unsets the "signer" element
     */
    void unsetSigner();
    
    /**
     * Gets the "signatureMethod" element
     */
    gov.loc.premis.v3.StringPlusAuthority getSignatureMethod();
    
    /**
     * Sets the "signatureMethod" element
     */
    void setSignatureMethod(gov.loc.premis.v3.StringPlusAuthority signatureMethod);
    
    /**
     * Appends and returns a new empty "signatureMethod" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewSignatureMethod();
    
    /**
     * Gets the "signatureValue" element
     */
    java.lang.String getSignatureValue();
    
    /**
     * Gets (as xml) the "signatureValue" element
     */
    org.apache.xmlbeans.XmlString xgetSignatureValue();
    
    /**
     * Sets the "signatureValue" element
     */
    void setSignatureValue(java.lang.String signatureValue);
    
    /**
     * Sets (as xml) the "signatureValue" element
     */
    void xsetSignatureValue(org.apache.xmlbeans.XmlString signatureValue);
    
    /**
     * Gets the "signatureValidationRules" element
     */
    gov.loc.premis.v3.StringPlusAuthority getSignatureValidationRules();
    
    /**
     * Sets the "signatureValidationRules" element
     */
    void setSignatureValidationRules(gov.loc.premis.v3.StringPlusAuthority signatureValidationRules);
    
    /**
     * Appends and returns a new empty "signatureValidationRules" element
     */
    gov.loc.premis.v3.StringPlusAuthority addNewSignatureValidationRules();
    
    /**
     * Gets array of all "signatureProperties" elements
     */
    java.lang.String[] getSignaturePropertiesArray();
    
    /**
     * Gets ith "signatureProperties" element
     */
    java.lang.String getSignaturePropertiesArray(int i);
    
    /**
     * Gets (as xml) array of all "signatureProperties" elements
     */
    org.apache.xmlbeans.XmlString[] xgetSignaturePropertiesArray();
    
    /**
     * Gets (as xml) ith "signatureProperties" element
     */
    org.apache.xmlbeans.XmlString xgetSignaturePropertiesArray(int i);
    
    /**
     * Returns number of "signatureProperties" element
     */
    int sizeOfSignaturePropertiesArray();
    
    /**
     * Sets array of all "signatureProperties" element
     */
    void setSignaturePropertiesArray(java.lang.String[] signaturePropertiesArray);
    
    /**
     * Sets ith "signatureProperties" element
     */
    void setSignaturePropertiesArray(int i, java.lang.String signatureProperties);
    
    /**
     * Sets (as xml) array of all "signatureProperties" element
     */
    void xsetSignaturePropertiesArray(org.apache.xmlbeans.XmlString[] signaturePropertiesArray);
    
    /**
     * Sets (as xml) ith "signatureProperties" element
     */
    void xsetSignaturePropertiesArray(int i, org.apache.xmlbeans.XmlString signatureProperties);
    
    /**
     * Inserts the value as the ith "signatureProperties" element
     */
    void insertSignatureProperties(int i, java.lang.String signatureProperties);
    
    /**
     * Appends the value as the last "signatureProperties" element
     */
    void addSignatureProperties(java.lang.String signatureProperties);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "signatureProperties" element
     */
    org.apache.xmlbeans.XmlString insertNewSignatureProperties(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "signatureProperties" element
     */
    org.apache.xmlbeans.XmlString addNewSignatureProperties();
    
    /**
     * Removes the ith "signatureProperties" element
     */
    void removeSignatureProperties(int i);
    
    /**
     * Gets array of all "keyInformation" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getKeyInformationArray();
    
    /**
     * Gets ith "keyInformation" element
     */
    gov.loc.premis.v3.ExtensionComplexType getKeyInformationArray(int i);
    
    /**
     * Returns number of "keyInformation" element
     */
    int sizeOfKeyInformationArray();
    
    /**
     * Sets array of all "keyInformation" element
     */
    void setKeyInformationArray(gov.loc.premis.v3.ExtensionComplexType[] keyInformationArray);
    
    /**
     * Sets ith "keyInformation" element
     */
    void setKeyInformationArray(int i, gov.loc.premis.v3.ExtensionComplexType keyInformation);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "keyInformation" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewKeyInformation(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "keyInformation" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewKeyInformation();
    
    /**
     * Removes the ith "keyInformation" element
     */
    void removeKeyInformation(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.SignatureComplexType newInstance() {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.SignatureComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.SignatureComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.SignatureComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.SignatureComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.SignatureComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
