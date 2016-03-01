/*
 * An XML document type.
 * Localname: rightsStatementIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one rightsStatementIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RightsStatementIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifierValue");
    
    
    /**
     * Gets the "rightsStatementIdentifierValue" element
     */
    public java.lang.String getRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "rightsStatementIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRightsStatementIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifierValue" element
     */
    public void setRightsStatementIdentifierValue(java.lang.String rightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(rightsStatementIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "rightsStatementIdentifierValue" element
     */
    public void xsetRightsStatementIdentifierValue(org.apache.xmlbeans.XmlString rightsStatementIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIERVALUE$0);
            }
            target.set(rightsStatementIdentifierValue);
        }
    }
}
