/*
 * An XML document type.
 * Localname: formatRegistryRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatRegistryRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one formatRegistryRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class FormatRegistryRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatRegistryRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public FormatRegistryRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATREGISTRYROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistryRole");
    
    
    /**
     * Gets the "formatRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(FORMATREGISTRYROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "formatRegistryRole" element
     */
    public void setFormatRegistryRole(gov.loc.premis.v3.StringPlusAuthority formatRegistryRole)
    {
        generatedSetterHelperImpl(formatRegistryRole, FORMATREGISTRYROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistryRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewFormatRegistryRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(FORMATREGISTRYROLE$0);
            return target;
        }
    }
}
