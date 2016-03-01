/*
 * An XML document type.
 * Localname: linkingAgentIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingAgentIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingAgentIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingAgentIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingAgentIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifierType");
    
    
    /**
     * Gets the "linkingAgentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGAGENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentIdentifierType" element
     */
    public void setLinkingAgentIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingAgentIdentifierType)
    {
        generatedSetterHelperImpl(linkingAgentIdentifierType, LINKINGAGENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingAgentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGAGENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
