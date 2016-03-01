/*
 * An XML document type.
 * Localname: hwOtherInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.HwOtherInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one hwOtherInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class HwOtherInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.HwOtherInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public HwOtherInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HWOTHERINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "hwOtherInformation");
    
    
    /**
     * Gets the "hwOtherInformation" element
     */
    public java.lang.String getHwOtherInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "hwOtherInformation" element
     */
    public org.apache.xmlbeans.XmlString xgetHwOtherInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(HWOTHERINFORMATION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "hwOtherInformation" element
     */
    public void setHwOtherInformation(java.lang.String hwOtherInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(HWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(HWOTHERINFORMATION$0);
            }
            target.setStringValue(hwOtherInformation);
        }
    }
    
    /**
     * Sets (as xml) the "hwOtherInformation" element
     */
    public void xsetHwOtherInformation(org.apache.xmlbeans.XmlString hwOtherInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(HWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(HWOTHERINFORMATION$0);
            }
            target.set(hwOtherInformation);
        }
    }
}
