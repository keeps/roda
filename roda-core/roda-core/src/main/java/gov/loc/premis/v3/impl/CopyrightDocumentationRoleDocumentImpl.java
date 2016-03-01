/*
 * An XML document type.
 * Localname: copyrightDocumentationRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightDocumentationRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightDocumentationRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightDocumentationRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightDocumentationRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationRole");
    
    
    /**
     * Gets the "copyrightDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTDOCUMENTATIONROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationRole" element
     */
    public void setCopyrightDocumentationRole(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationRole)
    {
        generatedSetterHelperImpl(copyrightDocumentationRole, COPYRIGHTDOCUMENTATIONROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTDOCUMENTATIONROLE$0);
            return target;
        }
    }
}
