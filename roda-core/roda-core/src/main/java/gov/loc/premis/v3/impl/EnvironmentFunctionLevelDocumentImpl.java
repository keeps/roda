/*
 * An XML document type.
 * Localname: environmentFunctionLevel
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentFunctionLevelDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentFunctionLevel(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentFunctionLevelDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentFunctionLevelDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentFunctionLevelDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTFUNCTIONLEVEL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentFunctionLevel");
    
    
    /**
     * Gets the "environmentFunctionLevel" element
     */
    public java.lang.String getEnvironmentFunctionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentFunctionLevel" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentFunctionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "environmentFunctionLevel" element
     */
    public void setEnvironmentFunctionLevel(java.lang.String environmentFunctionLevel)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTFUNCTIONLEVEL$0);
            }
            target.setStringValue(environmentFunctionLevel);
        }
    }
    
    /**
     * Sets (as xml) the "environmentFunctionLevel" element
     */
    public void xsetEnvironmentFunctionLevel(org.apache.xmlbeans.XmlString environmentFunctionLevel)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTFUNCTIONLEVEL$0);
            }
            target.set(environmentFunctionLevel);
        }
    }
}
