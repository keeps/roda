/*
 * An XML document type.
 * Localname: endDate
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EndDateDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one endDate(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EndDateDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EndDateDocument
{
    private static final long serialVersionUID = 1L;
    
    public EndDateDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENDDATE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "endDate");
    
    
    /**
     * Gets the "endDate" element
     */
    public java.lang.String getEndDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENDDATE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "endDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetEndDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(ENDDATE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "endDate" element
     */
    public void setEndDate(java.lang.String endDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENDDATE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENDDATE$0);
            }
            target.setStringValue(endDate);
        }
    }
    
    /**
     * Sets (as xml) the "endDate" element
     */
    public void xsetEndDate(gov.loc.premis.v3.EdtfSimpleType endDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(ENDDATE$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(ENDDATE$0);
            }
            target.set(endDate);
        }
    }
}
