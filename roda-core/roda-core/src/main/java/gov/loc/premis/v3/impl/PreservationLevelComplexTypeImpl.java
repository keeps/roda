/*
 * XML Type:  preservationLevelComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML preservationLevelComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class PreservationLevelComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelComplexType
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelType");
    private static final javax.xml.namespace.QName PRESERVATIONLEVELVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelValue");
    private static final javax.xml.namespace.QName PRESERVATIONLEVELROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelRole");
    private static final javax.xml.namespace.QName PRESERVATIONLEVELRATIONALE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelRationale");
    private static final javax.xml.namespace.QName PRESERVATIONLEVELDATEASSIGNED$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelDateAssigned");
    
    
    /**
     * Gets the "preservationLevelType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "preservationLevelType" element
     */
    public boolean isSetPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PRESERVATIONLEVELTYPE$0) != 0;
        }
    }
    
    /**
     * Sets the "preservationLevelType" element
     */
    public void setPreservationLevelType(gov.loc.premis.v3.StringPlusAuthority preservationLevelType)
    {
        generatedSetterHelperImpl(preservationLevelType, PRESERVATIONLEVELTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELTYPE$0);
            return target;
        }
    }
    
    /**
     * Unsets the "preservationLevelType" element
     */
    public void unsetPreservationLevelType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PRESERVATIONLEVELTYPE$0, 0);
        }
    }
    
    /**
     * Gets the "preservationLevelValue" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelValue" element
     */
    public void setPreservationLevelValue(gov.loc.premis.v3.StringPlusAuthority preservationLevelValue)
    {
        generatedSetterHelperImpl(preservationLevelValue, PRESERVATIONLEVELVALUE$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelValue" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELVALUE$2);
            return target;
        }
    }
    
    /**
     * Gets the "preservationLevelRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "preservationLevelRole" element
     */
    public boolean isSetPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PRESERVATIONLEVELROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "preservationLevelRole" element
     */
    public void setPreservationLevelRole(gov.loc.premis.v3.StringPlusAuthority preservationLevelRole)
    {
        generatedSetterHelperImpl(preservationLevelRole, PRESERVATIONLEVELROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "preservationLevelRole" element
     */
    public void unsetPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PRESERVATIONLEVELROLE$4, 0);
        }
    }
    
    /**
     * Gets array of all "preservationLevelRationale" elements
     */
    public java.lang.String[] getPreservationLevelRationaleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PRESERVATIONLEVELRATIONALE$6, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "preservationLevelRationale" element
     */
    public java.lang.String getPreservationLevelRationaleArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "preservationLevelRationale" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetPreservationLevelRationaleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PRESERVATIONLEVELRATIONALE$6, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "preservationLevelRationale" element
     */
    public org.apache.xmlbeans.XmlString xgetPreservationLevelRationaleArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "preservationLevelRationale" element
     */
    public int sizeOfPreservationLevelRationaleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PRESERVATIONLEVELRATIONALE$6);
        }
    }
    
    /**
     * Sets array of all "preservationLevelRationale" element
     */
    public void setPreservationLevelRationaleArray(java.lang.String[] preservationLevelRationaleArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(preservationLevelRationaleArray, PRESERVATIONLEVELRATIONALE$6);
        }
    }
    
    /**
     * Sets ith "preservationLevelRationale" element
     */
    public void setPreservationLevelRationaleArray(int i, java.lang.String preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(preservationLevelRationale);
        }
    }
    
    /**
     * Sets (as xml) array of all "preservationLevelRationale" element
     */
    public void xsetPreservationLevelRationaleArray(org.apache.xmlbeans.XmlString[]preservationLevelRationaleArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(preservationLevelRationaleArray, PRESERVATIONLEVELRATIONALE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "preservationLevelRationale" element
     */
    public void xsetPreservationLevelRationaleArray(int i, org.apache.xmlbeans.XmlString preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(preservationLevelRationale);
        }
    }
    
    /**
     * Inserts the value as the ith "preservationLevelRationale" element
     */
    public void insertPreservationLevelRationale(int i, java.lang.String preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            target.setStringValue(preservationLevelRationale);
        }
    }
    
    /**
     * Appends the value as the last "preservationLevelRationale" element
     */
    public void addPreservationLevelRationale(java.lang.String preservationLevelRationale)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PRESERVATIONLEVELRATIONALE$6);
            target.setStringValue(preservationLevelRationale);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "preservationLevelRationale" element
     */
    public org.apache.xmlbeans.XmlString insertNewPreservationLevelRationale(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(PRESERVATIONLEVELRATIONALE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "preservationLevelRationale" element
     */
    public org.apache.xmlbeans.XmlString addNewPreservationLevelRationale()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(PRESERVATIONLEVELRATIONALE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "preservationLevelRationale" element
     */
    public void removePreservationLevelRationale(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PRESERVATIONLEVELRATIONALE$6, i);
        }
    }
    
    /**
     * Gets the "preservationLevelDateAssigned" element
     */
    public java.lang.String getPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$8, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "preservationLevelDateAssigned" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$8, 0);
            return target;
        }
    }
    
    /**
     * True if has "preservationLevelDateAssigned" element
     */
    public boolean isSetPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PRESERVATIONLEVELDATEASSIGNED$8) != 0;
        }
    }
    
    /**
     * Sets the "preservationLevelDateAssigned" element
     */
    public void setPreservationLevelDateAssigned(java.lang.String preservationLevelDateAssigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$8, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(PRESERVATIONLEVELDATEASSIGNED$8);
            }
            target.setStringValue(preservationLevelDateAssigned);
        }
    }
    
    /**
     * Sets (as xml) the "preservationLevelDateAssigned" element
     */
    public void xsetPreservationLevelDateAssigned(gov.loc.premis.v3.EdtfSimpleType preservationLevelDateAssigned)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(PRESERVATIONLEVELDATEASSIGNED$8, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(PRESERVATIONLEVELDATEASSIGNED$8);
            }
            target.set(preservationLevelDateAssigned);
        }
    }
    
    /**
     * Unsets the "preservationLevelDateAssigned" element
     */
    public void unsetPreservationLevelDateAssigned()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PRESERVATIONLEVELDATEASSIGNED$8, 0);
        }
    }
}
