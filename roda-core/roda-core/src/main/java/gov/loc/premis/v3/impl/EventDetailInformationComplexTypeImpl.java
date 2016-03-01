/*
 * XML Type:  eventDetailInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDetailInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML eventDetailInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EventDetailInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDetailInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EventDetailInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTDETAIL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetail");
    private static final javax.xml.namespace.QName EVENTDETAILEXTENSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetailExtension");
    
    
    /**
     * Gets the "eventDetail" element
     */
    public java.lang.String getEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventDetail" element
     */
    public org.apache.xmlbeans.XmlString xgetEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDETAIL$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "eventDetail" element
     */
    public boolean isSetEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTDETAIL$0) != 0;
        }
    }
    
    /**
     * Sets the "eventDetail" element
     */
    public void setEventDetail(java.lang.String eventDetail)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTDETAIL$0);
            }
            target.setStringValue(eventDetail);
        }
    }
    
    /**
     * Sets (as xml) the "eventDetail" element
     */
    public void xsetEventDetail(org.apache.xmlbeans.XmlString eventDetail)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTDETAIL$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTDETAIL$0);
            }
            target.set(eventDetail);
        }
    }
    
    /**
     * Unsets the "eventDetail" element
     */
    public void unsetEventDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTDETAIL$0, 0);
        }
    }
    
    /**
     * Gets array of all "eventDetailExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getEventDetailExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENTDETAILEXTENSION$2, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "eventDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getEventDetailExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTDETAILEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "eventDetailExtension" element
     */
    public int sizeOfEventDetailExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTDETAILEXTENSION$2);
        }
    }
    
    /**
     * Sets array of all "eventDetailExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventDetailExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] eventDetailExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(eventDetailExtensionArray, EVENTDETAILEXTENSION$2);
    }
    
    /**
     * Sets ith "eventDetailExtension" element
     */
    public void setEventDetailExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType eventDetailExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTDETAILEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(eventDetailExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewEventDetailExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(EVENTDETAILEXTENSION$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewEventDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(EVENTDETAILEXTENSION$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "eventDetailExtension" element
     */
    public void removeEventDetailExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTDETAILEXTENSION$2, i);
        }
    }
}
