/*
 * An XML document type.
 * Localname: storageMedium
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StorageMediumDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one storageMedium(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StorageMediumDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StorageMediumDocument
{
    private static final long serialVersionUID = 1L;
    
    public StorageMediumDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STORAGEMEDIUM$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "storageMedium");
    
    
    /**
     * Gets the "storageMedium" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getStorageMedium()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(STORAGEMEDIUM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "storageMedium" element
     */
    public void setStorageMedium(gov.loc.premis.v3.StringPlusAuthority storageMedium)
    {
        generatedSetterHelperImpl(storageMedium, STORAGEMEDIUM$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
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
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(STORAGEMEDIUM$0);
            return target;
        }
    }
}
