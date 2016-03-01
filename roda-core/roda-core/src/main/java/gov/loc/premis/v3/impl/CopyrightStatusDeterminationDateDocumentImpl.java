/*
 * An XML document type.
 * Localname: copyrightStatusDeterminationDate
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightStatusDeterminationDateDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightStatusDeterminationDate(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightStatusDeterminationDateDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightStatusDeterminationDateDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightStatusDeterminationDateDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTSTATUSDETERMINATIONDATE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightStatusDeterminationDate");
    
    
    /**
     * Gets the "copyrightStatusDeterminationDate" element
     */
    public java.lang.String getCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "copyrightStatusDeterminationDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "copyrightStatusDeterminationDate" element
     */
    public void setCopyrightStatusDeterminationDate(java.lang.String copyrightStatusDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0);
            }
            target.setStringValue(copyrightStatusDeterminationDate);
        }
    }
    
    /**
     * Sets (as xml) the "copyrightStatusDeterminationDate" element
     */
    public void xsetCopyrightStatusDeterminationDate(gov.loc.premis.v3.EdtfSimpleType copyrightStatusDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$0);
            }
            target.set(copyrightStatusDeterminationDate);
        }
    }
}
