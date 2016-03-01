/*
 * An XML document type.
 * Localname: linkingEnvironmentRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEnvironmentRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEnvironmentRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEnvironmentRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEnvironmentRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentRole");
    
    
    /**
     * Gets the "linkingEnvironmentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingEnvironmentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGENVIRONMENTROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentRole" element
     */
    public void setLinkingEnvironmentRole(gov.loc.premis.v3.StringPlusAuthority linkingEnvironmentRole)
    {
        generatedSetterHelperImpl(linkingEnvironmentRole, LINKINGENVIRONMENTROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingEnvironmentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingEnvironmentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGENVIRONMENTROLE$0);
            return target;
        }
    }
}
