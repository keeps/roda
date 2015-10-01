package pt.gov.dgarq.roda.wui.common.client.tools;

import com.google.gwt.core.client.JavaScriptObject;

public class JavascriptUtils {

  public static native void runHighlighter() /*-{
		$wnd.jQuery('pre code').each(function(i, block) {
			$wnd.hljs.highlightBlock(block);
		});
  }-*/;

  public static native void runHighlighter(JavaScriptObject parent) /*-{
		$wnd.jQuery(parent).find('pre code').each(function(i, block) {
			$wnd.hljs.highlightBlock(block);
		});
  }-*/;

  public static native void slideToggle(String selector) /*-{
		$wnd.jQuery(selector).click(function() {
			$wnd.jQuery(this).next().slideToggle(300, function() {
				// Animation complete.
			});
		});
  }-*/;

  public static native void slideToggle(JavaScriptObject parent, String selector) /*-{
		$wnd.jQuery(parent).find(selector).click(function() {
			$wnd.jQuery(this).next().slideToggle(300, function() {
				// Animation complete.
			});
		});
  }-*/;

  public static native void smoothScroll() /*-{
		$wnd.jQuery('a[href^="#"]').on(
				'click',
				function(event) {
					var target = $wnd.jQuery(this.hash.replace(
							/(:|\.|\[|\]|,)/g, "\\$1"));
					if (target.length) {
						event.preventDefault();
						$wnd.jQuery('html, body').animate({
							scrollTop : target.offset().top
						}, 1000);
					} else {
						event.preventDefault();
						// TODO send error
						alert(this.hash + " not found");
					}
				});
  }-*/;

  public static native void smoothScroll(JavaScriptObject parent) /*-{
		$wnd.jQuery(parent).find('a[href^="#"]').on(
				'click',
				function(event) {
					var target = $wnd.jQuery(this.hash.replace(
							/(:|\.|\[|\]|,)/g, "\\$1"));
					if (target.length) {
						event.preventDefault();
						$wnd.jQuery('html, body').animate({
							scrollTop : target.offset().top
						}, 1000);
					} else {
						event.preventDefault();
						// TODO send error
						alert(this.hash + " not found");
					}
				});
  }-*/;

}
