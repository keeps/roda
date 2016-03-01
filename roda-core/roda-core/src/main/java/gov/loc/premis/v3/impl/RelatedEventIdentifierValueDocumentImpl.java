/*
 * An XML document type.
 * Localname: relatedEventIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEventIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEventIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEventIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEventIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDEVENTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventIdentifierValue");
    
    
    /**
     * Gets the "relatedEventIdentifierValue" element
     */
    public java.lang.String getRelatedEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedEventIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetRelatedEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventIdentifierValue" element
     */
    public void setRelatedEventIdentifierValue(java.lang.String relatedEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDEVENTIDENTIFIERVALUE$0);
            }
            target.setStringValue(relatedEventIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "relatedEventIdentifierValue" element
     */
    public void xsetRelatedEventIdentifierValue(org.apache.xmlbeans.XmlString relatedEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RELATEDEVENTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RELATEDEVENTIDENTIFIERVALUE$0);
            }
            target.set(relatedEventIdentifierValue);
        }
    }
}
