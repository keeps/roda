/*
 * An XML document type.
 * Localname: linkingObjectRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingObjectRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingObjectRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingObjectRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingObjectRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGOBJECTROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectRole");
    
    
    /**
     * Gets the "linkingObjectRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingObjectRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGOBJECTROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectRole" element
     */
    public void setLinkingObjectRole(gov.loc.premis.v3.StringPlusAuthority linkingObjectRole)
    {
        generatedSetterHelperImpl(linkingObjectRole, LINKINGOBJECTROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingObjectRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGOBJECTROLE$0);
            return target;
        }
    }
}
