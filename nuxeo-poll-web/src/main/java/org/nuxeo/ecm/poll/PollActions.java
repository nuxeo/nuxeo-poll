/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.poll;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.jboss.seam.international.StatusMessage.Severity;
import static org.nuxeo.ecm.poll.Constants.SURVEY_DOCUMENT_TYPE;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

/**
 * Handles Poll related web actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Name("pollActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class PollActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SURVEYS_TAB = ":TAB_SURVEYS";

    protected static final Log log = LogFactory.getLog(PollActions.class);

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient WebActions webActions;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected transient PollService pollService;

    protected boolean showCreateForm = false;

    protected DocumentModel newPoll;

    public DocumentModel getPollsContainer() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return getPollsContainer(currentDocument);
    }

    public DocumentModel getPollsContainer(DocumentModel doc)
            throws ClientException {
        return pollService.getPollsContainer(doc);
    }

    public boolean isShowCreateForm() {
        return showCreateForm;
    }

    public void toggleCreateForm() {
        showCreateForm = !showCreateForm;
    }

    public void toggleAndReset() {
        toggleCreateForm();
        resetNewPoll();
    }

    protected void resetNewPoll() {
        newPoll = null;
    }

    public DocumentModel getNewPoll() throws ClientException {
        if (newPoll == null) {
            newPoll = documentManager.createDocumentModel(SURVEY_DOCUMENT_TYPE);
        }
        return newPoll;
    }

    public void createPoll() throws ClientException {
        documentActions.saveDocument(newPoll);
        resetNewPoll();
        toggleCreateForm();
    }

    public Poll toPoll(DocumentModel poll) {
        return PollHelper.toPoll(poll);
    }

    public void openPoll(DocumentModel poll) throws ClientException {
        pollService.openPoll(toPoll(poll));
        facesMessages.addFromResourceBundle(Severity.INFO, "label.poll.opened");
    }

    public void closePoll(DocumentModel poll) throws ClientException {
        pollService.closePoll(toPoll(poll));
        facesMessages.addFromResourceBundle(Severity.INFO, "label.poll.closed");
    }

    public String backToPollsListing(DocumentModel poll) throws ClientException {
        DocumentModel superSpace = documentManager.getSuperSpace(poll);
        String view = navigationContext.navigateToDocument(superSpace);
        webActions.setCurrentTabIds(SURVEYS_TAB);
        return view;
    }

    public boolean hasUnansweredOpenPolls() {
        return !pollService.getUnansweredOpenPolls(documentManager).isEmpty();
    }

    public boolean displayResults(DocumentModel pollDoc) {
        Poll poll = toPoll(pollDoc);
        if (poll.isInProject()) {
            return false;
        } else if (poll.isOpen()) {
            PollResult pollResult = pollService.getResultFor(poll);
            return pollResult.getResultsCount() != 0;
        }
        return true;
    }

}
