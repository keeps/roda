/*
 * An XML document type.
 * Localname: environmentVersion
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentVersionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentVersion(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentVersionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentVersionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentVersionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTVERSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentVersion");
    
    
    /**
     * Gets the "environmentVersion" element
     */
    public java.lang.String getEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTVERSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTVERSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentVersion" element
     */
    public void setEnvironmentVersion(java.lang.String environmentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTVERSION$0);
            }
            target.setStringValue(environmentVersion);
        }
    }
    
    /**
     * Sets (as xml) the "environmentVersion" element
     */
    public void xsetEnvironmentVersion(org.apache.xmlbeans.XmlString environmentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTVERSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTVERSION$0);
            }
            target.set(environmentVersion);
        }
    }
}
