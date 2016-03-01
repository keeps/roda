/*
 * XML Type:  significantPropertiesComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML significantPropertiesComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class SignificantPropertiesComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignificantPropertiesComplexType
{
    private static final long serialVersionUID = 1L;
    
    public SignificantPropertiesComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesType");
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesValue");
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESEXTENSION$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesExtension");
    
    
    /**
     * Gets the "significantPropertiesType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNIFICANTPROPERTIESTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "significantPropertiesType" element
     */
    public boolean isSetSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNIFICANTPROPERTIESTYPE$0) != 0;
        }
    }
    
    /**
     * Sets the "significantPropertiesType" element
     */
    public void setSignificantPropertiesType(gov.loc.premis.v3.StringPlusAuthority significantPropertiesType)
    {
        generatedSetterHelperImpl(significantPropertiesType, SIGNIFICANTPROPERTIESTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "significantPropertiesType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNIFICANTPROPERTIESTYPE$0);
            return target;
        }
    }
    
    /**
     * Unsets the "significantPropertiesType" element
     */
    public void unsetSignificantPropertiesType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNIFICANTPROPERTIESTYPE$0, 0);
        }
    }
    
    /**
     * Gets the "significantPropertiesValue" element
     */
    public java.lang.String getSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "significantPropertiesValue" element
     */
    public org.apache.xmlbeans.XmlString xgetSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "significantPropertiesValue" element
     */
    public boolean isSetSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNIFICANTPROPERTIESVALUE$2) != 0;
        }
    }
    
    /**
     * Sets the "significantPropertiesValue" element
     */
    public void setSignificantPropertiesValue(java.lang.String significantPropertiesValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNIFICANTPROPERTIESVALUE$2);
            }
            target.setStringValue(significantPropertiesValue);
        }
    }
    
    /**
     * Sets (as xml) the "significantPropertiesValue" element
     */
    public void xsetSignificantPropertiesValue(org.apache.xmlbeans.XmlString significantPropertiesValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNIFICANTPROPERTIESVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNIFICANTPROPERTIESVALUE$2);
            }
            target.set(significantPropertiesValue);
        }
    }
    
    /**
     * Unsets the "significantPropertiesValue" element
     */
    public void unsetSignificantPropertiesValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNIFICANTPROPERTIESVALUE$2, 0);
        }
    }
    
    /**
     * Gets array of all "significantPropertiesExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getSignificantPropertiesExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNIFICANTPROPERTIESEXTENSION$4, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "significantPropertiesExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getSignificantPropertiesExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIESEXTENSION$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "significantPropertiesExtension" element
     */
    public int sizeOfSignificantPropertiesExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNIFICANTPROPERTIESEXTENSION$4);
        }
    }
    
    /**
     * Sets array of all "significantPropertiesExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setSignificantPropertiesExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] significantPropertiesExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(significantPropertiesExtensionArray, SIGNIFICANTPROPERTIESEXTENSION$4);
    }
    
    /**
     * Sets ith "significantPropertiesExtension" element
     */
    public void setSignificantPropertiesExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType significantPropertiesExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIESEXTENSION$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(significantPropertiesExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "significantPropertiesExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewSignificantPropertiesExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(SIGNIFICANTPROPERTIESEXTENSION$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "significantPropertiesExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewSignificantPropertiesExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(SIGNIFICANTPROPERTIESEXTENSION$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "significantPropertiesExtension" element
     */
    public void removeSignificantPropertiesExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNIFICANTPROPERTIESEXTENSION$4, i);
        }
    }
}
