/*
 * An XML document type.
 * Localname: linkingEventIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEventIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEventIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEventIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEventIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEventIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifierValue");
    
    
    /**
     * Gets the "linkingEventIdentifierValue" element
     */
    public java.lang.String getLinkingEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEventIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEventIdentifierValue" element
     */
    public void setLinkingEventIdentifierValue(java.lang.String linkingEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGEVENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(linkingEventIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEventIdentifierValue" element
     */
    public void xsetLinkingEventIdentifierValue(org.apache.xmlbeans.XmlString linkingEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGEVENTIDENTIFIERVALUE$0);
            }
            target.set(linkingEventIdentifierValue);
        }
    }
}
