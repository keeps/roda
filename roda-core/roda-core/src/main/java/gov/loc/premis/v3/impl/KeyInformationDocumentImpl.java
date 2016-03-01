/*
 * An XML document type.
 * Localname: keyInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.KeyInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one keyInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class KeyInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.KeyInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public KeyInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName KEYINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "keyInformation");
    
    
    /**
     * Gets the "keyInformation" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getKeyInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(KEYINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "keyInformation" element
     */
    public void setKeyInformation(gov.loc.premis.v3.ExtensionComplexType keyInformation)
    {
        generatedSetterHelperImpl(keyInformation, KEYINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "keyInformation" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewKeyInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(KEYINFORMATION$0);
            return target;
        }
    }
}
