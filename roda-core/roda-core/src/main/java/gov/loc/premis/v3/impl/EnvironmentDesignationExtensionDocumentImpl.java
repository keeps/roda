/*
 * An XML document type.
 * Localname: environmentDesignationExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentDesignationExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentDesignationExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentDesignationExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentDesignationExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentDesignationExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTDESIGNATIONEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentDesignationExtension");
    
    
    /**
     * Gets the "environmentDesignationExtension" element
     */
    public java.lang.String getEnvironmentDesignationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentDesignationExtension" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentDesignationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentDesignationExtension" element
     */
    public void setEnvironmentDesignationExtension(java.lang.String environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0);
            }
            target.setStringValue(environmentDesignationExtension);
        }
    }
    
    /**
     * Sets (as xml) the "environmentDesignationExtension" element
     */
    public void xsetEnvironmentDesignationExtension(org.apache.xmlbeans.XmlString environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTDESIGNATIONEXTENSION$0);
            }
            target.set(environmentDesignationExtension);
        }
    }
}
