/*
 * An XML document type.
 * Localname: relatedObjectIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedObjectIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedObjectIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedObjectIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedObjectIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDOBJECTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectIdentifierValue");
    
    
    /**
     * Gets the "relatedObjectIdentifierValue" element
     */
    public java.lang.String getRelatedObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedObjectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRelatedObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectIdentifierValue" element
     */
    public void setRelatedObjectIdentifierValue(java.lang.String relatedObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDOBJECTIDENTIFIERVALUE$0);
            }
            target.setStringValue(relatedObjectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "relatedObjectIdentifierValue" element
     */
    public void xsetRelatedObjectIdentifierValue(org.apache.xmlbeans.XmlString relatedObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDOBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RELATEDOBJECTIDENTIFIERVALUE$0);
            }
            target.set(relatedObjectIdentifierValue);
        }
    }
}
