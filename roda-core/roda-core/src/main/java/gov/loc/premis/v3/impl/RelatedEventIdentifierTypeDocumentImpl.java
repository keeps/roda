/*
 * An XML document type.
 * Localname: relatedEventIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEventIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEventIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEventIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEventIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifierType");
    
    
    /**
     * Gets the "relatedEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelatedEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATEDEVENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventIdentifierType" element
     */
    public void setRelatedEventIdentifierType(gov.loc.premis.v3.StringPlusAuthority relatedEventIdentifierType)
    {
        generatedSetterHelperImpl(relatedEventIdentifierType, RELATEDEVENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relatedEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelatedEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATEDEVENTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
