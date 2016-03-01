/*
 * An XML document type.
 * Localname: licenseDocumentationRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseDocumentationRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseDocumentationRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseDocumentationRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseDocumentationRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseDocumentationRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationRole");
    
    
    /**
     * Gets the "licenseDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LICENSEDOCUMENTATIONROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationRole" element
     */
    public void setLicenseDocumentationRole(gov.loc.premis.v3.StringPlusAuthority licenseDocumentationRole)
    {
        generatedSetterHelperImpl(licenseDocumentationRole, LICENSEDOCUMENTATIONROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LICENSEDOCUMENTATIONROLE$0);
            return target;
        }
    }
}
