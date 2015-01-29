/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.eadc.BioghistChronitem;
import pt.gov.dgarq.roda.core.data.eadc.BioghistChronlist;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.wui.common.client.images.CommonImageBundle;
import pt.gov.dgarq.roda.wui.common.client.widgets.DatePicker;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MetadataEditorConstants;

/**
 * @author Luis Faria
 * 
 */
public class ChronListEditor implements MetadataElementEditor {

	private static MetadataEditorConstants constants = (MetadataEditorConstants) GWT
			.create(MetadataEditorConstants.class);

	private static CommonImageBundle commonImageBundle = (CommonImageBundle) GWT
			.create(CommonImageBundle.class);

	private final DockPanel layout;

	private final WUIButton addChronItem;

	private final VerticalPanel chronListLayout;

	private final List<ChronItemEditor> items;

	private final List<ChangeListener> listeners;

	/**
	 * Listener for chron items
	 *
	 */
	public interface ChronItemListener extends ChangeListener {
		/**
		 * Called when an chron item is removed
		 * @param sender
		 */
		public void onRemove(ChronItemEditor sender);
	}

	/**
	 * Editor for chron items
	 *
	 */
	public class ChronItemEditor {

		private final HorizontalPanel layout;

		private final VerticalPanel subLayout;

		private final Grid datesLayout;

		private final Label initialDateLabel;

		private final DatePicker initialDate;

		private final Label finalDateLabel;

		private final DatePicker finalDate;

		private final TextArea content;

		private final Image remove;

		private final List<ChronItemListener> listeners;

