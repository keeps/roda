/*
 * XML Type:  signatureInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignatureInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML signatureInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class SignatureInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignatureInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public SignatureInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNATURE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signature");
    private static final javax.xml.namespace.QName SIGNATUREINFORMATIONEXTENSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "signatureInformationExtension");
    
    
    /**
     * Gets the "signature" element
     */
    public gov.loc.premis.v3.SignatureComplexType getSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureComplexType target = null;
            target = (gov.loc.premis.v3.SignatureComplexType)get_store().find_element_user(SIGNATURE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "signature" element
     */
    public boolean isSetSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATURE$0) != 0;
        }
    }
    
    /**
     * Sets the "signature" element
     */
    public void setSignature(gov.loc.premis.v3.SignatureComplexType signature)
    {
        generatedSetterHelperImpl(signature, SIGNATURE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "signature" element
     */
    public gov.loc.premis.v3.SignatureComplexType addNewSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.SignatureComplexType target = null;
            target = (gov.loc.premis.v3.SignatureComplexType)get_store().add_element_user(SIGNATURE$0);
            return target;
        }
    }
    
    /**
     * Unsets the "signature" element
     */
    public void unsetSignature()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATURE$0, 0);
        }
    }
    
    /**
     * Gets array of all "signatureInformationExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getSignatureInformationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(SIGNATUREINFORMATIONEXTENSION$2, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "signatureInformationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getSignatureInformationExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNATUREINFORMATIONEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "signatureInformationExtension" element
     */
    public int sizeOfSignatureInformationExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(SIGNATUREINFORMATIONEXTENSION$2);
        }
    }
    
    /**
     * Sets array of all "signatureInformationExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setSignatureInformationExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] signatureInformationExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(signatureInformationExtensionArray, SIGNATUREINFORMATIONEXTENSION$2);
    }
    
    /**
     * Sets ith "signatureInformationExtension" element
     */
    public void setSignatureInformationExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType signatureInformationExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNATUREINFORMATIONEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(signatureInformationExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "signatureInformationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewSignatureInformationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(SIGNATUREINFORMATIONEXTENSION$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "signatureInformationExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewSignatureInformationExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(SIGNATUREINFORMATIONEXTENSION$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "signatureInformationExtension" element
     */
    public void removeSignatureInformationExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(SIGNATUREINFORMATIONEXTENSION$2, i);
        }
    }
}
