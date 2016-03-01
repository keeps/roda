/*
 * An XML document type.
 * Localname: linkingRightsStatementIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingRightsStatementIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingRightsStatementIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingRightsStatementIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingRightsStatementIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingRightsStatementIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifier");
    
    
    /**
     * Gets the "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType getLinkingRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingRightsStatementIdentifier" element
     */
    public void setLinkingRightsStatementIdentifier(gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType linkingRightsStatementIdentifier)
    {
        generatedSetterHelperImpl(linkingRightsStatementIdentifier, LINKINGRIGHTSSTATEMENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType addNewLinkingRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$0);
            return target;
        }
    }
}