		/**
		 * Create a new biological history chronological item editor
		 */
		public ChronItemEditor() {
			layout = new HorizontalPanel();
			subLayout = new VerticalPanel();
			datesLayout = new Grid(2, 2);

			initialDateLabel = new Label(constants.chronitemInitialDate());
			finalDateLabel = new Label(constants.chronitemFinalDate());
			initialDate = new DatePicker(true);
			finalDate = new DatePicker(false);
			content = new TextArea();
			remove = commonImageBundle.minus().createImage();

			datesLayout.setWidget(0, 0, initialDateLabel);
			datesLayout.setWidget(0, 1, initialDate);
			datesLayout.setWidget(1, 0, finalDateLabel);
			datesLayout.setWidget(1, 1, finalDate);

			subLayout.add(datesLayout);
			subLayout.add(content);

			layout.add(subLayout);
			layout.add(remove);

			listeners = new Vector<ChronItemListener>();

			remove.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					onRemove();
				}

			});

			ChangeListener changeListener = new ChangeListener() {

				public void onChange(Widget sender) {
					ChronItemEditor.this.onChange(sender);
				}

			};

			initialDate.addChangeListener(changeListener);
			finalDate.addChangeListener(changeListener);
			content.addKeyboardListener(new KeyboardListener() {

				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				}

				public void onKeyPress(Widget sender, char keyCode,
						int modifiers) {
				}

				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					ChronItemEditor.this.onChange(sender);
				}

			});

			layout.setCellWidth(subLayout, "100%");
			layout.setCellVerticalAlignment(remove, HasAlignment.ALIGN_MIDDLE);
			layout
					.setCellHorizontalAlignment(remove,
							HasAlignment.ALIGN_CENTER);
			layout.addStyleName("wui-editor-chronitem");
			subLayout.addStyleName("wui-editor-chronitem-center");
			datesLayout.addStyleName("chronitem-date-layout");
			initialDate.addStyleName("chronitem-date-initial");
			finalDate.addStyleName("chronitem-date-final");
			content.addStyleName("chronlist-content");
			remove.addStyleName("chronitem-remove");

		}

		/**
		 * Get editor widget
		 * @return
		 */
		public Widget getWidget() {
			return layout;
		}

		/**
		 * Set the biological history chronological item value
		 * @param item
		 */
		public void setChronItem(BioghistChronitem item) {
			if (item.getDateInitial() != null) {
				initialDate.setISODate(item.getDateInitial());
			}
			if (item.getDateFinal() != null) {
				finalDate.setISODate(item.getDateFinal());
			}
			content.setText(item.getEvent());
		}

		/**
		 * Get the biological history chronological item value
		 * @return
		 */
		public BioghistChronitem getChronItem() {
			BioghistChronitem ret = null;
			if (initialDate.getISODate() != null
					|| finalDate.getISODate() != null) {
				ret = new BioghistChronitem(content.getText(), initialDate
						.getISODate(), finalDate.getISODate());
			}
			return ret;
		}

		/**
		 * Add a chron item listener
		 * @param listener
		 */
		public void addChronItemListener(ChronItemListener listener) {
			listeners.add(listener);
		}

		/**
		 * Remove a chron item listener
		 * @param listener
		 */
		public void removeChronItemListener(ChronItemListener listener) {
			listeners.remove(listener);
		}

		protected void onChange(Widget sender) {
			for (ChronItemListener listener : listeners) {
				listener.onChange(sender);
			}
		}

		protected void onRemove() {
			for (ChronItemListener listener : listeners) {
				listener.onRemove(this);
			}
		}

	}

	/**
	 * Editor for a chronological list of events
	 */
	public ChronListEditor() {
		layout = new DockPanel();
		addChronItem = new WUIButton(constants.newChronItem(),
				WUIButton.Left.ROUND,
				WUIButton.Right.PLUS);
		chronListLayout = new VerticalPanel();

		layout.add(chronListLayout, DockPanel.CENTER);
		layout.add(addChronItem, DockPanel.SOUTH);

		items = new Vector<ChronItemEditor>();
		listeners = new Vector<ChangeListener>();

		addChronItem.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				createChronItem();
				updateLayout();
				onChange(layout);
			}

		});

		layout.addStyleName("wui-editor-chronlist");
		addChronItem.addStyleName("chronlist-add");
		chronListLayout.addStyleName("chronlist-itemlist");
	}

	protected ChronItemEditor createChronItem() {
		ChronItemEditor editor = new ChronItemEditor();
		editor.addChronItemListener(new ChronItemListener() {

			public void onRemove(ChronItemEditor sender) {
				items.remove(sender);
				updateLayout();
				ChronListEditor.this.onChange(layout);
			}

			public void onChange(Widget sender) {
				ChronListEditor.this.onChange(sender);
			}

		});
		items.add(editor);
		return editor;
	}

	protected void updateLayout() {
		chronListLayout.clear();
		for (ChronItemEditor item : items) {
			chronListLayout.add(item.getWidget());
		}
	}

	public void setValue(EadCValue value) {
		if (value instanceof BioghistChronlist) {
			BioghistChronlist chronlist = (BioghistChronlist) value;
			items.clear();
			for (int i = 0; i < chronlist.getBioghistChronitems().length; i++) {
				BioghistChronitem item = chronlist.getBioghistChronitems()[i];
				ChronItemEditor editor = createChronItem();
				editor.setChronItem(item);
			}
			updateLayout();
		}
	}

	public EadCValue getValue() {
		List<BioghistChronitem> chronItems = new Vector<BioghistChronitem>();
		for (ChronItemEditor item : items) {
			BioghistChronitem chronItem = item.getChronItem();
			if (chronItem != null) {
				chronItems.add(chronItem);
			}
		}
		return chronItems.size() == 0 ? null : new BioghistChronlist(chronItems
				.toArray(new BioghistChronitem[] {}));
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	protected void onChange(Widget sender) {
		for (ChangeListener listener : listeners) {
			listener.onChange(sender);
		}
	}

	public Widget getWidget() {
		return layout;
	}

	public boolean isEmpty() {
		return items.size() == 0;
	}

	public boolean isValid() {
		return true;
	}
}
