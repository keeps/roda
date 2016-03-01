/*
 * An XML document type.
 * Localname: relatedEventIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEventIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEventIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEventIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEventIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifier");
    
    
    /**
     * Gets the "relatedEventIdentifier" element
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType getRelatedEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().find_element_user(RELATEDEVENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventIdentifier" element
     */
    public void setRelatedEventIdentifier(gov.loc.premis.v3.RelatedEventIdentifierComplexType relatedEventIdentifier)
    {
        generatedSetterHelperImpl(relatedEventIdentifier, RELATEDEVENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedEventIdentifier" element
     */
    public gov.loc.premis.v3.RelatedEventIdentifierComplexType addNewRelatedEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedEventIdentifierComplexType)get_store().add_element_user(RELATEDEVENTIDENTIFIER$0);
            return target;
        }
    }
}
