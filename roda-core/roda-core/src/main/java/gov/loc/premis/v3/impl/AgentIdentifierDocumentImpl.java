/*
 * An XML document type.
 * Localname: agentIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentIdentifier");
    
    
    /**
     * Gets the "agentIdentifier" element
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType getAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().find_element_user(AGENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agentIdentifier" element
     */
    public void setAgentIdentifier(gov.loc.premis.v3.AgentIdentifierComplexType agentIdentifier)
    {
        generatedSetterHelperImpl(agentIdentifier, AGENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentIdentifier" element
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType addNewAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().add_element_user(AGENTIDENTIFIER$0);
            return target;
        }
    }
}
