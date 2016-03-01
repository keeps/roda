/*
 * An XML document type.
 * Localname: environmentCharacteristic
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentCharacteristicDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentCharacteristic(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentCharacteristicDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentCharacteristicDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentCharacteristicDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTCHARACTERISTIC$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentCharacteristic");
    
    
    /**
     * Gets the "environmentCharacteristic" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTCHARACTERISTIC$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentCharacteristic" element
     */
    public void setEnvironmentCharacteristic(gov.loc.premis.v3.StringPlusAuthority environmentCharacteristic)
    {
        generatedSetterHelperImpl(environmentCharacteristic, ENVIRONMENTCHARACTERISTIC$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentCharacteristic" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentCharacteristic()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTCHARACTERISTIC$0);
            return target;
        }
    }
}
