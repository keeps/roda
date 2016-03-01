/*
 * An XML document type.
 * Localname: agentExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentExtension");
    
    
    /**
     * Gets the "agentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getAgentExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(AGENTEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "agentExtension" element
     */
    public void setAgentExtension(gov.loc.premis.v3.ExtensionComplexType agentExtension)
    {
        generatedSetterHelperImpl(agentExtension, AGENTEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewAgentExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(AGENTEXTENSION$0);
            return target;
        }
    }
}
