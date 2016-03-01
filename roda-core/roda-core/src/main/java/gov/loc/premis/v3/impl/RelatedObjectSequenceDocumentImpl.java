/*
 * An XML document type.
 * Localname: relatedObjectSequence
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelatedObjectSequenceDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relatedObjectSequence(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelatedObjectSequenceDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelatedObjectSequenceDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelatedObjectSequenceDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATEDOBJECTSEQUENCE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relatedObjectSequence");
    
    
    /**
     * Gets the "relatedObjectSequence" element
     */
    public java.math.BigInteger getRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTSEQUENCE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getBigIntegerValue();
        }
    }
    
    /**
     * Gets (as xml) the "relatedObjectSequence" element
     */
    public org.apache.xmlbeans.XmlNonNegativeInteger xgetRelatedObjectSequence()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDOBJECTSEQUENCE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "relatedObjectSequence" element
     */
    public void setRelatedObjectSequence(java.math.BigInteger relatedObjectSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RELATEDOBJECTSEQUENCE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RELATEDOBJECTSEQUENCE$0);
            }
            target.setBigIntegerValue(relatedObjectSequence);
        }
    }
    
    /**
     * Sets (as xml) the "relatedObjectSequence" element
     */
    public void xsetRelatedObjectSequence(org.apache.xmlbeans.XmlNonNegativeInteger relatedObjectSequence)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlNonNegativeInteger target = null;
            target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().find_element_user(RELATEDOBJECTSEQUENCE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlNonNegativeInteger)get_store().add_element_user(RELATEDOBJECTSEQUENCE$0);
            }
            target.set(relatedObjectSequence);
        }
    }
}
