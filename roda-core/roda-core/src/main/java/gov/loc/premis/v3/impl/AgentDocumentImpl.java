/*
 * An XML document type.
 * Localname: agent
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agent(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agent");
    
    
    /**
     * Gets the "agent" element
     */
    public gov.loc.premis.v3.AgentComplexType getAgent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().find_element_user(AGENT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agent" element
     */
    public void setAgent(gov.loc.premis.v3.AgentComplexType agent)
    {
        generatedSetterHelperImpl(agent, AGENT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agent" element
     */
    public gov.loc.premis.v3.AgentComplexType addNewAgent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().add_element_user(AGENT$0);
            return target;
        }
    }
}
