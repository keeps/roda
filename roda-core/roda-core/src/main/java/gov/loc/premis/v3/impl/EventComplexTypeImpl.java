/*
 * XML Type:  eventComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML eventComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EventComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EventComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventIdentifier");
    private static final javax.xml.namespace.QName EVENTTYPE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventType");
    private static final javax.xml.namespace.QName EVENTDATETIME$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDateTime");
    private static final javax.xml.namespace.QName EVENTDETAILINFORMATION$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetailInformation");
    private static final javax.xml.namespace.QName EVENTOUTCOMEINFORMATION$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeInformation");
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIER$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifier");
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIER$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifier");
    private static final javax.xml.namespace.QName XMLID$14 = 
        new javax.xml.namespace.QName("", "xmlID");
    private static final javax.xml.namespace.QName VERSION$16 = 
        new javax.xml.namespace.QName("", "version");
    
    
    /**
     * Gets the "eventIdentifier" element
     */
    public gov.loc.premis.v3.EventIdentifierComplexType getEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.EventIdentifierComplexType)get_store().find_element_user(EVENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventIdentifier" element
     */
    public void setEventIdentifier(gov.loc.premis.v3.EventIdentifierComplexType eventIdentifier)
    {
        generatedSetterHelperImpl(eventIdentifier, EVENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventIdentifier" element
     */
    public gov.loc.premis.v3.EventIdentifierComplexType addNewEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.EventIdentifierComplexType)get_store().add_element_user(EVENTIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Gets the "eventType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEventType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(EVENTTYPE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventType" element
     */
    public void setEventType(gov.loc.premis.v3.StringPlusAuthority eventType)
    {
        generatedSetterHelperImpl(eventType, EVENTTYPE$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEventType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(EVENTTYPE$2);
            return target;
        }
    }
    
    /**
     * Gets the "eventDateTime" element
     */
    public java.lang.String getEventDateTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDATETIME$4, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventDateTime" element
     */
    public org.apache.xmlbeans.XmlString xgetEventDateTime()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDATETIME$4, 0);
            return target;
        }
    }
    
    /**
     * Sets the "eventDateTime" element
     */
    public void setEventDateTime(java.lang.String eventDateTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDATETIME$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTDATETIME$4);
            }
            target.setStringValue(eventDateTime);
        }
    }
    
    /**
     * Sets (as xml) the "eventDateTime" element
     */
    public void xsetEventDateTime(org.apache.xmlbeans.XmlString eventDateTime)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDATETIME$4, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTDATETIME$4);
            }
            target.set(eventDateTime);
        }
    }
    
    /**
     * Gets array of all "eventDetailInformation" elements
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType[] getEventDetailInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENTDETAILINFORMATION$6, targetList);
            gov.loc.premis.v3.EventDetailInformationComplexType[] result = new gov.loc.premis.v3.EventDetailInformationComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "eventDetailInformation" element
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType getEventDetailInformationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().find_element_user(EVENTDETAILINFORMATION$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "eventDetailInformation" element
     */
    public int sizeOfEventDetailInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTDETAILINFORMATION$6);
        }
    }
    
    /**
     * Sets array of all "eventDetailInformation" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventDetailInformationArray(gov.loc.premis.v3.EventDetailInformationComplexType[] eventDetailInformationArray)
    {
        check_orphaned();
        arraySetterHelper(eventDetailInformationArray, EVENTDETAILINFORMATION$6);
    }
    
    /**
     * Sets ith "eventDetailInformation" element
     */
    public void setEventDetailInformationArray(int i, gov.loc.premis.v3.EventDetailInformationComplexType eventDetailInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().find_element_user(EVENTDETAILINFORMATION$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(eventDetailInformation);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventDetailInformation" element
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType insertNewEventDetailInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().insert_element_user(EVENTDETAILINFORMATION$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventDetailInformation" element
     */
    public gov.loc.premis.v3.EventDetailInformationComplexType addNewEventDetailInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventDetailInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventDetailInformationComplexType)get_store().add_element_user(EVENTDETAILINFORMATION$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "eventDetailInformation" element
     */
    public void removeEventDetailInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTDETAILINFORMATION$6, i);
        }
    }
    
    /**
     * Gets array of all "eventOutcomeInformation" elements
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType[] getEventOutcomeInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENTOUTCOMEINFORMATION$8, targetList);
            gov.loc.premis.v3.EventOutcomeInformationComplexType[] result = new gov.loc.premis.v3.EventOutcomeInformationComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "eventOutcomeInformation" element
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType getEventOutcomeInformationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().find_element_user(EVENTOUTCOMEINFORMATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "eventOutcomeInformation" element
     */
    public int sizeOfEventOutcomeInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTOUTCOMEINFORMATION$8);
        }
    }
    
    /**
     * Sets array of all "eventOutcomeInformation" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventOutcomeInformationArray(gov.loc.premis.v3.EventOutcomeInformationComplexType[] eventOutcomeInformationArray)
    {
        check_orphaned();
        arraySetterHelper(eventOutcomeInformationArray, EVENTOUTCOMEINFORMATION$8);
    }
    
    /**
     * Sets ith "eventOutcomeInformation" element
     */
    public void setEventOutcomeInformationArray(int i, gov.loc.premis.v3.EventOutcomeInformationComplexType eventOutcomeInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().find_element_user(EVENTOUTCOMEINFORMATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(eventOutcomeInformation);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventOutcomeInformation" element
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType insertNewEventOutcomeInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().insert_element_user(EVENTOUTCOMEINFORMATION$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventOutcomeInformation" element
     */
    public gov.loc.premis.v3.EventOutcomeInformationComplexType addNewEventOutcomeInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeInformationComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeInformationComplexType)get_store().add_element_user(EVENTOUTCOMEINFORMATION$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "eventOutcomeInformation" element
     */
    public void removeEventOutcomeInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTOUTCOMEINFORMATION$8, i);
        }
    }
    
    /**
     * Gets array of all "linkingAgentIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] getLinkingAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGAGENTIDENTIFIER$10, targetList);
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingAgentIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType getLinkingAgentIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().find_element_user(LINKINGAGENTIDENTIFIER$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingAgentIdentifier" element
     */
    public int sizeOfLinkingAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGAGENTIDENTIFIER$10);
        }
    }
    
    /**
     * Sets array of all "linkingAgentIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingAgentIdentifierArray(gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] linkingAgentIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingAgentIdentifierArray, LINKINGAGENTIDENTIFIER$10);
    }
    
    /**
     * Sets ith "linkingAgentIdentifier" element
     */
    public void setLinkingAgentIdentifierArray(int i, gov.loc.premis.v3.LinkingAgentIdentifierComplexType linkingAgentIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().find_element_user(LINKINGAGENTIDENTIFIER$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingAgentIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType insertNewLinkingAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().insert_element_user(LINKINGAGENTIDENTIFIER$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType addNewLinkingAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().add_element_user(LINKINGAGENTIDENTIFIER$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingAgentIdentifier" element
     */
    public void removeLinkingAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGAGENTIDENTIFIER$10, i);
        }
    }
    
    /**
     * Gets array of all "linkingObjectIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] getLinkingObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGOBJECTIDENTIFIER$12, targetList);
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingObjectIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType getLinkingObjectIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().find_element_user(LINKINGOBJECTIDENTIFIER$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingObjectIdentifier" element
     */
    public int sizeOfLinkingObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGOBJECTIDENTIFIER$12);
        }
    }
    
    /**
     * Sets array of all "linkingObjectIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingObjectIdentifierArray(gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] linkingObjectIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingObjectIdentifierArray, LINKINGOBJECTIDENTIFIER$12);
    }
    
    /**
     * Sets ith "linkingObjectIdentifier" element
     */
    public void setLinkingObjectIdentifierArray(int i, gov.loc.premis.v3.LinkingObjectIdentifierComplexType linkingObjectIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().find_element_user(LINKINGOBJECTIDENTIFIER$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingObjectIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType insertNewLinkingObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().insert_element_user(LINKINGOBJECTIDENTIFIER$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType addNewLinkingObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().add_element_user(LINKINGOBJECTIDENTIFIER$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingObjectIdentifier" element
     */
    public void removeLinkingObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGOBJECTIDENTIFIER$12, i);
        }
    }
    
    /**
     * Gets the "xmlID" attribute
     */
    public java.lang.String getXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$14);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "xmlID" attribute
     */
    public org.apache.xmlbeans.XmlID xgetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$14);
            return target;
        }
    }
    
    /**
     * True if has "xmlID" attribute
     */
    public boolean isSetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(XMLID$14) != null;
        }
    }
    
    /**
     * Sets the "xmlID" attribute
     */
    public void setXmlID(java.lang.String xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(XMLID$14);
            }
            target.setStringValue(xmlID);
        }
    }
    
    /**
     * Sets (as xml) the "xmlID" attribute
     */
    public void xsetXmlID(org.apache.xmlbeans.XmlID xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$14);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(XMLID$14);
            }
            target.set(xmlID);
        }
    }
    
    /**
     * Unsets the "xmlID" attribute
     */
    public void unsetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(XMLID$14);
        }
    }
    
    /**
     * Gets the "version" attribute
     */
    public gov.loc.premis.v3.Version3.Enum getVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$16);
            if (target == null)
            {
                return null;
            }
            return (gov.loc.premis.v3.Version3.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "version" attribute
     */
    public gov.loc.premis.v3.Version3 xgetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$16);
            return target;
        }
    }
    
    /**
     * True if has "version" attribute
     */
    public boolean isSetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(VERSION$16) != null;
        }
    }
    
    /**
     * Sets the "version" attribute
     */
    public void setVersion(gov.loc.premis.v3.Version3.Enum version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$16);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$16);
            }
            target.setEnumValue(version);
        }
    }
    
    /**
     * Sets (as xml) the "version" attribute
     */
    public void xsetVersion(gov.loc.premis.v3.Version3 version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$16);
            if (target == null)
            {
                target = (gov.loc.premis.v3.Version3)get_store().add_attribute_user(VERSION$16);
            }
            target.set(version);
        }
    }
    
    /**
     * Unsets the "version" attribute
     */
    public void unsetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(VERSION$16);
        }
    }
}
