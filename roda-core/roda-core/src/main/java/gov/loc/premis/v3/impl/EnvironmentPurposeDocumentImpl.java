/*
 * An XML document type.
 * Localname: environmentPurpose
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentPurposeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentPurpose(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentPurposeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentPurposeDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentPurposeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTPURPOSE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentPurpose");
    
    
    /**
     * Gets the "environmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentPurpose()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTPURPOSE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentPurpose" element
     */
    public void setEnvironmentPurpose(gov.loc.premis.v3.StringPlusAuthority environmentPurpose)
    {
        generatedSetterHelperImpl(environmentPurpose, ENVIRONMENTPURPOSE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentPurpose" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentPurpose()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTPURPOSE$0);
            return target;
        }
    }
}
