/*
 * An XML document type.
 * Localname: size
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SizeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one size(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SizeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SizeDocument
{
    private static final long serialVersionUID = 1L;
    
    public SizeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIZE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "size");
    
    
    /**
     * Gets the "size" element
     */
    public long getSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIZE$0, 0);
            if (target == null)
            {
                return 0L;
            }
            return target.getLongValue();
        }
    }
    
    /**
     * Gets (as xml) the "size" element
     */
    public org.apache.xmlbeans.XmlLong xgetSize()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlLong target = null;
            target = (org.apache.xmlbeans.XmlLong)get_store().find_element_user(SIZE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "size" element
     */
    public void setSize(long size)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIZE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIZE$0);
            }
            target.setLongValue(size);
        }
    }
    
    /**
     * Sets (as xml) the "size" element
     */
    public void xsetSize(org.apache.xmlbeans.XmlLong size)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlLong target = null;
            target = (org.apache.xmlbeans.XmlLong)get_store().find_element_user(SIZE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlLong)get_store().add_element_user(SIZE$0);
            }
            target.set(size);
        }
    }
}
