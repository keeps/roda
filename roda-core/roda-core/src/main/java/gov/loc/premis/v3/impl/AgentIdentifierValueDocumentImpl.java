/*
 * An XML document type.
 * Localname: agentIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentIdentifierValue");
    
    
    /**
     * Gets the "agentIdentifierValue" element
     */
    public java.lang.String getAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "agentIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "agentIdentifierValue" element
     */
    public void setAgentIdentifierValue(java.lang.String agentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AGENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(agentIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "agentIdentifierValue" element
     */
    public void xsetAgentIdentifierValue(org.apache.xmlbeans.XmlString agentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AGENTIDENTIFIERVALUE$0);
            }
            target.set(agentIdentifierValue);
        }
    }
}
