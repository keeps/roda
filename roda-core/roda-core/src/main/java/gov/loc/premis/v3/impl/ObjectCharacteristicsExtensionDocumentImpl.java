/*
 * An XML document type.
 * Localname: objectCharacteristicsExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectCharacteristicsExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one objectCharacteristicsExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectCharacteristicsExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectCharacteristicsExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectCharacteristicsExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTCHARACTERISTICSEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectCharacteristicsExtension");
    
    
    /**
     * Gets the "objectCharacteristicsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getObjectCharacteristicsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(OBJECTCHARACTERISTICSEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "objectCharacteristicsExtension" element
     */
    public void setObjectCharacteristicsExtension(gov.loc.premis.v3.ExtensionComplexType objectCharacteristicsExtension)
    {
        generatedSetterHelperImpl(objectCharacteristicsExtension, OBJECTCHARACTERISTICSEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "objectCharacteristicsExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewObjectCharacteristicsExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(OBJECTCHARACTERISTICSEXTENSION$0);
            return target;
        }
    }
}
