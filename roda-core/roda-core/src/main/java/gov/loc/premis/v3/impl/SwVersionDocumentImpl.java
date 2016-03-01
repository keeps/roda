/*
 * An XML document type.
 * Localname: swVersion
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SwVersionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one swVersion(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SwVersionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SwVersionDocument
{
    private static final long serialVersionUID = 1L;
    
    public SwVersionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SWVERSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "swVersion");
    
    
    /**
     * Gets the "swVersion" element
     */
    public java.lang.String getSwVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SWVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "swVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetSwVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SWVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "swVersion" element
     */
    public void setSwVersion(java.lang.String swVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SWVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SWVERSION$0);
            }
            target.setStringValue(swVersion);
        }
    }
    
    /**
     * Sets (as xml) the "swVersion" element
     */
    public void xsetSwVersion(org.apache.xmlbeans.XmlString swVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SWVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SWVERSION$0);
            }
            target.set(swVersion);
        }
    }
}
