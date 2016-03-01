/*
 * An XML document type.
 * Localname: creatingApplicationVersion
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CreatingApplicationVersionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one creatingApplicationVersion(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CreatingApplicationVersionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CreatingApplicationVersionDocument
{
    private static final long serialVersionUID = 1L;
    
    public CreatingApplicationVersionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CREATINGAPPLICATIONVERSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "creatingApplicationVersion");
    
    
    /**
     * Gets the "creatingApplicationVersion" element
     */
    public java.lang.String getCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CREATINGAPPLICATIONVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "creatingApplicationVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetCreatingApplicationVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CREATINGAPPLICATIONVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "creatingApplicationVersion" element
     */
    public void setCreatingApplicationVersion(java.lang.String creatingApplicationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(CREATINGAPPLICATIONVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(CREATINGAPPLICATIONVERSION$0);
            }
            target.setStringValue(creatingApplicationVersion);
        }
    }
    
    /**
     * Sets (as xml) the "creatingApplicationVersion" element
     */
    public void xsetCreatingApplicationVersion(org.apache.xmlbeans.XmlString creatingApplicationVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(CREATINGAPPLICATIONVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(CREATINGAPPLICATIONVERSION$0);
            }
            target.set(creatingApplicationVersion);
        }
    }
}
