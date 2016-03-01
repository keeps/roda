/*
 * An XML document type.
 * Localname: agentVersion
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentVersionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentVersion(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentVersionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentVersionDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentVersionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTVERSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentVersion");
    
    
    /**
     * Gets the "agentVersion" element
     */
    public java.lang.String getAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "agentVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "agentVersion" element
     */
    public void setAgentVersion(java.lang.String agentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AGENTVERSION$0);
            }
            target.setStringValue(agentVersion);
        }
    }
    
    /**
     * Sets (as xml) the "agentVersion" element
     */
    public void xsetAgentVersion(org.apache.xmlbeans.XmlString agentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AGENTVERSION$0);
            }
            target.set(agentVersion);
        }
    }
}
