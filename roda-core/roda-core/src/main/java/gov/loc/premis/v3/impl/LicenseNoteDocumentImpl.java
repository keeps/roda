/*
 * An XML document type.
 * Localname: licenseNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSENOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseNote");
    
    
    /**
     * Gets the "licenseNote" element
     */
    public java.lang.String getLicenseNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSENOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseNote" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSENOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "licenseNote" element
     */
    public void setLicenseNote(java.lang.String licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSENOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSENOTE$0);
            }
            target.setStringValue(licenseNote);
        }
    }
    
    /**
     * Sets (as xml) the "licenseNote" element
     */
    public void xsetLicenseNote(org.apache.xmlbeans.XmlString licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSENOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSENOTE$0);
            }
            target.set(licenseNote);
        }
    }
}
