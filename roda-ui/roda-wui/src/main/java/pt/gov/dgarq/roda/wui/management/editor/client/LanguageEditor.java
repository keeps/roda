/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.editor.client;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;

import pt.gov.dgarq.roda.core.data.DescriptionObject;
import pt.gov.dgarq.roda.core.data.eadc.EadCValue;
import pt.gov.dgarq.roda.core.data.eadc.LangmaterialLanguages;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.DisseminationConstants;

/**
 * @author Luis Faria
 * 
 */
public class LanguageEditor implements MetadataElementEditor {
	private static DisseminationConstants constants = (DisseminationConstants) GWT
			.create(DisseminationConstants.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	public class LanguageCheckBox {
		private final HorizontalPanel layout;

		private final String langCode;

		private final CheckBox cb;

		private final Label description;

		private final List<ChangeListener> listeners;

		public LanguageCheckBox(String langCode, String description) {
			this.langCode = langCode;
			this.description = new Label(description);
			layout = new HorizontalPanel();
			cb = new CheckBox();

			listeners = new Vector<ChangeListener>();

			layout.add(cb);
			layout.add(this.description);

			this.description.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					cb.setChecked(!cb.isChecked());
					onChange(cb);
				}

			});

			this.cb.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					onChange(cb);
				}

			});

			layout.addStyleName("languageBox");
			cb.addStyleName("languageBox-checkbox");
			this.description.addStyleName("languagebox-label");
		}

		public String getLangCode() {
			return langCode;
		}

		public boolean isChecked() {
			return cb.isChecked();
		}

		public void setChecked(boolean checked) {
			cb.setChecked(checked);
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

	}

	private final FlowPanel layout;

	private final List<LanguageCheckBox> languages;

	private final List<ChangeListener> listeners;

	private boolean initialized;

	private final List<Command> initListeners;

	public LanguageEditor() {
		layout = new FlowPanel();
		languages = new Vector<LanguageCheckBox>();
		listeners = new Vector<ChangeListener>();
		initialized = false;
		initListeners = new Vector<Command>();

		ControlledVocabularyEditor.getControlledVocabulary(
				DescriptionObject.LANGMATERIAL_LANGUAGES,
				new AsyncCallback<String[]>() {

					public void onFailure(Throwable caught) {
						logger.error("Error getting languages controlled"
								+ " vocabullary", caught);

					}

					public void onSuccess(String[] langcodes) {
						for (int i = 0; i < langcodes.length; i++) {
							String description;
							try {
								description = constants.getString("lang_"
										+ langcodes[i]);
							} catch (MissingResourceException e) {
								description = langcodes[i];
							}

							createLanguage(langcodes[i], description);
							updateLayout();
							initialized = true;
							onLoad();
						}

					}

				});

		layout.addStyleName("wui-editor-languages");
	}

	private void onLoad() {
		for (Command c : initListeners) {
			c.execute();
		}
		initListeners.clear();
	}

	private void ensureLoaded(Command command) {
		if (initialized) {
			command.execute();
		} else {
			initListeners.add(command);
		}
	}

	private LanguageCheckBox createLanguage(String langCode, String description) {
		LanguageCheckBox lcb = new LanguageCheckBox(langCode, description);
		lcb.addChangeListener(new ChangeListener() {

			public void onChange(Widget sender) {
				LanguageEditor.this.onChange(sender);
			}

		});
		languages.add(lcb);

		return lcb;
	}

	private void updateLayout() {
		layout.clear();
		for (LanguageCheckBox language : languages) {
			layout.add(language.getWidget());
		}
	}

	public void setValue(EadCValue value) {
		if (value != null && value instanceof LangmaterialLanguages) {
			LangmaterialLanguages langmaterialLanguages = (LangmaterialLanguages) value;

			final String[] languages = langmaterialLanguages
					.getLangmaterialLanguages();
			ensureLoaded(new Command() {

				public void execute() {
					int languagesFoundCount = 0;
					for (LanguageCheckBox lcb : LanguageEditor.this.languages) {
						lcb.setChecked(false);
						for (int i = 0; i < languages.length; i++) {
							if (lcb.getLangCode().equals(languages[i])) {
								lcb.setChecked(true);
								languagesFoundCount++;
							}
						}
					}

					if (languagesFoundCount < languages.length) {
						logger.error("Some languages were not found"
								+ " in language list: "
								+ Tools.toString(languages));
					}
				}

			});

		}
	}

	public EadCValue getValue() {
		List<String> langCodes = new Vector<String>();
		for(LanguageCheckBox language : languages) {
			if (language.isChecked()) {
				langCodes.add(language.getLangCode());
			}
		}

		return (langCodes.size() == 0) ? null : new LangmaterialLanguages(
				(String[]) langCodes.toArray(new String[] {}));
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	protected void onChange(Widget sender) {
		for(ChangeListener listener : listeners) {
			listener.onChange(sender);
		}
	}

	public Widget getWidget() {
		return layout;
	}

	public boolean isEmpty() {
		return languages.size() == 0;
	}

	public boolean isValid() {
		return true;
	}
}
