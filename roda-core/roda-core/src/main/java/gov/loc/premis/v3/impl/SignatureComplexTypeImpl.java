/*
 * XML Type:  signatureComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML signatureComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class SignatureComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureComplexType
{
    private static final long serialVersionUID = 1L;
    
    public SignatureComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATUREENCODING$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureEncoding");
    private static final javax.xml.namespace.QName SIGNER$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signer");
    private static final javax.xml.namespace.QName SIGNATUREMETHOD$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureMethod");
    private static final javax.xml.namespace.QName SIGNATUREVALUE$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureValue");
    private static final javax.xml.namespace.QName SIGNATUREVALIDATIONRULES$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureValidationRules");
    private static final javax.xml.namespace.QName SIGNATUREPROPERTIES$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureProperties");
    private static final javax.xml.namespace.QName KEYINFORMATION$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "keyInformation");
    
    
    /**
     * Gets the "signatureEncoding" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREENCODING$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureEncoding" element
     */
    public void setSignatureEncoding(gov.loc.premis.v3.StringPlusAuthority signatureEncoding)
    {
        generatedSetterHelperImpl(signatureEncoding, SIGNATUREENCODING$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureEncoding" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureEncoding()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREENCODING$0);
            return target;
        }
    }
    
    /**
     * Gets the "signer" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNER$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "signer" element
     */
    public boolean isSetSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNER$2) != 0;
        }
    }
    
    /**
     * Sets the "signer" element
     */
    public void setSigner(gov.loc.premis.v3.StringPlusAuthority signer)
    {
        generatedSetterHelperImpl(signer, SIGNER$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signer" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNER$2);
            return target;
        }
    }
    
    /**
     * Unsets the "signer" element
     */
    public void unsetSigner()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNER$2, 0);
        }
    }
    
    /**
     * Gets the "signatureMethod" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREMETHOD$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureMethod" element
     */
    public void setSignatureMethod(gov.loc.premis.v3.StringPlusAuthority signatureMethod)
    {
        generatedSetterHelperImpl(signatureMethod, SIGNATUREMETHOD$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureMethod" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureMethod()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREMETHOD$4);
            return target;
        }
    }
    
    /**
     * Gets the "signatureValue" element
     */
    public java.lang.String getSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "signatureValue" element
     */
    public org.apache.xmlbeans.XmlString xgetSignatureValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREVALUE$6, 0);
            return target;
        }
    }
    
    /**
     * Sets the "signatureValue" element
     */
    public void setSignatureValue(java.lang.String signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREVALUE$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREVALUE$6);
            }
            target.setStringValue(signatureValue);
        }
    }
    
    /**
     * Sets (as xml) the "signatureValue" element
     */
    public void xsetSignatureValue(org.apache.xmlbeans.XmlString signatureValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREVALUE$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNATUREVALUE$6);
            }
            target.set(signatureValue);
        }
    }
    
    /**
     * Gets the "signatureValidationRules" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSignatureValidationRules()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SIGNATUREVALIDATIONRULES$8, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "signatureValidationRules" element
     */
    public void setSignatureValidationRules(gov.loc.premis.v3.StringPlusAuthority signatureValidationRules)
    {
        generatedSetterHelperImpl(signatureValidationRules, SIGNATUREVALIDATIONRULES$8, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signatureValidationRules" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSignatureValidationRules()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SIGNATUREVALIDATIONRULES$8);
            return target;
        }
    }
    
    /**
     * Gets array of all "signatureProperties" elements
     */
    public java.lang.String[] getSignaturePropertiesArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNATUREPROPERTIES$10, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "signatureProperties" element
     */
    public java.lang.String getSignaturePropertiesArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREPROPERTIES$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "signatureProperties" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetSignaturePropertiesArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNATUREPROPERTIES$10, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "signatureProperties" element
     */
    public org.apache.xmlbeans.XmlString xgetSignaturePropertiesArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREPROPERTIES$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "signatureProperties" element
     */
    public int sizeOfSignaturePropertiesArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATUREPROPERTIES$10);
        }
    }
    
    /**
     * Sets array of all "signatureProperties" element
     */
    public void setSignaturePropertiesArray(java.lang.String[] signaturePropertiesArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(signaturePropertiesArray, SIGNATUREPROPERTIES$10);
        }
    }
    
    /**
     * Sets ith "signatureProperties" element
     */
    public void setSignaturePropertiesArray(int i, java.lang.String signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(SIGNATUREPROPERTIES$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(signatureProperties);
        }
    }
    
    /**
     * Sets (as xml) array of all "signatureProperties" element
     */
    public void xsetSignaturePropertiesArray(org.apache.xmlbeans.XmlString[]signaturePropertiesArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(signaturePropertiesArray, SIGNATUREPROPERTIES$10);
        }
    }
    
    /**
     * Sets (as xml) ith "signatureProperties" element
     */
    public void xsetSignaturePropertiesArray(int i, org.apache.xmlbeans.XmlString signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(SIGNATUREPROPERTIES$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(signatureProperties);
        }
    }
    
    /**
     * Inserts the value as the ith "signatureProperties" element
     */
    public void insertSignatureProperties(int i, java.lang.String signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(SIGNATUREPROPERTIES$10, i);
            target.setStringValue(signatureProperties);
        }
    }
    
    /**
     * Appends the value as the last "signatureProperties" element
     */
    public void addSignatureProperties(java.lang.String signatureProperties)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(SIGNATUREPROPERTIES$10);
            target.setStringValue(signatureProperties);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "signatureProperties" element
     */
    public org.apache.xmlbeans.XmlString insertNewSignatureProperties(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(SIGNATUREPROPERTIES$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "signatureProperties" element
     */
    public org.apache.xmlbeans.XmlString addNewSignatureProperties()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(SIGNATUREPROPERTIES$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "signatureProperties" element
     */
    public void removeSignatureProperties(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATUREPROPERTIES$10, i);
        }
    }
    
    /**
     * Gets array of all "keyInformation" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getKeyInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(KEYINFORMATION$12, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "keyInformation" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getKeyInformationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(KEYINFORMATION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "keyInformation" element
     */
    public int sizeOfKeyInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(KEYINFORMATION$12);
        }
    }
    
    /**
     * Sets array of all "keyInformation" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setKeyInformationArray(gov.loc.premis.v3.ExtensionComplexType[] keyInformationArray)
    {
        check_orphaned();
        arraySetterHelper(keyInformationArray, KEYINFORMATION$12);
    }
    
    /**
     * Sets ith "keyInformation" element
     */
    public void setKeyInformationArray(int i, gov.loc.premis.v3.ExtensionComplexType keyInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(KEYINFORMATION$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(keyInformation);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "keyInformation" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewKeyInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(KEYINFORMATION$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "keyInformation" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewKeyInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(KEYINFORMATION$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "keyInformation" element
     */
    public void removeKeyInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(KEYINFORMATION$12, i);
        }
    }
}
