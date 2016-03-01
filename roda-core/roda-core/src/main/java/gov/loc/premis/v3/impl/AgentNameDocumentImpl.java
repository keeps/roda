/*
 * An XML document type.
 * Localname: agentName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentName");
    
    
    /**
     * Gets the "agentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAgentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agentName" element
     */
    public void setAgentName(gov.loc.premis.v3.StringPlusAuthority agentName)
    {
        generatedSetterHelperImpl(agentName, AGENTNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAgentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(AGENTNAME$0);
            return target;
        }
    }
}
