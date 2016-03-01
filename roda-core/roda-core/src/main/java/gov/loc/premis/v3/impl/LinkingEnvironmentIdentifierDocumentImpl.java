/*
 * An XML document type.
 * Localname: linkingEnvironmentIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEnvironmentIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingEnvironmentIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingEnvironmentIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEnvironmentIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEnvironmentIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifier");
    
    
    /**
     * Gets the "linkingEnvironmentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType getLinkingEnvironmentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingEnvironmentIdentifier" element
     */
    public void setLinkingEnvironmentIdentifier(gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType linkingEnvironmentIdentifier)
    {
        generatedSetterHelperImpl(linkingEnvironmentIdentifier, LINKINGENVIRONMENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingEnvironmentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType addNewLinkingEnvironmentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIER$0);
            return target;
        }
    }
}
