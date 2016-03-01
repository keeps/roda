/*
 * An XML document type.
 * Localname: statuteDocumentationRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteDocumentationRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteDocumentationRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteDocumentationRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteDocumentationRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteDocumentationRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationRole");
    
    
    /**
     * Gets the "statuteDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STATUTEDOCUMENTATIONROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteDocumentationRole" element
     */
    public void setStatuteDocumentationRole(gov.loc.premis.v3.StringPlusAuthority statuteDocumentationRole)
    {
        generatedSetterHelperImpl(statuteDocumentationRole, STATUTEDOCUMENTATIONROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStatuteDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STATUTEDOCUMENTATIONROLE$0);
            return target;
        }
    }
}
