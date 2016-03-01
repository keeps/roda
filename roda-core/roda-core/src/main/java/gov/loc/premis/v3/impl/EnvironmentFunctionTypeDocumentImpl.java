/*
 * An XML document type.
 * Localname: environmentFunctionType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentFunctionTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentFunctionType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentFunctionTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentFunctionTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentFunctionTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTFUNCTIONTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentFunctionType");
    
    
    /**
     * Gets the "environmentFunctionType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEnvironmentFunctionType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ENVIRONMENTFUNCTIONTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentFunctionType" element
     */
    public void setEnvironmentFunctionType(gov.loc.premis.v3.StringPlusAuthority environmentFunctionType)
    {
        generatedSetterHelperImpl(environmentFunctionType, ENVIRONMENTFUNCTIONTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentFunctionType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEnvironmentFunctionType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ENVIRONMENTFUNCTIONTYPE$0);
            return target;
        }
    }
}
