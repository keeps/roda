/*
 * An XML document type.
 * Localname: otherRightsDocumentationRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsDocumentationRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsDocumentationRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsDocumentationRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsDocumentationRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsDocumentationRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationRole");
    
    
    /**
     * Gets the "otherRightsDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationRole" element
     */
    public void setOtherRightsDocumentationRole(gov.loc.premis.v3.StringPlusAuthority otherRightsDocumentationRole)
    {
        generatedSetterHelperImpl(otherRightsDocumentationRole, OTHERRIGHTSDOCUMENTATIONROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONROLE$0);
            return target;
        }
    }
}
