/*
 * An XML document type.
 * Localname: licenseInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseInformation");
    
    
    /**
     * Gets the "licenseInformation" element
     */
    public gov.loc.premis.v3.LicenseInformationComplexType getLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseInformationComplexType target = null;
            target = (gov.loc.premis.v3.LicenseInformationComplexType)get_store().find_element_user(LICENSEINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseInformation" element
     */
    public void setLicenseInformation(gov.loc.premis.v3.LicenseInformationComplexType licenseInformation)
    {
        generatedSetterHelperImpl(licenseInformation, LICENSEINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseInformation" element
     */
    public gov.loc.premis.v3.LicenseInformationComplexType addNewLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseInformationComplexType target = null;
            target = (gov.loc.premis.v3.LicenseInformationComplexType)get_store().add_element_user(LICENSEINFORMATION$0);
            return target;
        }
    }
}
