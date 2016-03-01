/*
 * An XML document type.
 * Localname: licenseDocumentationIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseDocumentationIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseDocumentationIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseDocumentationIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseDocumentationIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseDocumentationIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifierValue");
    
    
    /**
     * Gets the "licenseDocumentationIdentifierValue" element
     */
    public java.lang.String getLicenseDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationIdentifierValue" element
     */
    public void setLicenseDocumentationIdentifierValue(java.lang.String licenseDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.setStringValue(licenseDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "licenseDocumentationIdentifierValue" element
     */
    public void xsetLicenseDocumentationIdentifierValue(org.apache.xmlbeans.XmlString licenseDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.set(licenseDocumentationIdentifierValue);
        }
    }
}
