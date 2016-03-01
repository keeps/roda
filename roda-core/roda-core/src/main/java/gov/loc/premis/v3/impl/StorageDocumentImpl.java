/*
 * An XML document type.
 * Localname: storage
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StorageDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one storage(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StorageDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StorageDocument
{
    private static final long serialVersionUID = 1L;
    
    public StorageDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STORAGE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "storage");
    
    
    /**
     * Gets the "storage" element
     */
    public gov.loc.premis.v3.StorageComplexType getStorage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().find_element_user(STORAGE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "storage" element
     */
    public void setStorage(gov.loc.premis.v3.StorageComplexType storage)
    {
        generatedSetterHelperImpl(storage, STORAGE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "storage" element
     */
    public gov.loc.premis.v3.StorageComplexType addNewStorage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StorageComplexType target = null;
            target = (gov.loc.premis.v3.StorageComplexType)get_store().add_element_user(STORAGE$0);
            return target;
        }
    }
}
