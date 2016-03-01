/*
 * XML Type:  environmentFunctionComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentFunctionComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML environmentFunctionComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EnvironmentFunctionComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentFunctionComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentFunctionComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTFUNCTIONTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentFunctionType");
    private static final javax.xml.namespace.QName ENVIRONMENTFUNCTIONLEVEL$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentFunctionLevel");
    
    
    /**
     * Gets the "environmentFunctionType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentFunctionType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTFUNCTIONTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentFunctionType" element
     */
    public void setEnvironmentFunctionType(gov.loc.premis.v3.StringPlusAuthority environmentFunctionType)
    {
        generatedSetterHelperImpl(environmentFunctionType, ENVIRONMENTFUNCTIONTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentFunctionType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentFunctionType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTFUNCTIONTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "environmentFunctionLevel" element
     */
    public java.lang.String getEnvironmentFunctionLevel()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$2, 0);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$2, 0);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTFUNCTIONLEVEL$2);
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
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTFUNCTIONLEVEL$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTFUNCTIONLEVEL$2);
            }
            target.set(environmentFunctionLevel);
        }
    }
}
