/*
 * An XML document type.
 * Localname: licenseDocumentationIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseDocumentationIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseDocumentationIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseDocumentationIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseDocumentationIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseDocumentationIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifierType");
    
    
    /**
     * Gets the "licenseDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLicenseDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationIdentifierType" element
     */
    public void setLicenseDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority licenseDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(licenseDocumentationIdentifierType, LICENSEDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLicenseDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
}
