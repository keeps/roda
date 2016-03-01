/*
 * An XML document type.
 * Localname: eventOutcomeDetailExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeDetailExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventOutcomeDetailExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventOutcomeDetailExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeDetailExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeDetailExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAILEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetailExtension");
    
    
    /**
     * Gets the "eventOutcomeDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getEventOutcomeDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTOUTCOMEDETAILEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventOutcomeDetailExtension" element
     */
    public void setEventOutcomeDetailExtension(gov.loc.premis.v3.ExtensionComplexType eventOutcomeDetailExtension)
    {
        generatedSetterHelperImpl(eventOutcomeDetailExtension, EVENTOUTCOMEDETAILEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventOutcomeDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewEventOutcomeDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(EVENTOUTCOMEDETAILEXTENSION$0);
            return target;
        }
    }
}
