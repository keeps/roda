/*
 * An XML document type.
 * Localname: agentType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentType");
    
    
    /**
     * Gets the "agentType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agentType" element
     */
    public void setAgentType(gov.loc.premis.v3.StringPlusAuthority agentType)
    {
        generatedSetterHelperImpl(agentType, AGENTTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(AGENTTYPE$0);
            return target;
        }
    }
}
