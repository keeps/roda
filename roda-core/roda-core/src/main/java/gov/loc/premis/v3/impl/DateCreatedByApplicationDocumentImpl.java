/*
 * An XML document type.
 * Localname: dateCreatedByApplication
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.DateCreatedByApplicationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one dateCreatedByApplication(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class DateCreatedByApplicationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.DateCreatedByApplicationDocument
{
    private static final long serialVersionUID = 1L;
    
    public DateCreatedByApplicationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName DATECREATEDBYAPPLICATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "dateCreatedByApplication");
    
    
    /**
     * Gets the "dateCreatedByApplication" element
     */
    public java.lang.String getDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DATECREATEDBYAPPLICATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "dateCreatedByApplication" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetDateCreatedByApplication()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(DATECREATEDBYAPPLICATION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "dateCreatedByApplication" element
     */
    public void setDateCreatedByApplication(java.lang.String dateCreatedByApplication)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(DATECREATEDBYAPPLICATION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(DATECREATEDBYAPPLICATION$0);
            }
            target.setStringValue(dateCreatedByApplication);
        }
    }
    
    /**
     * Sets (as xml) the "dateCreatedByApplication" element
     */
    public void xsetDateCreatedByApplication(gov.loc.premis.v3.EdtfSimpleType dateCreatedByApplication)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(DATECREATEDBYAPPLICATION$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(DATECREATEDBYAPPLICATION$0);
            }
            target.set(dateCreatedByApplication);
        }
    }
}
