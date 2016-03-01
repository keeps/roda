/*
 * An XML document type.
 * Localname: linkingRightsStatementIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingRightsStatementIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingRightsStatementIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingRightsStatementIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingRightsStatementIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingRightsStatementIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifierType");
    
    
    /**
     * Gets the "linkingRightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingRightsStatementIdentifierType" element
     */
    public void setLinkingRightsStatementIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingRightsStatementIdentifierType)
    {
        generatedSetterHelperImpl(linkingRightsStatementIdentifierType, LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingRightsStatementIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingRightsStatementIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
