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

package org.nuxeo.ecm.survey;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import static org.nuxeo.ecm.survey.Constants.SURVEY_DOCUMENT_TYPE;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

/**
 * Handles Survey related web actions.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Name("surveyActions")
@Scope(CONVERSATION)
@Install(precedence = FRAMEWORK)
public class SurveyActions implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SURVEYS_TAB = ":TAB_SURVEYS";

    protected static final Log log = LogFactory.getLog(SurveyActions.class);

    @In(create = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient DocumentActions documentActions;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient WebActions webActions;

    @In(create = true)
    protected transient SurveyService surveyService;

    protected boolean showCreateForm = false;

    protected DocumentModel newSurvey;

    public DocumentModel getSurveysContainer() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return getSurveysContainer(currentDocument);
    }

    public DocumentModel getSurveysContainer(DocumentModel doc)
            throws ClientException {
        return surveyService.getSurveysContainer(doc);
    }

    public boolean isShowCreateForm() {
        return showCreateForm;
    }

    public void toggleCreateForm() {
        showCreateForm = !showCreateForm;
    }

    public void toggleAndReset() {
        toggleCreateForm();
        resetNewSurvey();
    }

    protected void resetNewSurvey() {
        newSurvey = null;
    }

    public DocumentModel getNewSurvey() throws ClientException {
        if (newSurvey == null) {
            newSurvey = documentManager.createDocumentModel(SURVEY_DOCUMENT_TYPE);
        }
        return newSurvey;
    }

    public void createSurvey() throws ClientException {
        documentActions.saveDocument(newSurvey);
        resetNewSurvey();
        toggleCreateForm();
    }

    public Survey toSurvey(DocumentModel survey) {
        return SurveyHelper.toSurvey(survey);
    }

    public void openSurvey(DocumentModel survey) throws ClientException {
        surveyService.openSurvey(toSurvey(survey));
    }

    public void closeSurvey(DocumentModel survey) throws ClientException {
        surveyService.closeSurvey(toSurvey(survey));
    }

    public String backToSurveysListing(DocumentModel survey)
            throws ClientException {
        DocumentModel superSpace = documentManager.getSuperSpace(survey);
        String view = navigationContext.navigateToDocument(superSpace);
        webActions.setCurrentTabIds(SURVEYS_TAB);
        return view;
    }

    public boolean hasUnansweredOpenSurveys() {
        return !surveyService.getUnansweredOpenSurveys(documentManager).isEmpty();
    }

    public boolean displayResults(DocumentModel surveyDoc) {
        Survey survey = toSurvey(surveyDoc);
        if (survey.isInProject()) {
            return false;
        } else if (survey.isOpen()) {
            SurveyResult surveyResult = surveyService.getResultFor(survey);
            return surveyResult.getResultsCount() != 0;
        }
        return true;
    }

}
