/*
 * An XML document type.
 * Localname: environmentRegistryName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentRegistryNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentRegistryName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentRegistryNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentRegistryNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentRegistryNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTREGISTRYNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentRegistryName");
    
    
    /**
     * Gets the "environmentRegistryName" element
     */
    public java.lang.String getEnvironmentRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentRegistryName" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentRegistryName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentRegistryName" element
     */
    public void setEnvironmentRegistryName(java.lang.String environmentRegistryName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTREGISTRYNAME$0);
            }
            target.setStringValue(environmentRegistryName);
        }
    }
    
    /**
     * Sets (as xml) the "environmentRegistryName" element
     */
    public void xsetEnvironmentRegistryName(org.apache.xmlbeans.XmlString environmentRegistryName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTREGISTRYNAME$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTREGISTRYNAME$0);
            }
            target.set(environmentRegistryName);
        }
    }
}
