/*
 * An XML document type.
 * Localname: environmentRegistryRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentRegistryRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentRegistryRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentRegistryRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentRegistryRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentRegistryRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryRole");
    
    
    /**
     * Gets the "environmentRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTREGISTRYROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistryRole" element
     */
    public void setEnvironmentRegistryRole(gov.loc.premis.v3.StringPlusAuthority environmentRegistryRole)
    {
        generatedSetterHelperImpl(environmentRegistryRole, ENVIRONMENTREGISTRYROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTREGISTRYROLE$0);
            return target;
        }
    }
}
