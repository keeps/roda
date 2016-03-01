/*
 * XML Type:  otherRightsInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML otherRightsInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class OtherRightsInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifier");
    private static final javax.xml.namespace.QName OTHERRIGHTSBASIS$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsBasis");
    private static final javax.xml.namespace.QName OTHERRIGHTSAPPLICABLEDATES$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsApplicableDates");
    private static final javax.xml.namespace.QName OTHERRIGHTSNOTE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsNote");
    
    
    /**
     * Gets array of all "otherRightsDocumentationIdentifier" elements
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[] getOtherRightsDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, targetList);
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[] result = new gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "otherRightsDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType getOtherRightsDocumentationIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "otherRightsDocumentationIdentifier" element
     */
    public int sizeOfOtherRightsDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0);
        }
    }
    
    /**
     * Sets array of all "otherRightsDocumentationIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setOtherRightsDocumentationIdentifierArray(gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType[] otherRightsDocumentationIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(otherRightsDocumentationIdentifierArray, OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0);
    }
    
    /**
     * Sets ith "otherRightsDocumentationIdentifier" element
     */
    public void setOtherRightsDocumentationIdentifierArray(int i, gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType otherRightsDocumentationIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(otherRightsDocumentationIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "otherRightsDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType insertNewOtherRightsDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().insert_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "otherRightsDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType addNewOtherRightsDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "otherRightsDocumentationIdentifier" element
     */
    public void removeOtherRightsDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, i);
        }
    }
    
    /**
     * Gets the "otherRightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSBASIS$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsBasis" element
     */
    public void setOtherRightsBasis(gov.loc.premis.v3.StringPlusAuthority otherRightsBasis)
    {
        generatedSetterHelperImpl(otherRightsBasis, OTHERRIGHTSBASIS$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSBASIS$2);
            return target;
        }
    }
    
    /**
     * Gets the "otherRightsApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(OTHERRIGHTSAPPLICABLEDATES$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "otherRightsApplicableDates" element
     */
    public boolean isSetOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OTHERRIGHTSAPPLICABLEDATES$4) != 0;
        }
    }
    
    /**
     * Sets the "otherRightsApplicableDates" element
     */
    public void setOtherRightsApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType otherRightsApplicableDates)
    {
        generatedSetterHelperImpl(otherRightsApplicableDates, OTHERRIGHTSAPPLICABLEDATES$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(OTHERRIGHTSAPPLICABLEDATES$4);
            return target;
        }
    }
    
    /**
     * Unsets the "otherRightsApplicableDates" element
     */
    public void unsetOtherRightsApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OTHERRIGHTSAPPLICABLEDATES$4, 0);
        }
    }
    
    /**
     * Gets array of all "otherRightsNote" elements
     */
    public java.lang.String[] getOtherRightsNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OTHERRIGHTSNOTE$6, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "otherRightsNote" element
     */
    public java.lang.String getOtherRightsNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "otherRightsNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetOtherRightsNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OTHERRIGHTSNOTE$6, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "otherRightsNote" element
     */
    public org.apache.xmlbeans.XmlString xgetOtherRightsNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "otherRightsNote" element
     */
    public int sizeOfOtherRightsNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OTHERRIGHTSNOTE$6);
        }
    }
    
    /**
     * Sets array of all "otherRightsNote" element
     */
    public void setOtherRightsNoteArray(java.lang.String[] otherRightsNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(otherRightsNoteArray, OTHERRIGHTSNOTE$6);
        }
    }
    
    /**
     * Sets ith "otherRightsNote" element
     */
    public void setOtherRightsNoteArray(int i, java.lang.String otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(otherRightsNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "otherRightsNote" element
     */
    public void xsetOtherRightsNoteArray(org.apache.xmlbeans.XmlString[]otherRightsNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(otherRightsNoteArray, OTHERRIGHTSNOTE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "otherRightsNote" element
     */
    public void xsetOtherRightsNoteArray(int i, org.apache.xmlbeans.XmlString otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(otherRightsNote);
        }
    }
    
    /**
     * Inserts the value as the ith "otherRightsNote" element
     */
    public void insertOtherRightsNote(int i, java.lang.String otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(OTHERRIGHTSNOTE$6, i);
            target.setStringValue(otherRightsNote);
        }
    }
    
    /**
     * Appends the value as the last "otherRightsNote" element
     */
    public void addOtherRightsNote(java.lang.String otherRightsNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OTHERRIGHTSNOTE$6);
            target.setStringValue(otherRightsNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "otherRightsNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewOtherRightsNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(OTHERRIGHTSNOTE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "otherRightsNote" element
     */
    public org.apache.xmlbeans.XmlString addNewOtherRightsNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OTHERRIGHTSNOTE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "otherRightsNote" element
     */
    public void removeOtherRightsNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OTHERRIGHTSNOTE$6, i);
        }
    }
}
