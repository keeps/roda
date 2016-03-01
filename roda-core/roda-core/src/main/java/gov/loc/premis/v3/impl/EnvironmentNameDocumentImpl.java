/*
 * An XML document type.
 * Localname: environmentName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentName");
    
    
    /**
     * Gets the "environmentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentName" element
     */
    public void setEnvironmentName(gov.loc.premis.v3.StringPlusAuthority environmentName)
    {
        generatedSetterHelperImpl(environmentName, ENVIRONMENTNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTNAME$0);
            return target;
        }
    }
}
