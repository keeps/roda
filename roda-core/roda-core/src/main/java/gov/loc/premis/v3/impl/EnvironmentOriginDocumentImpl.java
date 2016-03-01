/*
 * An XML document type.
 * Localname: environmentOrigin
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentOriginDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentOrigin(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentOriginDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentOriginDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentOriginDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTORIGIN$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentOrigin");
    
    
    /**
     * Gets the "environmentOrigin" element
     */
    public java.lang.String getEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTORIGIN$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentOrigin" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTORIGIN$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentOrigin" element
     */
    public void setEnvironmentOrigin(java.lang.String environmentOrigin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTORIGIN$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTORIGIN$0);
            }
            target.setStringValue(environmentOrigin);
        }
    }
    
    /**
     * Sets (as xml) the "environmentOrigin" element
     */
    public void xsetEnvironmentOrigin(org.apache.xmlbeans.XmlString environmentOrigin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTORIGIN$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTORIGIN$0);
            }
            target.set(environmentOrigin);
        }
    }
}
