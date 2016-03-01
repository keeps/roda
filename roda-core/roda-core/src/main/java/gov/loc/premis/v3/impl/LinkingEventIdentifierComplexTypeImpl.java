/*
 * XML Type:  linkingEventIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LinkingEventIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML linkingEventIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LinkingEventIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LinkingEventIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LinkingEventIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifierType");
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifierValue");
    private static final javax.xml.namespace.QName LINKEVENTXMLID$4 = 
        new javax.xml.namespace.QName("", "LinkEventXmlID");
    private static final javax.xml.namespace.QName SIMPLELINK$6 = 
        new javax.xml.namespace.QName("", "simpleLink");
    
    
    /**
     * Gets the "linkingEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLinkingEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LINKINGEVENTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "linkingEventIdentifierType" element
     */
    public void setLinkingEventIdentifierType(gov.loc.premis.v3.StringPlusAuthority linkingEventIdentifierType)
    {
        generatedSetterHelperImpl(linkingEventIdentifierType, LINKINGEVENTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "linkingEventIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLinkingEventIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LINKINGEVENTIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "linkingEventIdentifierValue" element
     */
    public java.lang.String getLinkingEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "linkingEventIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLinkingEventIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "linkingEventIdentifierValue" element
     */
    public void setLinkingEventIdentifierValue(java.lang.String linkingEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LINKINGEVENTIDENTIFIERVALUE$2);
            }
            target.setStringValue(linkingEventIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "linkingEventIdentifierValue" element
     */
    public void xsetLinkingEventIdentifierValue(org.apache.xmlbeans.XmlString linkingEventIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LINKINGEVENTIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LINKINGEVENTIDENTIFIERVALUE$2);
            }
            target.set(linkingEventIdentifierValue);
        }
    }
    
    /**
     * Gets the "LinkEventXmlID" attribute
     */
    public java.lang.String getLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKEVENTXMLID$4);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "LinkEventXmlID" attribute
     */
    public org.apache.xmlbeans.XmlIDREF xgetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKEVENTXMLID$4);
            return target;
        }
    }
    
    /**
     * True if has "LinkEventXmlID" attribute
     */
    public boolean isSetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(LINKEVENTXMLID$4) != null;
        }
    }
    
    /**
     * Sets the "LinkEventXmlID" attribute
     */
    public void setLinkEventXmlID(java.lang.String linkEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(LINKEVENTXMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(LINKEVENTXMLID$4);
            }
            target.setStringValue(linkEventXmlID);
        }
    }
    
    /**
     * Sets (as xml) the "LinkEventXmlID" attribute
     */
    public void xsetLinkEventXmlID(org.apache.xmlbeans.XmlIDREF linkEventXmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlIDREF target = null;
            target = (org.apache.xmlbeans.XmlIDREF)get_store().find_attribute_user(LINKEVENTXMLID$4);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlIDREF)get_store().add_attribute_user(LINKEVENTXMLID$4);
            }
            target.set(linkEventXmlID);
        }
    }
    
    /**
     * Unsets the "LinkEventXmlID" attribute
     */
    public void unsetLinkEventXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(LINKEVENTXMLID$4);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
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
            return get_store().find_attribute_user(SIMPLELINK$6) != null;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(SIMPLELINK$6);
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
            target = (org.apache.xmlbeans.XmlAnyURI)get_store().find_attribute_user(SIMPLELINK$6);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlAnyURI)get_store().add_attribute_user(SIMPLELINK$6);
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
            get_store().remove_attribute(SIMPLELINK$6);
        }
    }
}
