/*
 * An XML document type.
 * Localname: eventDetailExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventDetailExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventDetailExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventDetailExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventDetailExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventDetailExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTDETAILEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventDetailExtension");
    
    
    /**
     * Gets the "eventDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getEventDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTDETAILEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventDetailExtension" element
     */
    public void setEventDetailExtension(gov.loc.premis.v3.ExtensionComplexType eventDetailExtension)
    {
        generatedSetterHelperImpl(eventDetailExtension, EVENTDETAILEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewEventDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(EVENTDETAILEXTENSION$0);
            return target;
        }
    }
}
