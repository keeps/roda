/*
 * XML Type:  formatComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FormatComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML formatComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class FormatComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FormatComplexType
{
    private static final long serialVersionUID = 1L;
    
    public FormatComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName FORMATDESIGNATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatDesignation");
    private static final javax.xml.namespace.QName FORMATREGISTRY$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatRegistry");
    private static final javax.xml.namespace.QName FORMATNOTE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "formatNote");
    
    
    /**
     * Gets the "formatDesignation" element
     */
    public gov.loc.premis.v3.FormatDesignationComplexType getFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatDesignationComplexType target = null;
            target = (gov.loc.premis.v3.FormatDesignationComplexType)get_store().find_element_user(FORMATDESIGNATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "formatDesignation" element
     */
    public boolean isSetFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMATDESIGNATION$0) != 0;
        }
    }
    
    /**
     * Sets the "formatDesignation" element
     */
    public void setFormatDesignation(gov.loc.premis.v3.FormatDesignationComplexType formatDesignation)
    {
        generatedSetterHelperImpl(formatDesignation, FORMATDESIGNATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatDesignation" element
     */
    public gov.loc.premis.v3.FormatDesignationComplexType addNewFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatDesignationComplexType target = null;
            target = (gov.loc.premis.v3.FormatDesignationComplexType)get_store().add_element_user(FORMATDESIGNATION$0);
            return target;
        }
    }
    
    /**
     * Unsets the "formatDesignation" element
     */
    public void unsetFormatDesignation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMATDESIGNATION$0, 0);
        }
    }
    
    /**
     * Gets the "formatRegistry" element
     */
    public gov.loc.premis.v3.FormatRegistryComplexType getFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatRegistryComplexType target = null;
            target = (gov.loc.premis.v3.FormatRegistryComplexType)get_store().find_element_user(FORMATREGISTRY$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "formatRegistry" element
     */
    public boolean isSetFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMATREGISTRY$2) != 0;
        }
    }
    
    /**
     * Sets the "formatRegistry" element
     */
    public void setFormatRegistry(gov.loc.premis.v3.FormatRegistryComplexType formatRegistry)
    {
        generatedSetterHelperImpl(formatRegistry, FORMATREGISTRY$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "formatRegistry" element
     */
    public gov.loc.premis.v3.FormatRegistryComplexType addNewFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.FormatRegistryComplexType target = null;
            target = (gov.loc.premis.v3.FormatRegistryComplexType)get_store().add_element_user(FORMATREGISTRY$2);
            return target;
        }
    }
    
    /**
     * Unsets the "formatRegistry" element
     */
    public void unsetFormatRegistry()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMATREGISTRY$2, 0);
        }
    }
    
    /**
     * Gets array of all "formatNote" elements
     */
    public java.lang.String[] getFormatNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(FORMATNOTE$4, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "formatNote" element
     */
    public java.lang.String getFormatNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATNOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "formatNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetFormatNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(FORMATNOTE$4, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "formatNote" element
     */
    public org.apache.xmlbeans.XmlString xgetFormatNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATNOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "formatNote" element
     */
    public int sizeOfFormatNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(FORMATNOTE$4);
        }
    }
    
    /**
     * Sets array of all "formatNote" element
     */
    public void setFormatNoteArray(java.lang.String[] formatNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(formatNoteArray, FORMATNOTE$4);
        }
    }
    
    /**
     * Sets ith "formatNote" element
     */
    public void setFormatNoteArray(int i, java.lang.String formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(FORMATNOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(formatNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "formatNote" element
     */
    public void xsetFormatNoteArray(org.apache.xmlbeans.XmlString[]formatNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(formatNoteArray, FORMATNOTE$4);
        }
    }
    
    /**
     * Sets (as xml) ith "formatNote" element
     */
    public void xsetFormatNoteArray(int i, org.apache.xmlbeans.XmlString formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(FORMATNOTE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(formatNote);
        }
    }
    
    /**
     * Inserts the value as the ith "formatNote" element
     */
    public void insertFormatNote(int i, java.lang.String formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(FORMATNOTE$4, i);
            target.setStringValue(formatNote);
        }
    }
    
    /**
     * Appends the value as the last "formatNote" element
     */
    public void addFormatNote(java.lang.String formatNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(FORMATNOTE$4);
            target.setStringValue(formatNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "formatNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewFormatNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(FORMATNOTE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "formatNote" element
     */
    public org.apache.xmlbeans.XmlString addNewFormatNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(FORMATNOTE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "formatNote" element
     */
    public void removeFormatNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(FORMATNOTE$4, i);
        }
    }
}
