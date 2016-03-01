/*
 * An XML document type.
 * Localname: environmentRegistryKey
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentRegistryKeyDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentRegistryKey(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentRegistryKeyDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentRegistryKeyDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentRegistryKeyDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYKEY$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryKey");
    
    
    /**
     * Gets the "environmentRegistryKey" element
     */
    public java.lang.String getEnvironmentRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentRegistryKey" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentRegistryKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistryKey" element
     */
    public void setEnvironmentRegistryKey(java.lang.String environmentRegistryKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTREGISTRYKEY$0);
            }
            target.setStringValue(environmentRegistryKey);
        }
    }
    
    /**
     * Sets (as xml) the "environmentRegistryKey" element
     */
    public void xsetEnvironmentRegistryKey(org.apache.xmlbeans.XmlString environmentRegistryKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYKEY$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTREGISTRYKEY$0);
            }
            target.set(environmentRegistryKey);
        }
    }
}
