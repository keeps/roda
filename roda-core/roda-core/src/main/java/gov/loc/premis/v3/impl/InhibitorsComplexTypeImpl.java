/*
 * XML Type:  inhibitorsComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.InhibitorsComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML inhibitorsComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class InhibitorsComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.InhibitorsComplexType
{
    private static final long serialVersionUID = 1L;
    
    public InhibitorsComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INHIBITORTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorType");
    private static final javax.xml.namespace.QName INHIBITORTARGET$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorTarget");
    private static final javax.xml.namespace.QName INHIBITORKEY$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorKey");
    
    
    /**
     * Gets the "inhibitorType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getInhibitorType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(INHIBITORTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "inhibitorType" element
     */
    public void setInhibitorType(gov.loc.premis.v3.StringPlusAuthority inhibitorType)
    {
        generatedSetterHelperImpl(inhibitorType, INHIBITORTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "inhibitorType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewInhibitorType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(INHIBITORTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets array of all "inhibitorTarget" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getInhibitorTargetArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(INHIBITORTARGET$2, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "inhibitorTarget" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getInhibitorTargetArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(INHIBITORTARGET$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "inhibitorTarget" element
     */
    public int sizeOfInhibitorTargetArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INHIBITORTARGET$2);
        }
    }
    
    /**
     * Sets array of all "inhibitorTarget" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setInhibitorTargetArray(gov.loc.premis.v3.StringPlusAuthority[] inhibitorTargetArray)
    {
        check_orphaned();
        arraySetterHelper(inhibitorTargetArray, INHIBITORTARGET$2);
    }
    
    /**
     * Sets ith "inhibitorTarget" element
     */
    public void setInhibitorTargetArray(int i, gov.loc.premis.v3.StringPlusAuthority inhibitorTarget)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(INHIBITORTARGET$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(inhibitorTarget);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "inhibitorTarget" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewInhibitorTarget(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(INHIBITORTARGET$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "inhibitorTarget" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewInhibitorTarget()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(INHIBITORTARGET$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "inhibitorTarget" element
     */
    public void removeInhibitorTarget(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INHIBITORTARGET$2, i);
        }
    }
    
    /**
     * Gets the "inhibitorKey" element
     */
    public java.lang.String getInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INHIBITORKEY$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "inhibitorKey" element
     */
    public org.apache.xmlbeans.XmlString xgetInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INHIBITORKEY$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "inhibitorKey" element
     */
    public boolean isSetInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(INHIBITORKEY$4) != 0;
        }
    }
    
    /**
     * Sets the "inhibitorKey" element
     */
    public void setInhibitorKey(java.lang.String inhibitorKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(INHIBITORKEY$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(INHIBITORKEY$4);
            }
            target.setStringValue(inhibitorKey);
        }
    }
    
    /**
     * Sets (as xml) the "inhibitorKey" element
     */
    public void xsetInhibitorKey(org.apache.xmlbeans.XmlString inhibitorKey)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(INHIBITORKEY$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(INHIBITORKEY$4);
            }
            target.set(inhibitorKey);
        }
    }
    
    /**
     * Unsets the "inhibitorKey" element
     */
    public void unsetInhibitorKey()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(INHIBITORKEY$4, 0);
        }
    }
}
