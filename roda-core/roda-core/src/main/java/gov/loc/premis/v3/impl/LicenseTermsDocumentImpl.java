/*
 * An XML document type.
 * Localname: licenseTerms
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseTermsDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseTerms(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseTermsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseTermsDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseTermsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSETERMS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseTerms");
    
    
    /**
     * Gets the "licenseTerms" element
     */
    public java.lang.String getLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSETERMS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseTerms" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSETERMS$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "licenseTerms" element
     */
    public void setLicenseTerms(java.lang.String licenseTerms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSETERMS$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSETERMS$0);
            }
            target.setStringValue(licenseTerms);
        }
    }
    
    /**
     * Sets (as xml) the "licenseTerms" element
     */
    public void xsetLicenseTerms(org.apache.xmlbeans.XmlString licenseTerms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSETERMS$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSETERMS$0);
            }
            target.set(licenseTerms);
        }
    }
}
