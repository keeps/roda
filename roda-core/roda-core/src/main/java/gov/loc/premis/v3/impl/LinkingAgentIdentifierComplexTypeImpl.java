/*
 * XML Type:  linkingAgentIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingAgentIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML linkingAgentIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LinkingAgentIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingAgentIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LinkingAgentIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifierType");
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifierValue");
    private static final javax.xml.namespace.QName LINKINGAGENTROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentRole");
    private static final javax.xml.namespace.QName LINKAGENTXMLID$6 = 
        new javax.xml.namespace.QName("", "LinkAgentXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$8 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "linkingAgentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGAGENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentIdentifierType" element
     */
    public void setLinkingAgentIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingAgentIdentifierType)
    {
        generatedSetterHelperImpl(linkingAgentIdentifierType, LINKINGAGENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingAgentIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGAGENTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "linkingAgentIdentifierValue" element
     */
    public java.lang.String getLinkingAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingAgentIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingAgentIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingAgentIdentifierValue" element
     */
    public void setLinkingAgentIdentifierValue(java.lang.String linkingAgentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGAGENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(linkingAgentIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingAgentIdentifierValue" element
     */
    public void xsetLinkingAgentIdentifierValue(org.apache.xmlbeans.XmlString linkingAgentIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGAGENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGAGENTIDENTIFIERVALUE$2);
            }
            target.set(linkingAgentIdentifierValue);
        }
    }
    
    /**
     * Gets array of all "linkingAgentRole" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getLinkingAgentRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGAGENTROLE$4, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingAgentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingAgentRoleArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGAGENTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingAgentRole" element
     */
    public int sizeOfLinkingAgentRoleArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGAGENTROLE$4);
        }
    }
    
    /**
     * Sets array of all "linkingAgentRole" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingAgentRoleArray(gov.loc.premis.v3.StringPlusAuthority[] linkingAgentRoleArray)
    {
        check_orphaned();
        arraySetterHelper(linkingAgentRoleArray, LINKINGAGENTROLE$4);
    }
    
    /**
     * Sets ith "linkingAgentRole" element
     */
    public void setLinkingAgentRoleArray(int i, gov.loc.premis.v3.StringPlusAuthority linkingAgentRole)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGAGENTROLE$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingAgentRole);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewLinkingAgentRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(LINKINGAGENTROLE$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingAgentRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGAGENTROLE$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingAgentRole" element
     */
    public void removeLinkingAgentRole(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGAGENTROLE$4, i);
        }
    }
    
    /**
     * Gets the "LinkAgentXmlID" attribute
     */
    public java.lang.String getLinkAgentXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKAGENTXMLID$6);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "LinkAgentXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetLinkAgentXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKAGENTXMLID$6);
            return target;
        }
    }
    
    /**
     * True if has "LinkAgentXmlID" attribute
     */
    public boolean isSetLinkAgentXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LINKAGENTXMLID$6) != null;
        }
    }
    
    /**
     * Sets the "LinkAgentXmlID" attribute
     */
    public void setLinkAgentXmlID(java.lang.String linkAgentXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKAGENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LINKAGENTXMLID$6);
            }
            target.setStringValue(linkAgentXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "LinkAgentXmlID" attribute
     */
    public void xsetLinkAgentXmlID(org.apache.xmlbeans.XmlIDREF linkAgentXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKAGENTXMLID$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(LINKAGENTXMLID$6);
            }
            target.set(linkAgentXmlID);
        }
    }
    
    /**
     * Unsets the "LinkAgentXmlID" attribute
     */
    public void unsetLinkAgentXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LINKAGENTXMLID$6);
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
