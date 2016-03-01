/*
 * XML Type:  statuteInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML statuteInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class StatuteInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public StatuteInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEJURISDICTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteJurisdiction");
    private static final javax.xml.namespace.QName STATUTECITATION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteCitation");
    private static final javax.xml.namespace.QName STATUTEINFORMATIONDETERMINATIONDATE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteInformationDeterminationDate");
    private static final javax.xml.namespace.QName STATUTENOTE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteNote");
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIER$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifier");
    private static final javax.xml.namespace.QName STATUTEAPPLICABLEDATES$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteApplicableDates");
    
    
    /**
     * Gets the "statuteJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode getStatuteJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().find_element_user(STATUTEJURISDICTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteJurisdiction" element
     */
    public void setStatuteJurisdiction(gov.loc.premis.v3.CountryCode statuteJurisdiction)
    {
        generatedSetterHelperImpl(statuteJurisdiction, STATUTEJURISDICTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode addNewStatuteJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().add_element_user(STATUTEJURISDICTION$0);
            return target;
        }
    }
    
    /**
     * Gets the "statuteCitation" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStatuteCitation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STATUTECITATION$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteCitation" element
     */
    public void setStatuteCitation(gov.loc.premis.v3.StringPlusAuthority statuteCitation)
    {
        generatedSetterHelperImpl(statuteCitation, STATUTECITATION$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteCitation" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStatuteCitation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STATUTECITATION$2);
            return target;
        }
    }
    
    /**
     * Gets the "statuteInformationDeterminationDate" element
     */
    public java.lang.String getStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "statuteInformationDeterminationDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "statuteInformationDeterminationDate" element
     */
    public boolean isSetStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTEINFORMATIONDETERMINATIONDATE$4) != 0;
        }
    }
    
    /**
     * Sets the "statuteInformationDeterminationDate" element
     */
    public void setStatuteInformationDeterminationDate(java.lang.String statuteInformationDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4);
            }
            target.setStringValue(statuteInformationDeterminationDate);
        }
    }
    
    /**
     * Sets (as xml) the "statuteInformationDeterminationDate" element
     */
    public void xsetStatuteInformationDeterminationDate(gov.loc.premis.v3.EdtfSimpleType statuteInformationDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(STATUTEINFORMATIONDETERMINATIONDATE$4);
            }
            target.set(statuteInformationDeterminationDate);
        }
    }
    
    /**
     * Unsets the "statuteInformationDeterminationDate" element
     */
    public void unsetStatuteInformationDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTEINFORMATIONDETERMINATIONDATE$4, 0);
        }
    }
    
    /**
     * Gets array of all "statuteNote" elements
     */
    public java.lang.String[] getStatuteNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STATUTENOTE$6, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "statuteNote" element
     */
    public java.lang.String getStatuteNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTENOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "statuteNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetStatuteNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STATUTENOTE$6, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "statuteNote" element
     */
    public org.apache.xmlbeans.XmlString xgetStatuteNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTENOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "statuteNote" element
     */
    public int sizeOfStatuteNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTENOTE$6);
        }
    }
    
    /**
     * Sets array of all "statuteNote" element
     */
    public void setStatuteNoteArray(java.lang.String[] statuteNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(statuteNoteArray, STATUTENOTE$6);
        }
    }
    
    /**
     * Sets ith "statuteNote" element
     */
    public void setStatuteNoteArray(int i, java.lang.String statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(STATUTENOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(statuteNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "statuteNote" element
     */
    public void xsetStatuteNoteArray(org.apache.xmlbeans.XmlString[]statuteNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(statuteNoteArray, STATUTENOTE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "statuteNote" element
     */
    public void xsetStatuteNoteArray(int i, org.apache.xmlbeans.XmlString statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(STATUTENOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(statuteNote);
        }
    }
    
    /**
     * Inserts the value as the ith "statuteNote" element
     */
    public void insertStatuteNote(int i, java.lang.String statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(STATUTENOTE$6, i);
            target.setStringValue(statuteNote);
        }
    }
    
    /**
     * Appends the value as the last "statuteNote" element
     */
    public void addStatuteNote(java.lang.String statuteNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(STATUTENOTE$6);
            target.setStringValue(statuteNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewStatuteNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(STATUTENOTE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteNote" element
     */
    public org.apache.xmlbeans.XmlString addNewStatuteNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(STATUTENOTE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "statuteNote" element
     */
    public void removeStatuteNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTENOTE$6, i);
        }
    }
    
    /**
     * Gets array of all "statuteDocumentationIdentifier" elements
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[] getStatuteDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STATUTEDOCUMENTATIONIDENTIFIER$8, targetList);
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[] result = new gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "statuteDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType getStatuteDocumentationIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "statuteDocumentationIdentifier" element
     */
    public int sizeOfStatuteDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTEDOCUMENTATIONIDENTIFIER$8);
        }
    }
    
    /**
     * Sets array of all "statuteDocumentationIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setStatuteDocumentationIdentifierArray(gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType[] statuteDocumentationIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(statuteDocumentationIdentifierArray, STATUTEDOCUMENTATIONIDENTIFIER$8);
    }
    
    /**
     * Sets ith "statuteDocumentationIdentifier" element
     */
    public void setStatuteDocumentationIdentifierArray(int i, gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType statuteDocumentationIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(statuteDocumentationIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType insertNewStatuteDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().insert_element_user(STATUTEDOCUMENTATIONIDENTIFIER$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType addNewStatuteDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIER$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "statuteDocumentationIdentifier" element
     */
    public void removeStatuteDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTEDOCUMENTATIONIDENTIFIER$8, i);
        }
    }
    
    /**
     * Gets the "statuteApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(STATUTEAPPLICABLEDATES$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "statuteApplicableDates" element
     */
    public boolean isSetStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTEAPPLICABLEDATES$10) != 0;
        }
    }
    
    /**
     * Sets the "statuteApplicableDates" element
     */
    public void setStatuteApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType statuteApplicableDates)
    {
        generatedSetterHelperImpl(statuteApplicableDates, STATUTEAPPLICABLEDATES$10, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(STATUTEAPPLICABLEDATES$10);
            return target;
        }
    }
    
    /**
     * Unsets the "statuteApplicableDates" element
     */
    public void unsetStatuteApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTEAPPLICABLEDATES$10, 0);
        }
    }
}
