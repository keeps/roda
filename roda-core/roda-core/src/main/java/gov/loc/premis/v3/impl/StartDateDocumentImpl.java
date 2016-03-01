/*
 * An XML document type.
 * Localname: startDate
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StartDateDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one startDate(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StartDateDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StartDateDocument
{
    private static final long serialVersionUID = 1L;
    
    public StartDateDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STARTDATE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "startDate");
    
    
    /**
     * Gets the "startDate" element
     */
    public java.lang.String getStartDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STARTDATE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "startDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetStartDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STARTDATE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "startDate" element
     */
    public void setStartDate(java.lang.String startDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STARTDATE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STARTDATE$0);
            }
            target.setStringValue(startDate);
        }
    }
    
    /**
     * Sets (as xml) the "startDate" element
     */
    public void xsetStartDate(gov.loc.premis.v3.EdtfSimpleType startDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STARTDATE$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(STARTDATE$0);
            }
            target.set(startDate);
        }
    }
}
