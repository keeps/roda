/*
 * XML Type:  objectIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML objectIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class ObjectIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public ObjectIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifierType");
    private static final javax.xml.namespace.QName OBJECTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifierValue");
    private static final javax.xml.namespace.QName SIMPLELINK$4 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "objectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "objectIdentifierType" element
     */
    public void setObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority objectIdentifierType)
    {
        generatedSetterHelperImpl(objectIdentifierType, OBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "objectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "objectIdentifierValue" element
     */
    public java.lang.String getObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "objectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OBJECTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "objectIdentifierValue" element
     */
    public void setObjectIdentifierValue(java.lang.String objectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OBJECTIDENTIFIERVALUE$2);
            }
            target.setStringValue(objectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "objectIdentifierValue" element
     */
    public void xsetObjectIdentifierValue(org.apache.xmlbeans.XmlString objectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OBJECTIDENTIFIERVALUE$2);
            }
            target.set(objectIdentifierValue);
        }
    }
    
    /**
     * Gets the "simpleLink" attribute
     */
    public java.lang.String getSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "simpleLink" attribute
     */
    public org.apache.xmlbeans.XmlAnyURI xgetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
            return target;
        }
    }
    
    /**
     * True if has "simpleLink" attribute
     */
    public boolean isSetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(SIMPLELINK$4) != null;
        }
    }
    
    /**
     * Sets the "simpleLink" attribute
     */
    public void setSimpleLink(java.lang.String simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$4);
            }
            target.setStringValue(simpleLink);
        }
    }
    
    /**
     * Sets (as xml) the "simpleLink" attribute
     */
    public void xsetSimpleLink(org.apache.xmlbeans.XmlAnyURI simpleLink)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlAnyURI target = null;
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$4);
            }
            target.set(simpleLink);
        }
    }
    
    /**
     * Unsets the "simpleLink" attribute
     */
    public void unsetSimpleLink()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(SIMPLELINK$4);
        }
    }
}
