/*
 * An XML document type.
 * Localname: linkingEventIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEventIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEventIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEventIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEventIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEventIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifier");
    
    
    /**
     * Gets the "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType getLinkingEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().find_element_user(LINKINGEVENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingEventIdentifier" element
     */
    public void setLinkingEventIdentifier(gov.loc.premis.v3.LinkingEventIdentifierComplexType linkingEventIdentifier)
    {
        generatedSetterHelperImpl(linkingEventIdentifier, LINKINGEVENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType addNewLinkingEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().add_element_user(LINKINGEVENTIDENTIFIER$0);
            return target;
        }
    }
}
