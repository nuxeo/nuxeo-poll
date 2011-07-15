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

import static org.nuxeo.ecm.survey.Constants.ANSWER_SURVEY_VERB;
import static org.nuxeo.ecm.survey.Constants.SURVEY_DOCUMENT_TYPE;
import static org.nuxeo.ecm.survey.Constants.SURVEY_PUBLISHED_STATE;
import static org.nuxeo.ecm.survey.SurveyActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.survey.SurveyActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.survey.SurveyActivityStreamFilter.QueryType.ACTOR_ANSWERS_FOR_SURVEY;
import static org.nuxeo.ecm.survey.SurveyActivityStreamFilter.QueryType.ALL_ANSWERS_FOR_SURVEY;
import static org.nuxeo.ecm.survey.SurveyActivityStreamFilter.SURVEY_ID_PARAMETER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityEventContext;
import org.nuxeo.ecm.activity.ActivityImpl;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@link SurveyService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SurveyServiceImpl implements SurveyService {

    public static final String SURVEYS_CONTAINER_NAME = "surveysContainer";

    protected ActivityStreamService activityStreamService;

    protected EventService eventService;

    @Override
    public DocumentModel getSurveysContainer(DocumentModel doc) {
        try {
            Path surveysContainerPath = doc.getPath().append(
                    SURVEYS_CONTAINER_NAME);
            PathRef surveysContainerRef = new PathRef(
                    surveysContainerPath.toString());
            CoreSession session = doc.getCoreSession();
            if (session.exists(surveysContainerRef)) {
                return session.getDocument(surveysContainerRef);
            } else {
                return createSurveysContainer(session, doc);
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected DocumentModel createSurveysContainer(CoreSession session,
            DocumentModel doc) {
        try {
            DocumentModel surveysContainer = session.createDocumentModel(
                    doc.getPathAsString(), SURVEYS_CONTAINER_NAME,
                    "HiddenFolder");
            surveysContainer.setPropertyValue("dc:title", "Surveys");
            surveysContainer = session.createDocument(surveysContainer);
            session.save();
            return surveysContainer;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public List<Survey> getPublishedSurveys(CoreSession session) {
        try {
            String query = "SELECT * FROM Document WHERE ecm:primaryType = '%s'"
                    + " AND ecm:currentLifeCycleState = '%s'";
            List<DocumentModel> docs = session.query(String.format(query,
                    SURVEY_DOCUMENT_TYPE, SURVEY_PUBLISHED_STATE));

            List<Survey> surveys = new ArrayList<Survey>();
            for (DocumentModel doc : docs) {
                surveys.add(doc.getAdapter(Survey.class));
            }
            return surveys;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public boolean hasUserAnswered(String username, Survey survey) {
        ActivityStreamService activityStreamService = getActivityStreamService();

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ACTOR_ANSWERS_FOR_SURVEY);
        parameters.put(SURVEY_ID_PARAMETER, survey.getId());
        parameters.put(ACTOR_PARAMETER, username);
        List<Activity> activities = activityStreamService.query(
                SurveyActivityStreamFilter.ID, parameters);
        return !activities.isEmpty();
    }

    @Override
    public void answer(String username, Survey survey, int answerIndex) {
        Activity activity = new ActivityImpl();
        activity.setVerb(ANSWER_SURVEY_VERB);
        activity.setActor(username);
        activity.setTarget(survey.getId());
        activity.setObject(getAnswer(survey, answerIndex));
        activity.setPublishedDate(new Date());

        DocumentModel surveyDoc = survey.getSurveyDocumentModel();
        CoreSession session = surveyDoc.getCoreSession();
        EventContext activityEventContext = new ActivityEventContext(session,
                session.getPrincipal(), activity);
        Event event = new EventImpl("surveyAnswered", activityEventContext);
        fireEvent(event);
    }

    private String getAnswer(Survey survey, int answerIndex) {
        String[] answers = survey.getAnswers();
        return answers.length > answerIndex ? answers[answerIndex] : null;
    }

    private void fireEvent(Event event) {
        try {
            getEventService().fireEvent(event);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public SurveyResult getResultFor(Survey survey) {
        ActivityStreamService activityStreamService = getActivityStreamService();

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ALL_ANSWERS_FOR_SURVEY);
        parameters.put("surveyId", survey.getId());
        List<Activity> activities = activityStreamService.query(
                SurveyActivityStreamFilter.ID, parameters);

        Map<String, Long> resultsByAnswer = new LinkedHashMap<String, Long>();
        String[] answers = survey.getAnswers();
        for (String answer : answers) {
            resultsByAnswer.put(answer, 0L);
        }

        for (Activity activity : activities) {
            Long result = resultsByAnswer.get(activity.getObject());
            if (result == null) {
                result = 0L;
            }
            result += 1;
            resultsByAnswer.put(activity.getObject(), result);
        }

        return new SurveyResult(survey.getId(), activities.size(),
                resultsByAnswer);
    }

    protected ActivityStreamService getActivityStreamService()
            throws ClientRuntimeException {
        if (activityStreamService == null) {
            try {
                activityStreamService = Framework.getService(ActivityStreamService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to ActivityStreamService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (activityStreamService == null) {
                throw new ClientRuntimeException(
                        "ActivityStreamService service not bound");
            }
        }
        return activityStreamService;
    }

    protected EventService getEventService() throws ClientRuntimeException {
        if (eventService == null) {
            try {
                eventService = Framework.getService(EventService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to EventService. "
                        + e.getMessage();
                throw new ClientRuntimeException(errMsg, e);
            }
            if (eventService == null) {
                throw new ClientRuntimeException(
                        "EventService service not bound");
            }
        }
        return eventService;
    }

}
