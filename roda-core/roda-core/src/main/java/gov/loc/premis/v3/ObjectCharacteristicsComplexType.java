/*
 * XML Type:  objectCharacteristicsComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectCharacteristicsComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3;


/**
 * An XML objectCharacteristicsComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public interface ObjectCharacteristicsComplexType extends org.apache.xmlbeans.XmlObject
{
    public static final org.apache.xmlbeans.SchemaType type = (org.apache.xmlbeans.SchemaType)
        org.apache.xmlbeans.XmlBeans.typeSystemForClassLoader(ObjectCharacteristicsComplexType.class.getClassLoader(), "schemaorg_apache_xmlbeans.system.s7436A832873D7D1134D0870C60EDA074").resolveHandle("objectcharacteristicscomplextype0286type");
    
    /**
     * Gets the "compositionLevel" element
     */
    gov.loc.premis.v3.CompositionLevelComplexType getCompositionLevel();
    
    /**
     * True if has "compositionLevel" element
     */
    boolean isSetCompositionLevel();
    
    /**
     * Sets the "compositionLevel" element
     */
    void setCompositionLevel(gov.loc.premis.v3.CompositionLevelComplexType compositionLevel);
    
    /**
     * Appends and returns a new empty "compositionLevel" element
     */
    gov.loc.premis.v3.CompositionLevelComplexType addNewCompositionLevel();
    
    /**
     * Unsets the "compositionLevel" element
     */
    void unsetCompositionLevel();
    
    /**
     * Gets array of all "fixity" elements
     */
    gov.loc.premis.v3.FixityComplexType[] getFixityArray();
    
    /**
     * Gets ith "fixity" element
     */
    gov.loc.premis.v3.FixityComplexType getFixityArray(int i);
    
    /**
     * Returns number of "fixity" element
     */
    int sizeOfFixityArray();
    
    /**
     * Sets array of all "fixity" element
     */
    void setFixityArray(gov.loc.premis.v3.FixityComplexType[] fixityArray);
    
    /**
     * Sets ith "fixity" element
     */
    void setFixityArray(int i, gov.loc.premis.v3.FixityComplexType fixity);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "fixity" element
     */
    gov.loc.premis.v3.FixityComplexType insertNewFixity(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "fixity" element
     */
    gov.loc.premis.v3.FixityComplexType addNewFixity();
    
    /**
     * Removes the ith "fixity" element
     */
    void removeFixity(int i);
    
    /**
     * Gets the "size" element
     */
    long getSize();
    
    /**
     * Gets (as xml) the "size" element
     */
    org.apache.xmlbeans.XmlLong xgetSize();
    
    /**
     * True if has "size" element
     */
    boolean isSetSize();
    
    /**
     * Sets the "size" element
     */
    void setSize(long size);
    
    /**
     * Sets (as xml) the "size" element
     */
    void xsetSize(org.apache.xmlbeans.XmlLong size);
    
    /**
     * Unsets the "size" element
     */
    void unsetSize();
    
    /**
     * Gets array of all "format" elements
     */
    gov.loc.premis.v3.FormatComplexType[] getFormatArray();
    
    /**
     * Gets ith "format" element
     */
    gov.loc.premis.v3.FormatComplexType getFormatArray(int i);
    
    /**
     * Returns number of "format" element
     */
    int sizeOfFormatArray();
    
    /**
     * Sets array of all "format" element
     */
    void setFormatArray(gov.loc.premis.v3.FormatComplexType[] formatArray);
    
    /**
     * Sets ith "format" element
     */
    void setFormatArray(int i, gov.loc.premis.v3.FormatComplexType format);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "format" element
     */
    gov.loc.premis.v3.FormatComplexType insertNewFormat(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "format" element
     */
    gov.loc.premis.v3.FormatComplexType addNewFormat();
    
    /**
     * Removes the ith "format" element
     */
    void removeFormat(int i);
    
    /**
     * Gets array of all "creatingApplication" elements
     */
    gov.loc.premis.v3.CreatingApplicationComplexType[] getCreatingApplicationArray();
    
    /**
     * Gets ith "creatingApplication" element
     */
    gov.loc.premis.v3.CreatingApplicationComplexType getCreatingApplicationArray(int i);
    
    /**
     * Returns number of "creatingApplication" element
     */
    int sizeOfCreatingApplicationArray();
    
    /**
     * Sets array of all "creatingApplication" element
     */
    void setCreatingApplicationArray(gov.loc.premis.v3.CreatingApplicationComplexType[] creatingApplicationArray);
    
    /**
     * Sets ith "creatingApplication" element
     */
    void setCreatingApplicationArray(int i, gov.loc.premis.v3.CreatingApplicationComplexType creatingApplication);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "creatingApplication" element
     */
    gov.loc.premis.v3.CreatingApplicationComplexType insertNewCreatingApplication(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "creatingApplication" element
     */
    gov.loc.premis.v3.CreatingApplicationComplexType addNewCreatingApplication();
    
    /**
     * Removes the ith "creatingApplication" element
     */
    void removeCreatingApplication(int i);
    
    /**
     * Gets array of all "inhibitors" elements
     */
    gov.loc.premis.v3.InhibitorsComplexType[] getInhibitorsArray();
    
    /**
     * Gets ith "inhibitors" element
     */
    gov.loc.premis.v3.InhibitorsComplexType getInhibitorsArray(int i);
    
    /**
     * Returns number of "inhibitors" element
     */
    int sizeOfInhibitorsArray();
    
    /**
     * Sets array of all "inhibitors" element
     */
    void setInhibitorsArray(gov.loc.premis.v3.InhibitorsComplexType[] inhibitorsArray);
    
    /**
     * Sets ith "inhibitors" element
     */
    void setInhibitorsArray(int i, gov.loc.premis.v3.InhibitorsComplexType inhibitors);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "inhibitors" element
     */
    gov.loc.premis.v3.InhibitorsComplexType insertNewInhibitors(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "inhibitors" element
     */
    gov.loc.premis.v3.InhibitorsComplexType addNewInhibitors();
    
    /**
     * Removes the ith "inhibitors" element
     */
    void removeInhibitors(int i);
    
    /**
     * Gets array of all "objectCharacteristicsExtension" elements
     */
    gov.loc.premis.v3.ExtensionComplexType[] getObjectCharacteristicsExtensionArray();
    
    /**
     * Gets ith "objectCharacteristicsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType getObjectCharacteristicsExtensionArray(int i);
    
    /**
     * Returns number of "objectCharacteristicsExtension" element
     */
    int sizeOfObjectCharacteristicsExtensionArray();
    
    /**
     * Sets array of all "objectCharacteristicsExtension" element
     */
    void setObjectCharacteristicsExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] objectCharacteristicsExtensionArray);
    
    /**
     * Sets ith "objectCharacteristicsExtension" element
     */
    void setObjectCharacteristicsExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType objectCharacteristicsExtension);
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "objectCharacteristicsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType insertNewObjectCharacteristicsExtension(int i);
    
    /**
     * Appends and returns a new empty value (as xml) as the last "objectCharacteristicsExtension" element
     */
    gov.loc.premis.v3.ExtensionComplexType addNewObjectCharacteristicsExtension();
    
    /**
     * Removes the ith "objectCharacteristicsExtension" element
     */
    void removeObjectCharacteristicsExtension(int i);
    
    /**
     * A factory class with static methods for creating instances
     * of this type.
     */
    
    public static final class Factory
    {
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType newInstance() {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType newInstance(org.apache.xmlbeans.XmlOptions options) {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newInstance( type, options ); }
        
        /** @param xmlAsString the string value to parse */
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.lang.String xmlAsString) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.lang.String xmlAsString, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xmlAsString, type, options ); }
        
        /** @param file the file from which to load an xml document */
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.File file) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.File file, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( file, type, options ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( u, type, options ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.InputStream is) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.InputStream is, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( is, type, options ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.Reader r) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(java.io.Reader r, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( r, type, options ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(javax.xml.stream.XMLStreamReader sr) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(javax.xml.stream.XMLStreamReader sr, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( sr, type, options ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(org.w3c.dom.Node node) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, null ); }
        
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(org.w3c.dom.Node node, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( node, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static gov.loc.premis.v3.ObjectCharacteristicsComplexType parse(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return (gov.loc.premis.v3.ObjectCharacteristicsComplexType) org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse( xis, type, options ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, null ); }
        
        /** @deprecated {@link org.apache.xmlbeans.xml.stream.XMLInputStream} */
        public static org.apache.xmlbeans.xml.stream.XMLInputStream newValidatingXMLInputStream(org.apache.xmlbeans.xml.stream.XMLInputStream xis, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException {
          return org.apache.xmlbeans.XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, type, options ); }
        
        private Factory() { } // No instance of this class allowed
    }
}
