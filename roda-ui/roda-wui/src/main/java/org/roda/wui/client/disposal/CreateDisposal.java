package org.roda.wui.client.disposal;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import java.util.Collections;
import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDisposal extends Composite {
    public static final HistoryResolver RESOLVER = new HistoryResolver() {

        @Override
        public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
            getInstance().resolve(historyTokens, callback);
        }

        @Override
        public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
            UserLogin.getInstance().checkRoles(new HistoryResolver[] {CreateDisposalSchedule.RESOLVER, CreateDisposalHold.RESOLVER}, false, callback);

        }

        @Override
        public List<String> getHistoryPath() {
            return Collections.singletonList(getHistoryToken());
        }

        @Override
        public String getHistoryToken() {
            return "create";
        }
    };

    private static CreateDisposal instance = null;

    /**
     * Get the singleton instance
     *
     * @return the instance
     */
    public static CreateDisposal getInstance() {
        if (instance == null) {
            instance = new CreateDisposal();
        }
        return instance;
    }

    private boolean initialized;

    private HTMLWidgetWrapper page;

    private CreateDisposal() {
        initialized = false;
    }

    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
        if (historyTokens.isEmpty()) {
            callback.onSuccess(page);
        } else if (historyTokens.get(0).equals(CreateDisposalSchedule.RESOLVER.getHistoryToken())) {
            CreateDisposalSchedule.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
        } else if (historyTokens.get(0).equals(CreateDisposalHold.RESOLVER.getHistoryToken())) {
            CreateDisposalHold.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
        }
    }
}
