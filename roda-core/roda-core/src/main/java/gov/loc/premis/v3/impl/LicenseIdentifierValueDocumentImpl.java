/*
 * An XML document type.
 * Localname: licenseIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseIdentifierValue");
    
    
    /**
     * Gets the "licenseIdentifierValue" element
     */
    public java.lang.String getLicenseIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "licenseIdentifierValue" element
     */
    public void setLicenseIdentifierValue(java.lang.String licenseIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSEIDENTIFIERVALUE$0);
            }
            target.setStringValue(licenseIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "licenseIdentifierValue" element
     */
    public void xsetLicenseIdentifierValue(org.apache.xmlbeans.XmlString licenseIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSEIDENTIFIERVALUE$0);
            }
            target.set(licenseIdentifierValue);
        }
    }
}
