/*
 * An XML document type.
 * Localname: linkingAgentIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingAgentIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingAgentIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingAgentIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingAgentIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifier");
    
    
    /**
     * Gets the "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType getLinkingAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().find_element_user(LINKINGAGENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentIdentifier" element
     */
    public void setLinkingAgentIdentifier(gov.loc.premis.v3.LinkingAgentIdentifierComplexType linkingAgentIdentifier)
    {
        generatedSetterHelperImpl(linkingAgentIdentifier, LINKINGAGENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType addNewLinkingAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().add_element_user(LINKINGAGENTIDENTIFIER$0);
            return target;
        }
    }
}
