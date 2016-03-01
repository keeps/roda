/*
 * An XML document type.
 * Localname: statuteInformationDeterminationDate
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteInformationDeterminationDateDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteInformationDeterminationDate(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteInformationDeterminationDateDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteInformationDeterminationDateDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteInformationDeterminationDateDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEINFORMATIONDETERMINATIONDATE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteInformationDeterminationDate");
    
    
    /**
     * Gets the "statuteInformationDeterminationDate" element
     */
    public java.lang.String getStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "statuteInformationDeterminationDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "statuteInformationDeterminationDate" element
     */
    public void setStatuteInformationDeterminationDate(java.lang.String statuteInformationDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0);
            }
            target.setStringValue(statuteInformationDeterminationDate);
        }
    }
    
    /**
     * Sets (as xml) the "statuteInformationDeterminationDate" element
     */
    public void xsetStatuteInformationDeterminationDate(gov.loc.premis.v3.EdtfSimpleType statuteInformationDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(STATUTEINFORMATIONDETERMINATIONDATE$0);
            }
            target.set(statuteInformationDeterminationDate);
        }
    }
}
