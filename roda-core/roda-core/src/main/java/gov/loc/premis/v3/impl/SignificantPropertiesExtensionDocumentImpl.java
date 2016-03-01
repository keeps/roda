/*
 * An XML document type.
 * Localname: significantPropertiesExtension
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SignificantPropertiesExtensionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one significantPropertiesExtension(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SignificantPropertiesExtensionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SignificantPropertiesExtensionDocument
{
    private static final long serialVersionUID = 1L;
    
    public SignificantPropertiesExtensionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SIGNIFICANTPROPERTIESEXTENSION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "significantPropertiesExtension");
    
    
    /**
     * Gets the "significantPropertiesExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getSignificantPropertiesExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(SIGNIFICANTPROPERTIESEXTENSION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "significantPropertiesExtension" element
     */
    public void setSignificantPropertiesExtension(gov.loc.premis.v3.ExtensionComplexType significantPropertiesExtension)
    {
        generatedSetterHelperImpl(significantPropertiesExtension, SIGNIFICANTPROPERTIESEXTENSION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "significantPropertiesExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewSignificantPropertiesExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(SIGNIFICANTPROPERTIESEXTENSION$0);
            return target;
        }
    }
}
