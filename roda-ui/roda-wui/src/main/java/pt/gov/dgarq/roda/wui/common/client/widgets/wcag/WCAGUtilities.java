package pt.gov.dgarq.roda.wui.common.client.widgets.wcag;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Random;

public class WCAGUtilities {
	static WCAGUtilities instance;
	public void makeAccessible(Element element) {
		if(element.getAttribute("align")!=null){
			String className = "";
			if(element.getAttribute("align").equals("right")){
				className="alignRight";
			}else if(element.getAttribute("align").equals("left")){
				className="alignLeft";
			}else if(element.getAttribute("align").equals("center")){
				className="alignCenter";
			}else {
				className="alignJustify";
			}
			element.removeAttribute("align");
			element.addClassName(className);
		}
		
		if(element instanceof InputElement){
			addAttributeIfNonExistent(element, "title", "t_"+Random.nextInt(1000));
		}
		if(element.getChildCount()>0){
			for(int i=0; i<element.getChildCount();i++){
				if(element.getChild(i).getNodeType() == Node.ELEMENT_NODE){
					makeAccessible((Element)element.getChild(i));
				}
			}
		}
		
	}
	

	public static WCAGUtilities getInstance() {
		if(instance==null){
			instance = new WCAGUtilities();
		}
		return instance;
	}


	public static void addAttributeIfNonExistent(Element element, String attributeName, String attributeValue) {
		if(element.getAttribute(attributeName)==null || element.getAttribute(attributeName).equalsIgnoreCase("") ){
			element.setAttribute(attributeName, attributeValue);
		}		
	}
}
