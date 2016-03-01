/*
 * XML Type:  environmentDesignationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentDesignationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML environmentDesignationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EnvironmentDesignationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentDesignationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentDesignationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentName");
    private static final javax.xml.namespace.QName ENVIRONMENTVERSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentVersion");
    private static final javax.xml.namespace.QName ENVIRONMENTORIGIN$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentOrigin");
    private static final javax.xml.namespace.QName ENVIRONMENTDESIGNATIONNOTE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentDesignationNote");
    private static final javax.xml.namespace.QName ENVIRONMENTDESIGNATIONEXTENSION$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentDesignationExtension");
    
    
    /**
     * Gets the "environmentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentName" element
     */
    public void setEnvironmentName(gov.loc.premis.v3.StringPlusAuthority environmentName)
    {
        generatedSetterHelperImpl(environmentName, ENVIRONMENTNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTNAME$0);
            return target;
        }
    }
    
    /**
     * Gets the "environmentVersion" element
     */
    public java.lang.String getEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTVERSION$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTVERSION$2, 0);
            return target;
        }
    }
    
    /**
     * True if has "environmentVersion" element
     */
    public boolean isSetEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ENVIRONMENTVERSION$2) != 0;
        }
    }
    
    /**
     * Sets the "environmentVersion" element
     */
    public void setEnvironmentVersion(java.lang.String environmentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTVERSION$2);
            }
            target.setStringValue(environmentVersion);
        }
    }
    
    /**
     * Sets (as xml) the "environmentVersion" element
     */
    public void xsetEnvironmentVersion(org.apache.xmlbeans.XmlString environmentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTVERSION$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTVERSION$2);
            }
            target.set(environmentVersion);
        }
    }
    
    /**
     * Unsets the "environmentVersion" element
     */
    public void unsetEnvironmentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ENVIRONMENTVERSION$2, 0);
        }
    }
    
    /**
     * Gets the "environmentOrigin" element
     */
    public java.lang.String getEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTORIGIN$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "environmentOrigin" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTORIGIN$4, 0);
            return target;
        }
    }
    
    /**
     * True if has "environmentOrigin" element
     */
    public boolean isSetEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ENVIRONMENTORIGIN$4) != 0;
        }
    }
    
    /**
     * Sets the "environmentOrigin" element
     */
    public void setEnvironmentOrigin(java.lang.String environmentOrigin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTORIGIN$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTORIGIN$4);
            }
            target.setStringValue(environmentOrigin);
        }
    }
    
    /**
     * Sets (as xml) the "environmentOrigin" element
     */
    public void xsetEnvironmentOrigin(org.apache.xmlbeans.XmlString environmentOrigin)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTORIGIN$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTORIGIN$4);
            }
            target.set(environmentOrigin);
        }
    }
    
    /**
     * Unsets the "environmentOrigin" element
     */
    public void unsetEnvironmentOrigin()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ENVIRONMENTORIGIN$4, 0);
        }
    }
    
    /**
     * Gets array of all "environmentDesignationNote" elements
     */
    public java.lang.String[] getEnvironmentDesignationNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ENVIRONMENTDESIGNATIONNOTE$6, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "environmentDesignationNote" element
     */
    public java.lang.String getEnvironmentDesignationNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "environmentDesignationNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetEnvironmentDesignationNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ENVIRONMENTDESIGNATIONNOTE$6, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "environmentDesignationNote" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentDesignationNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "environmentDesignationNote" element
     */
    public int sizeOfEnvironmentDesignationNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ENVIRONMENTDESIGNATIONNOTE$6);
        }
    }
    
    /**
     * Sets array of all "environmentDesignationNote" element
     */
    public void setEnvironmentDesignationNoteArray(java.lang.String[] environmentDesignationNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(environmentDesignationNoteArray, ENVIRONMENTDESIGNATIONNOTE$6);
        }
    }
    
    /**
     * Sets ith "environmentDesignationNote" element
     */
    public void setEnvironmentDesignationNoteArray(int i, java.lang.String environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(environmentDesignationNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "environmentDesignationNote" element
     */
    public void xsetEnvironmentDesignationNoteArray(org.apache.xmlbeans.XmlString[]environmentDesignationNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(environmentDesignationNoteArray, ENVIRONMENTDESIGNATIONNOTE$6);
        }
    }
    
    /**
     * Sets (as xml) ith "environmentDesignationNote" element
     */
    public void xsetEnvironmentDesignationNoteArray(int i, org.apache.xmlbeans.XmlString environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(environmentDesignationNote);
        }
    }
    
    /**
     * Inserts the value as the ith "environmentDesignationNote" element
     */
    public void insertEnvironmentDesignationNote(int i, java.lang.String environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            target.setStringValue(environmentDesignationNote);
        }
    }
    
    /**
     * Appends the value as the last "environmentDesignationNote" element
     */
    public void addEnvironmentDesignationNote(java.lang.String environmentDesignationNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTDESIGNATIONNOTE$6);
            target.setStringValue(environmentDesignationNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "environmentDesignationNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewEnvironmentDesignationNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(ENVIRONMENTDESIGNATIONNOTE$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "environmentDesignationNote" element
     */
    public org.apache.xmlbeans.XmlString addNewEnvironmentDesignationNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTDESIGNATIONNOTE$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "environmentDesignationNote" element
     */
    public void removeEnvironmentDesignationNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ENVIRONMENTDESIGNATIONNOTE$6, i);
        }
    }
    
    /**
     * Gets array of all "environmentDesignationExtension" elements
     */
    public java.lang.String[] getEnvironmentDesignationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ENVIRONMENTDESIGNATIONEXTENSION$8, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "environmentDesignationExtension" element
     */
    public java.lang.String getEnvironmentDesignationExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "environmentDesignationExtension" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetEnvironmentDesignationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(ENVIRONMENTDESIGNATIONEXTENSION$8, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "environmentDesignationExtension" element
     */
    public org.apache.xmlbeans.XmlString xgetEnvironmentDesignationExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "environmentDesignationExtension" element
     */
    public int sizeOfEnvironmentDesignationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(ENVIRONMENTDESIGNATIONEXTENSION$8);
        }
    }
    
    /**
     * Sets array of all "environmentDesignationExtension" element
     */
    public void setEnvironmentDesignationExtensionArray(java.lang.String[] environmentDesignationExtensionArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(environmentDesignationExtensionArray, ENVIRONMENTDESIGNATIONEXTENSION$8);
        }
    }
    
    /**
     * Sets ith "environmentDesignationExtension" element
     */
    public void setEnvironmentDesignationExtensionArray(int i, java.lang.String environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(environmentDesignationExtension);
        }
    }
    
    /**
     * Sets (as xml) array of all "environmentDesignationExtension" element
     */
    public void xsetEnvironmentDesignationExtensionArray(org.apache.xmlbeans.XmlString[]environmentDesignationExtensionArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(environmentDesignationExtensionArray, ENVIRONMENTDESIGNATIONEXTENSION$8);
        }
    }
    
    /**
     * Sets (as xml) ith "environmentDesignationExtension" element
     */
    public void xsetEnvironmentDesignationExtensionArray(int i, org.apache.xmlbeans.XmlString environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(environmentDesignationExtension);
        }
    }
    
    /**
     * Inserts the value as the ith "environmentDesignationExtension" element
     */
    public void insertEnvironmentDesignationExtension(int i, java.lang.String environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            target.setStringValue(environmentDesignationExtension);
        }
    }
    
    /**
     * Appends the value as the last "environmentDesignationExtension" element
     */
    public void addEnvironmentDesignationExtension(java.lang.String environmentDesignationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8);
            target.setStringValue(environmentDesignationExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "environmentDesignationExtension" element
     */
    public org.apache.xmlbeans.XmlString insertNewEnvironmentDesignationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "environmentDesignationExtension" element
     */
    public org.apache.xmlbeans.XmlString addNewEnvironmentDesignationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(ENVIRONMENTDESIGNATIONEXTENSION$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "environmentDesignationExtension" element
     */
    public void removeEnvironmentDesignationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(ENVIRONMENTDESIGNATIONEXTENSION$8, i);
        }
    }
}
