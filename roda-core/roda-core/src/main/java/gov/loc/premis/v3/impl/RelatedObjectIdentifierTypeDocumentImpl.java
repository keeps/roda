/*
 * An XML document type.
 * Localname: relatedObjectIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedObjectIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedObjectIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedObjectIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedObjectIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifierType");
    
    
    /**
     * Gets the "relatedObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDOBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectIdentifierType" element
     */
    public void setRelatedObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedObjectIdentifierType)
    {
        generatedSetterHelperImpl(relatedObjectIdentifierType, RELATEDOBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDOBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
