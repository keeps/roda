package pt.gov.dgarq.roda.wui.common.client.widgets.wcag;

import com.google.gwt.user.cellview.client.SimplePager;

public class AccessibleSimplePager extends SimplePager{

	public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, boolean showLastPageButton){
		super(location, showFastForwardButton, showLastPageButton);
		makeAccessible();
	}
	
	public AccessibleSimplePager(){
		super();
		makeAccessible();
	}
	public AccessibleSimplePager(TextLocation location){
		super(location);
		makeAccessible();
	}
	public AccessibleSimplePager(TextLocation location, boolean showFastForwardButton, int fastForwardRows, boolean showLastPageButton){
		super(location, showFastForwardButton,fastForwardRows, showLastPageButton);
		makeAccessible();
	}
	
	private void makeAccessible() {
		// TODO Auto-generated method stub
		
	}
}
