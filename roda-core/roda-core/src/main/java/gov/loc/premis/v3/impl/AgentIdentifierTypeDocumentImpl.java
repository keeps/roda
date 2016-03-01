/*
 * An XML document type.
 * Localname: agentIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentIdentifierType");
    
    
    /**
     * Gets the "agentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agentIdentifierType" element
     */
    public void setAgentIdentifierType(gov.loc.premis.v3.StringPlusAuthority agentIdentifierType)
    {
        generatedSetterHelperImpl(agentIdentifierType, AGENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(AGENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
