/*
 * An XML document type.
 * Localname: swOtherInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SwOtherInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one swOtherInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SwOtherInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SwOtherInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public SwOtherInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SWOTHERINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "swOtherInformation");
    
    
    /**
     * Gets the "swOtherInformation" element
     */
    public java.lang.String getSwOtherInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "swOtherInformation" element
     */
    public org.apache.xmlbeans.XmlString xgetSwOtherInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SWOTHERINFORMATION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "swOtherInformation" element
     */
    public void setSwOtherInformation(java.lang.String swOtherInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SWOTHERINFORMATION$0);
            }
            target.setStringValue(swOtherInformation);
        }
    }
    
    /**
     * Sets (as xml) the "swOtherInformation" element
     */
    public void xsetSwOtherInformation(org.apache.xmlbeans.XmlString swOtherInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SWOTHERINFORMATION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SWOTHERINFORMATION$0);
            }
            target.set(swOtherInformation);
        }
    }
}
