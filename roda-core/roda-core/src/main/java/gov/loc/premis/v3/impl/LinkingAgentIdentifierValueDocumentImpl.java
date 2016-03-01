/*
 * An XML document type.
 * Localname: linkingAgentIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingAgentIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingAgentIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingAgentIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingAgentIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifierValue");
    
    
    /**
     * Gets the "linkingAgentIdentifierValue" element
     */
    public java.lang.String getLinkingAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingAgentIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentIdentifierValue" element
     */
    public void setLinkingAgentIdentifierValue(java.lang.String linkingAgentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGAGENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(linkingAgentIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingAgentIdentifierValue" element
     */
    public void xsetLinkingAgentIdentifierValue(org.apache.xmlbeans.XmlString linkingAgentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGAGENTIDENTIFIERVALUE$0);
            }
            target.set(linkingAgentIdentifierValue);
        }
    }
}
