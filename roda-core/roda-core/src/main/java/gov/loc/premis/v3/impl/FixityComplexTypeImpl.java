/*
 * XML Type:  fixityComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.FixityComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML fixityComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class FixityComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.FixityComplexType
{
    private static final long serialVersionUID = 1L;
    
    public FixityComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName MESSAGEDIGESTALGORITHM$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "messageDigestAlgorithm");
    private static final javax.xml.namespace.QName MESSAGEDIGEST$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "messageDigest");
    private static final javax.xml.namespace.QName MESSAGEDIGESTORIGINATOR$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "messageDigestOriginator");
    
    
    /**
     * Gets the "messageDigestAlgorithm" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getMessageDigestAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(MESSAGEDIGESTALGORITHM$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "messageDigestAlgorithm" element
     */
    public void setMessageDigestAlgorithm(gov.loc.premis.v3.StringPlusAuthority messageDigestAlgorithm)
    {
        generatedSetterHelperImpl(messageDigestAlgorithm, MESSAGEDIGESTALGORITHM$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "messageDigestAlgorithm" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewMessageDigestAlgorithm()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(MESSAGEDIGESTALGORITHM$0);
            return target;
        }
    }
    
    /**
     * Gets the "messageDigest" element
     */
    public java.lang.String getMessageDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MESSAGEDIGEST$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "messageDigest" element
     */
    public org.apache.xmlbeans.XmlString xgetMessageDigest()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MESSAGEDIGEST$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "messageDigest" element
     */
    public void setMessageDigest(java.lang.String messageDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(MESSAGEDIGEST$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(MESSAGEDIGEST$2);
            }
            target.setStringValue(messageDigest);
        }
    }
    
    /**
     * Sets (as xml) the "messageDigest" element
     */
    public void xsetMessageDigest(org.apache.xmlbeans.XmlString messageDigest)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(MESSAGEDIGEST$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(MESSAGEDIGEST$2);
            }
            target.set(messageDigest);
        }
    }
    
    /**
     * Gets the "messageDigestOriginator" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getMessageDigestOriginator()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(MESSAGEDIGESTORIGINATOR$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "messageDigestOriginator" element
     */
    public boolean isSetMessageDigestOriginator()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(MESSAGEDIGESTORIGINATOR$4) != 0;
        }
    }
    
    /**
     * Sets the "messageDigestOriginator" element
     */
    public void setMessageDigestOriginator(gov.loc.premis.v3.StringPlusAuthority messageDigestOriginator)
    {
        generatedSetterHelperImpl(messageDigestOriginator, MESSAGEDIGESTORIGINATOR$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "messageDigestOriginator" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewMessageDigestOriginator()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(MESSAGEDIGESTORIGINATOR$4);
            return target;
        }
    }
    
    /**
     * Unsets the "messageDigestOriginator" element
     */
    public void unsetMessageDigestOriginator()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(MESSAGEDIGESTORIGINATOR$4, 0);
        }
    }
}
