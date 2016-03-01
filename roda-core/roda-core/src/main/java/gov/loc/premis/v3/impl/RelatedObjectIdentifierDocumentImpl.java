/*
 * An XML document type.
 * Localname: relatedObjectIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedObjectIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedObjectIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedObjectIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedObjectIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifier");
    
    
    /**
     * Gets the "relatedObjectIdentifier" element
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType getRelatedObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().find_element_user(RELATEDOBJECTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectIdentifier" element
     */
    public void setRelatedObjectIdentifier(gov.loc.premis.v3.RelatedObjectIdentifierComplexType relatedObjectIdentifier)
    {
        generatedSetterHelperImpl(relatedObjectIdentifier, RELATEDOBJECTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedObjectIdentifier" element
     */
    public gov.loc.premis.v3.RelatedObjectIdentifierComplexType addNewRelatedObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelatedObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RelatedObjectIdentifierComplexType)get_store().add_element_user(RELATEDOBJECTIDENTIFIER$0);
            return target;
        }
    }
}
