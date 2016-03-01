/*
 * XML Type:  linkingObjectIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingObjectIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML linkingObjectIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LinkingObjectIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingObjectIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LinkingObjectIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifierType");
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifierValue");
    private static final javax.xml.namespace.QName LINKINGOBJECTROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectRole");
    private static final javax.xml.namespace.QName LINKOBJECTXMLID$6 = 
        new javax.xml.namespace.QName("", "LinkObjectXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$8 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "linkingObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGOBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectIdentifierType" element
     */
    public void setLinkingObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingObjectIdentifierType)
    {
        generatedSetterHelperImpl(linkingObjectIdentifierType, LINKINGOBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingObjectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGOBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "linkingObjectIdentifierValue" element
     */
    public java.lang.String getLinkingObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingObjectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingObjectIdentifierValue" element
     */
    public void setLinkingObjectIdentifierValue(java.lang.String linkingObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGOBJECTIDENTIFIERVALUE$2);
            }
            target.setStringValue(linkingObjectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingObjectIdentifierValue" element
     */
    public void xsetLinkingObjectIdentifierValue(org.apache.xmlbeans.XmlString linkingObjectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGOBJECTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGOBJECTIDENTIFIERVALUE$2);
            }
            target.set(linkingObjectIdentifierValue);
        }
    }
    
    /**
     * Gets array of all "linkingObjectRole" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getLinkingObjectRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGOBJECTROLE$4, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingObjectRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingObjectRoleArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGOBJECTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingObjectRole" element
     */
    public int sizeOfLinkingObjectRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGOBJECTROLE$4);
        }
    }
    
    /**
     * Sets array of all "linkingObjectRole" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingObjectRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingObjectRoleArray)
    {
        check_orphaned();
        arraySetterHelper(linkingObjectRoleArray, LINKINGOBJECTROLE$4);
    }
    
    /**
     * Sets ith "linkingObjectRole" element
     */
    public void setLinkingObjectRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingObjectRole)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGOBJECTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingObjectRole);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewLinkingObjectRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(LINKINGOBJECTROLE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingObjectRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGOBJECTROLE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingObjectRole" element
     */
    public void removeLinkingObjectRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGOBJECTROLE$4, i);
        }
    }
    
    /**
     * Gets the "LinkObjectXmlID" attribute
     */
    public java.lang.String getLinkObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKOBJECTXMLID$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "LinkObjectXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetLinkObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKOBJECTXMLID$6);
            return target;
        }
    }
    
    /**
     * True if has "LinkObjectXmlID" attribute
     */
    public boolean isSetLinkObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LINKOBJECTXMLID$6) != null;
        }
    }
    
    /**
     * Sets the "LinkObjectXmlID" attribute
     */
    public void setLinkObjectXmlID(java.lang.String linkObjectXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKOBJECTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LINKOBJECTXMLID$6);
            }
            target.setStringValue(linkObjectXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "LinkObjectXmlID" attribute
     */
    public void xsetLinkObjectXmlID(org.apache.xmlbeans.XmlIDREF linkObjectXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKOBJECTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(LINKOBJECTXMLID$6);
            }
            target.set(linkObjectXmlID);
        }
    }
    
    /**
     * Unsets the "LinkObjectXmlID" attribute
     */
    public void unsetLinkObjectXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LINKOBJECTXMLID$6);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
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
            return get_store().find_attribute_user(SIMPLELINK$8) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$8);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$8);
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
            get_store().remove_attribute(SIMPLELINK$8);
        }
    }
}
