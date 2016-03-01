/*
 * An XML document type.
 * Localname: statuteDocumentationIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteDocumentationIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteDocumentationIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteDocumentationIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteDocumentationIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteDocumentationIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifierValue");
    
    
    /**
     * Gets the "statuteDocumentationIdentifierValue" element
     */
    public java.lang.String getStatuteDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "statuteDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetStatuteDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "statuteDocumentationIdentifierValue" element
     */
    public void setStatuteDocumentationIdentifierValue(java.lang.String statuteDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.setStringValue(statuteDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "statuteDocumentationIdentifierValue" element
     */
    public void xsetStatuteDocumentationIdentifierValue(org.apache.xmlbeans.XmlString statuteDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.set(statuteDocumentationIdentifierValue);
        }
    }
}
