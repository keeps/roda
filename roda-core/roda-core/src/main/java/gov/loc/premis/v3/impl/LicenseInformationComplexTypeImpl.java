/*
 * XML Type:  licenseInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML licenseInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LicenseInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LicenseInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifier");
    private static final javax.xml.namespace.QName LICENSETERMS$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseTerms");
    private static final javax.xml.namespace.QName LICENSENOTE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseNote");
    private static final javax.xml.namespace.QName LICENSEAPPLICABLEDATES$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseApplicableDates");
    
    
    /**
     * Gets array of all "licenseDocumentationIdentifier" elements
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[] getLicenseDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LICENSEDOCUMENTATIONIDENTIFIER$0, targetList);
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[] result = new gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "licenseDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType getLicenseDocumentationIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "licenseDocumentationIdentifier" element
     */
    public int sizeOfLicenseDocumentationIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSEDOCUMENTATIONIDENTIFIER$0);
        }
    }
    
    /**
     * Sets array of all "licenseDocumentationIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLicenseDocumentationIdentifierArray(gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType[] licenseDocumentationIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(licenseDocumentationIdentifierArray, LICENSEDOCUMENTATIONIDENTIFIER$0);
    }
    
    /**
     * Sets ith "licenseDocumentationIdentifier" element
     */
    public void setLicenseDocumentationIdentifierArray(int i, gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType licenseDocumentationIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(licenseDocumentationIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "licenseDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType insertNewLicenseDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().insert_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "licenseDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType addNewLicenseDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "licenseDocumentationIdentifier" element
     */
    public void removeLicenseDocumentationIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSEDOCUMENTATIONIDENTIFIER$0, i);
        }
    }
    
    /**
     * Gets the "licenseTerms" element
     */
    public java.lang.String getLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSETERMS$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseTerms" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSETERMS$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "licenseTerms" element
     */
    public boolean isSetLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSETERMS$2) != 0;
        }
    }
    
    /**
     * Sets the "licenseTerms" element
     */
    public void setLicenseTerms(java.lang.String licenseTerms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSETERMS$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSETERMS$2);
            }
            target.setStringValue(licenseTerms);
        }
    }
    
    /**
     * Sets (as xml) the "licenseTerms" element
     */
    public void xsetLicenseTerms(org.apache.xmlbeans.XmlString licenseTerms)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSETERMS$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSETERMS$2);
            }
            target.set(licenseTerms);
        }
    }
    
    /**
     * Unsets the "licenseTerms" element
     */
    public void unsetLicenseTerms()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSETERMS$2, 0);
        }
    }
    
    /**
     * Gets array of all "licenseNote" elements
     */
    public java.lang.String[] getLicenseNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LICENSENOTE$4, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "licenseNote" element
     */
    public java.lang.String getLicenseNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSENOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "licenseNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetLicenseNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LICENSENOTE$4, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "licenseNote" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSENOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "licenseNote" element
     */
    public int sizeOfLicenseNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSENOTE$4);
        }
    }
    
    /**
     * Sets array of all "licenseNote" element
     */
    public void setLicenseNoteArray(java.lang.String[] licenseNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(licenseNoteArray, LICENSENOTE$4);
        }
    }
    
    /**
     * Sets ith "licenseNote" element
     */
    public void setLicenseNoteArray(int i, java.lang.String licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSENOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(licenseNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "licenseNote" element
     */
    public void xsetLicenseNoteArray(org.apache.xmlbeans.XmlString[]licenseNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(licenseNoteArray, LICENSENOTE$4);
        }
    }
    
    /**
     * Sets (as xml) ith "licenseNote" element
     */
    public void xsetLicenseNoteArray(int i, org.apache.xmlbeans.XmlString licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSENOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(licenseNote);
        }
    }
    
    /**
     * Inserts the value as the ith "licenseNote" element
     */
    public void insertLicenseNote(int i, java.lang.String licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(LICENSENOTE$4, i);
            target.setStringValue(licenseNote);
        }
    }
    
    /**
     * Appends the value as the last "licenseNote" element
     */
    public void addLicenseNote(java.lang.String licenseNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSENOTE$4);
            target.setStringValue(licenseNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "licenseNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewLicenseNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(LICENSENOTE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "licenseNote" element
     */
    public org.apache.xmlbeans.XmlString addNewLicenseNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSENOTE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "licenseNote" element
     */
    public void removeLicenseNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSENOTE$4, i);
        }
    }
    
    /**
     * Gets the "licenseApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType getLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().find_element_user(LICENSEAPPLICABLEDATES$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "licenseApplicableDates" element
     */
    public boolean isSetLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSEAPPLICABLEDATES$6) != 0;
        }
    }
    
    /**
     * Sets the "licenseApplicableDates" element
     */
    public void setLicenseApplicableDates(gov.loc.premis.v3.StartAndEndDateComplexType licenseApplicableDates)
    {
        generatedSetterHelperImpl(licenseApplicableDates, LICENSEAPPLICABLEDATES$6, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseApplicableDates" element
     */
    public gov.loc.premis.v3.StartAndEndDateComplexType addNewLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StartAndEndDateComplexType target = null;
            target = (gov.loc.premis.v3.StartAndEndDateComplexType)get_store().add_element_user(LICENSEAPPLICABLEDATES$6);
            return target;
        }
    }
    
    /**
     * Unsets the "licenseApplicableDates" element
     */
    public void unsetLicenseApplicableDates()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSEAPPLICABLEDATES$6, 0);
        }
    }
}
