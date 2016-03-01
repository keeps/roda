/*
 * An XML document type.
 * Localname: linkingAgentRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingAgentRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingAgentRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingAgentRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingAgentRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGAGENTROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentRole");
    
    
    /**
     * Gets the "linkingAgentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingAgentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGAGENTROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentRole" element
     */
    public void setLinkingAgentRole(gov.loc.premis.v3.StringPlusAuthority linkingAgentRole)
    {
        generatedSetterHelperImpl(linkingAgentRole, LINKINGAGENTROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingAgentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGAGENTROLE$0);
            return target;
        }
    }
}
