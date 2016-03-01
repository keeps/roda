/*
 * XML Type:  storageComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StorageComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML storageComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class StorageComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StorageComplexType
{
    private static final long serialVersionUID = 1L;
    
    public StorageComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName CONTENTLOCATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "contentLocation");
    private static final javax.xml.namespace.QName STORAGEMEDIUM$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "storageMedium");
    
    
    /**
     * Gets the "contentLocation" element
     */
    public gov.loc.premis.v3.ContentLocationComplexType getContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ContentLocationComplexType target = null;
            target = (gov.loc.premis.v3.ContentLocationComplexType)get_store().find_element_user(CONTENTLOCATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "contentLocation" element
     */
    public boolean isSetContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(CONTENTLOCATION$0) != 0;
        }
    }
    
    /**
     * Sets the "contentLocation" element
     */
    public void setContentLocation(gov.loc.premis.v3.ContentLocationComplexType contentLocation)
    {
        generatedSetterHelperImpl(contentLocation, CONTENTLOCATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "contentLocation" element
     */
    public gov.loc.premis.v3.ContentLocationComplexType addNewContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ContentLocationComplexType target = null;
            target = (gov.loc.premis.v3.ContentLocationComplexType)get_store().add_element_user(CONTENTLOCATION$0);
            return target;
        }
    }
    
    /**
     * Unsets the "contentLocation" element
     */
    public void unsetContentLocation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(CONTENTLOCATION$0, 0);
        }
    }
    
    /**
     * Gets the "storageMedium" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStorageMedium()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STORAGEMEDIUM$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "storageMedium" element
     */
    public boolean isSetStorageMedium()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STORAGEMEDIUM$2) != 0;
        }
    }
    
    /**
     * Sets the "storageMedium" element
     */
    public void setStorageMedium(gov.loc.premis.v3.StringPlusAuthority storageMedium)
    {
        generatedSetterHelperImpl(storageMedium, STORAGEMEDIUM$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "storageMedium" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewStorageMedium()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STORAGEMEDIUM$2);
            return target;
        }
    }
    
    /**
     * Unsets the "storageMedium" element
     */
    public void unsetStorageMedium()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STORAGEMEDIUM$2, 0);
        }
    }
}
