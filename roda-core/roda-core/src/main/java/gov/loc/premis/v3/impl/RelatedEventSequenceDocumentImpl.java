/*
 * An XML document type.
 * Localname: relatedEventSequence
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedEventSequenceDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedEventSequence(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedEventSequenceDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedEventSequenceDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedEventSequenceDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDEVENTSEQUENCE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedEventSequence");
    
    
    /**
     * Gets the "relatedEventSequence" element
     */
    public java.math.BigInteger getRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTSEQUENCE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedEventSequence" element
     */
    public org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedEventSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDEVENTSEQUENCE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedEventSequence" element
     */
    public void setRelatedEventSequence(java.math.BigInteger relatedEventSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDEVENTSEQUENCE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDEVENTSEQUENCE$0);
            }
            target.setBigIntegerValue(relatedEventSequence);
        }
    }
    
    /**
     * Sets (as xml) the "relatedEventSequence" element
     */
    public void xsetRelatedEventSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedEventSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDEVENTSEQUENCE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().add_element_user(RELATEDEVENTSEQUENCE$0);
            }
            target.set(relatedEventSequence);
        }
    }
}
