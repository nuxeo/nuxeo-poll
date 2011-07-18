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

package org.nuxeo.ecm.survey.listeners;

import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETED_STATE;
import static org.nuxeo.ecm.survey.Constants.SURVEY_CLOSED_STATE;
import static org.nuxeo.ecm.survey.SurveyHelper.toSurvey;

import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.ecm.survey.SurveyService;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener updating the status of a Survey.
 * <p/>
 * {@code project} -> {@code published} -> {@code closed}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class UpdateSurveyStatusListener implements EventListener {

    public static final String UPDATE_SURVEYS_STATUS_EVENT = "updateSurveysStatus";

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!UPDATE_SURVEYS_STATUS_EVENT.equals(event.getName())) {
            return;
        }

        try {
            RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);
            Repository repository = repositoryManager.getDefaultRepository();
            UpdateSurveysStatus updateSurveysStatus = new UpdateSurveysStatus(
                    repository.getName());
            updateSurveysStatus.runUnrestricted();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    /**
     * {@code UnrestrictedSessionRunner} updating the status of all Surveys.
     */
    public static class UpdateSurveysStatus extends UnrestrictedSessionRunner {

        public UpdateSurveysStatus(String repositoryName) {
            super(repositoryName);
        }

        @Override
        public void run() throws ClientException {
            String query = "SELECT * FROM Survey WHERE ecm:currentLifeCycleState <> '%s' AND ecm:currentLifeCycleState <> '%s'";
            List<DocumentModel> surveys = session.query(String.format(query,
                    SURVEY_CLOSED_STATE, DELETED_STATE));
            SurveyService surveyService = getSurveyService();
            Date now = new Date();
            for (DocumentModel survey : surveys) {
                updateStatus(surveyService, toSurvey(survey), now);
            }
            session.save();
        }

        protected void updateStatus(SurveyService surveyService, Survey survey,
                Date now) throws ClientException {
            surveyService.updateSurveyStatus(survey, now);
        }

        protected SurveyService getSurveyService() throws ClientException {
            try {
                return Framework.getService(SurveyService.class);
            } catch (Exception e) {
                throw new ClientException(e);
            }
        }

    }

}
