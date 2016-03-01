/*
 * An XML document type.
 * Localname: licenseIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseIdentifierType");
    
    
    /**
     * Gets the "licenseIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLicenseIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LICENSEIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseIdentifierType" element
     */
    public void setLicenseIdentifierType(gov.loc.premis.v3.StringPlusAuthority licenseIdentifierType)
    {
        generatedSetterHelperImpl(licenseIdentifierType, LICENSEIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLicenseIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LICENSEIDENTIFIERTYPE$0);
            return target;
        }
    }
}
