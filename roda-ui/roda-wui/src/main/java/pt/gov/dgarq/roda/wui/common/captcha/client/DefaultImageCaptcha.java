/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.captcha.client;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class DefaultImageCaptcha extends AbstractImageCaptcha {

	private final VerticalPanel layout;

	private final Image image;

	private final TextBox input;

	/**
	 * Default image captcha constructor.
	 *
	 */
	public DefaultImageCaptcha() {
		super();
		layout = new VerticalPanel();
		image = getImage();
		input = new TextBox();

		layout.add(image);
		layout.add(input);

		input.setWidth("100%");

		layout.addStyleName("wui-captcha-image-default");
		image.addStyleName("captcha-image");
		input.addStyleName("captcha-input");
	}

	public String getResponse() {
		return input.getText();
	}

	public Widget getWidget() {
		return layout;
	}

	public void refresh() {
		super.refresh();
		input.setText("");
	}

}
