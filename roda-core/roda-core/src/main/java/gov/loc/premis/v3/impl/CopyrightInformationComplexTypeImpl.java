/*
 * XML Type:  copyrightInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML copyrightInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class CopyrightInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTSTATUS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightStatus");
    private static final javax.xml.namespace.QName COPYRIGHTJURISDICTION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightJurisdiction");
    private static final javax.xml.namespace.QName COPYRIGHTSTATUSDETERMINATIONDATE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightStatusDeterminationDate");
    private static final javax.xml.namespace.QName COPYRIGHTNOTE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightNote");
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIER$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifier");
    private static final javax.xml.namespace.QName COPYRIGHTAPPLICABLEDATES$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightApplicableDates");
    
    
    /**
     * Gets the "copyrightStatus" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTSTATUS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightStatus" element
     */
    public void setCopyrightStatus(gov.loc.premis.v3.StringPlusAuthority copyrightStatus)
    {
        generatedSetterHelperImpl(copyrightStatus, COPYRIGHTSTATUS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightStatus" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTSTATUS$0);
            return target;
        }
    }
    
    /**
     * Gets the "copyrightJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode getCopyrightJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().find_element_user(COPYRIGHTJURISDICTION$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightJurisdiction" element
     */
    public void setCopyrightJurisdiction(gov.loc.premis.v3.CountryCode copyrightJurisdiction)
    {
        generatedSetterHelperImpl(copyrightJurisdiction, COPYRIGHTJURISDICTION$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode addNewCopyrightJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().add_element_user(COPYRIGHTJURISDICTION$2);
            return target;
        }
    }
    
    /**
     * Gets the "copyrightStatusDeterminationDate" element
     */
    public java.lang.String getCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "copyrightStatusDeterminationDate" element
     */
    public gov.loc.premis.v3.EdtfSimpleType xgetCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "copyrightStatusDeterminationDate" element
     */
    public boolean isSetCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTSTATUSDETERMINATIONDATE$4) != 0;
        }
    }
    
    /**
     * Sets the "copyrightStatusDeterminationDate" element
     */
    public void setCopyrightStatusDeterminationDate(java.lang.String copyrightStatusDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4);
            }
            target.setStringValue(copyrightStatusDeterminationDate);
        }
    }
    
    /**
     * Sets (as xml) the "copyrightStatusDeterminationDate" element
     */
    public void xsetCopyrightStatusDeterminationDate(gov.loc.premis.v3.EdtfSimpleType copyrightStatusDeterminationDate)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EdtfSimpleType target = null;
            target = (gov.loc.premis.v3.EdtfSimpleType)get_store().find_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4, 0);
            if (target == null)
            {
                target = (gov.loc.premis.v3.EdtfSimpleType)get_store().add_element_user(COPYRIGHTSTATUSDETERMINATIONDATE$4);
            }
            target.set(copyrightStatusDeterminationDate);
        }
    }
    
    /**
     * Unsets the "copyrightStatusDeterminationDate" element
     */
    public void unsetCopyrightStatusDeterminationDate()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTSTATUSDETERMINATIONDATE$4, 0);
        }
    }
    
    /**
     * Gets array of all "copyrightNote" elements
     */
    public java.lang.String[] getCopyrightNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COPYRIGHTNOTE$6, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "copyrightNote" element
     */
    public java.lang.String getCopyrightNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "copyrightNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetCopyrightNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COPYRIGHTNOTE$6, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "copyrightNote" element
     */
    public org.apache.xmlbeans.XmlString xgetCopyrightNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "copyrightNote" element
     */
    public int sizeOfCopyrightNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTNOTE$6);
        }
    }
    
    /**
     * Sets array of all "copyrightNote" element
     */
    public void setCopyrightNoteArray(java.lang.String[] copyrightNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(copyrightNoteArray, COPYRIGHTNOTE$6);
        }
    }
    
    /**
     * Sets ith "copyrightNote" element
     */
    public void setCopyrightNoteArray(int i, java.lang.String copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(copyrightNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "copyrightNote" element
     */
    public void xsetCopyrightNoteArray(org.apache.xmlbeans.XmlString[]copyrightNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(copyrightNoteArray, COPYRIGHTNOTE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "copyrightNote" element
     */
    public void xsetCopyrightNoteArray(int i, org.apache.xmlbeans.XmlString copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(copyrightNote);
        }
    }
    
    /**
     * Inserts the value as the ith "copyrightNote" element
     */
    public void insertCopyrightNote(int i, java.lang.String copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(COPYRIGHTNOTE$6, i);
            target.setStringValue(copyrightNote);
        }
    }
    
    /**
     * Appends the value as the last "copyrightNote" element
     */
    public void addCopyrightNote(java.lang.String copyrightNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTNOTE$6);
            target.setStringValue(copyrightNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "copyrightNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewCopyrightNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(COPYRIGHTNOTE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "copyrightNote" element
     */
    public org.apache.xmlbeans.XmlString addNewCopyrightNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COPYRIGHTNOTE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "copyrightNote" element
     */
    public void removeCopyrightNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTNOTE$6, i);
        }
    }
    
    /**
     * Gets array of all "copyrightDocumentationIdentifier" elements
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[] getCopyrightDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(COPYRIGHTDOCUMENTATIONIDENTIFIER$8, targetList);
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[] result = new gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "copyrightDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType getCopyrightDocumentationIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "copyrightDocumentationIdentifier" element
     */
    public int sizeOfCopyrightDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTDOCUMENTATIONIDENTIFIER$8);
        }
    }
    
    /**
     * Sets array of all "copyrightDocumentationIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setCopyrightDocumentationIdentifierArray(gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType[] copyrightDocumentationIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(copyrightDocumentationIdentifierArray, COPYRIGHTDOCUMENTATIONIDENTIFIER$8);
    }
    
    /**
     * Sets ith "copyrightDocumentationIdentifier" element
     */
    public void setCopyrightDocumentationIdentifierArray(int i, gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType copyrightDocumentationIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(copyrightDocumentationIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "copyrightDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType insertNewCopyrightDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().insert_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "copyrightDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType addNewCopyrightDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "copyrightDocumentationIdentifier" element
     */
    public void removeCopyrightDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTDOCUMENTATIONIDENTIFIER$8, i);
        }
    }
    
    /**
     * Gets the "copyrightApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(COPYRIGHTAPPLICABLEDATES$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "copyrightApplicableDates" element
     */
    public boolean isSetCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTAPPLICABLEDATES$10) != 0;
        }
    }
    
    /**
     * Sets the "copyrightApplicableDates" element
     */
    public void setCopyrightApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType copyrightApplicableDates)
    {
        generatedSetterHelperImpl(copyrightApplicableDates, COPYRIGHTAPPLICABLEDATES$10, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(COPYRIGHTAPPLICABLEDATES$10);
            return target;
        }
    }
    
    /**
     * Unsets the "copyrightApplicableDates" element
     */
    public void unsetCopyrightApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTAPPLICABLEDATES$10, 0);
        }
    }
}
