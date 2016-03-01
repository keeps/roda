/*
 * An XML document type.
 * Localname: linkingObjectIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one linkingObjectIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LinkingObjectIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingObjectIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LinkingObjectIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifier");
    
    
    /**
     * Gets the "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType getLinkingObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().find_element_user(LINKINGOBJECTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectIdentifier" element
     */
    public void setLinkingObjectIdentifier(gov.loc.premis.v3.LinkingObjectIdentifierComplexType linkingObjectIdentifier)
    {
        generatedSetterHelperImpl(linkingObjectIdentifier, LINKINGOBJECTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType addNewLinkingObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().add_element_user(LINKINGOBJECTIDENTIFIER$0);
            return target;
        }
    }
}
