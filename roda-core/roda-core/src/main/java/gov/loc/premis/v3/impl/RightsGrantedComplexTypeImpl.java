/*
 * XML Type:  rightsGrantedComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsGrantedComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML rightsGrantedComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RightsGrantedComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsGrantedComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RightsGrantedComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ACT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "act");
    private static final javax.xml.namespace.QName RESTRICTION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "restriction");
    private static final javax.xml.namespace.QName TERMOFGRANT$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "termOfGrant");
    private static final javax.xml.namespace.QName TERMOFRESTRICTION$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "termOfRestriction");
    private static final javax.xml.namespace.QName RIGHTSGRANTEDNOTE$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsGrantedNote");
    
    
    /**
     * Gets the "act" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAct()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ACT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "act" element
     */
    public void setAct(gov.loc.premis.v3.StringPlusAuthority act)
    {
        generatedSetterHelperImpl(act, ACT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "act" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAct()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ACT$0);
            return target;
        }
    }
    
    /**
     * Gets array of all "restriction" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getRestrictionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RESTRICTION$2, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "restriction" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRestrictionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RESTRICTION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "restriction" element
     */
    public int sizeOfRestrictionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RESTRICTION$2);
        }
    }
    
    /**
     * Sets array of all "restriction" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRestrictionArray(gov.loc.premis.v3.StringPlusAuthority[] restrictionArray)
    {
        check_orphaned();
        arraySetterHelper(restrictionArray, RESTRICTION$2);
    }
    
    /**
     * Sets ith "restriction" element
     */
    public void setRestrictionArray(int i, gov.loc.premis.v3.StringPlusAuthority restriction)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RESTRICTION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(restriction);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "restriction" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewRestriction(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(RESTRICTION$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "restriction" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RESTRICTION$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "restriction" element
     */
    public void removeRestriction(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RESTRICTION$2, i);
        }
    }
    
    /**
     * Gets the "termOfGrant" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(TERMOFGRANT$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "termOfGrant" element
     */
    public boolean isSetTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TERMOFGRANT$4) != 0;
        }
    }
    
    /**
     * Sets the "termOfGrant" element
     */
    public void setTermOfGrant(gov.loc.premis.v3.StartAndEndDateComplexType termOfGrant)
    {
        generatedSetterHelperImpl(termOfGrant, TERMOFGRANT$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "termOfGrant" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(TERMOFGRANT$4);
            return target;
        }
    }
    
    /**
     * Unsets the "termOfGrant" element
     */
    public void unsetTermOfGrant()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TERMOFGRANT$4, 0);
        }
    }
    
    /**
     * Gets the "termOfRestriction" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(TERMOFRESTRICTION$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "termOfRestriction" element
     */
    public boolean isSetTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(TERMOFRESTRICTION$6) != 0;
        }
    }
    
    /**
     * Sets the "termOfRestriction" element
     */
    public void setTermOfRestriction(gov.loc.premis.v3.StartAndEndDateComplexType termOfRestriction)
    {
        generatedSetterHelperImpl(termOfRestriction, TERMOFRESTRICTION$6, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "termOfRestriction" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(TERMOFRESTRICTION$6);
            return target;
        }
    }
    
    /**
     * Unsets the "termOfRestriction" element
     */
    public void unsetTermOfRestriction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(TERMOFRESTRICTION$6, 0);
        }
    }
    
    /**
     * Gets array of all "rightsGrantedNote" elements
     */
    public java.lang.String[] getRightsGrantedNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTSGRANTEDNOTE$8, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "rightsGrantedNote" element
     */
    public java.lang.String getRightsGrantedNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSGRANTEDNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "rightsGrantedNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetRightsGrantedNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTSGRANTEDNOTE$8, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "rightsGrantedNote" element
     */
    public org.apache.xmlbeans.XmlString xgetRightsGrantedNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSGRANTEDNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "rightsGrantedNote" element
     */
    public int sizeOfRightsGrantedNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RIGHTSGRANTEDNOTE$8);
        }
    }
    
    /**
     * Sets array of all "rightsGrantedNote" element
     */
    public void setRightsGrantedNoteArray(java.lang.String[] rightsGrantedNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(rightsGrantedNoteArray, RIGHTSGRANTEDNOTE$8);
        }
    }
    
    /**
     * Sets ith "rightsGrantedNote" element
     */
    public void setRightsGrantedNoteArray(int i, java.lang.String rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(RIGHTSGRANTEDNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(rightsGrantedNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "rightsGrantedNote" element
     */
    public void xsetRightsGrantedNoteArray(org.apache.xmlbeans.XmlString[]rightsGrantedNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(rightsGrantedNoteArray, RIGHTSGRANTEDNOTE$8);
        }
    }
    
    /**
     * Sets (as xml) ith "rightsGrantedNote" element
     */
    public void xsetRightsGrantedNoteArray(int i, org.apache.xmlbeans.XmlString rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(RIGHTSGRANTEDNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(rightsGrantedNote);
        }
    }
    
    /**
     * Inserts the value as the ith "rightsGrantedNote" element
     */
    public void insertRightsGrantedNote(int i, java.lang.String rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(RIGHTSGRANTEDNOTE$8, i);
            target.setStringValue(rightsGrantedNote);
        }
    }
    
    /**
     * Appends the value as the last "rightsGrantedNote" element
     */
    public void addRightsGrantedNote(java.lang.String rightsGrantedNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(RIGHTSGRANTEDNOTE$8);
            target.setStringValue(rightsGrantedNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsGrantedNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewRightsGrantedNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(RIGHTSGRANTEDNOTE$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsGrantedNote" element
     */
    public org.apache.xmlbeans.XmlString addNewRightsGrantedNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(RIGHTSGRANTEDNOTE$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "rightsGrantedNote" element
     */
    public void removeRightsGrantedNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RIGHTSGRANTEDNOTE$8, i);
        }
    }
}
