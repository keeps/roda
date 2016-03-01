/*
 * An XML document type.
 * Localname: licenseApplicableDates
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseApplicableDatesDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseApplicableDates(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseApplicableDatesDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseApplicableDatesDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseApplicableDatesDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEAPPLICABLEDATES$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseApplicableDates");
    
    
    /**
     * Gets the "licenseApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(LICENSEAPPLICABLEDATES$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseApplicableDates" element
     */
    public void setLicenseApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType licenseApplicableDates)
    {
        generatedSetterHelperImpl(licenseApplicableDates, LICENSEAPPLICABLEDATES$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(LICENSEAPPLICABLEDATES$0);
            return target;
        }
    }
}
