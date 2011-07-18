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

import static org.nuxeo.ecm.survey.Constants.PUBLISH_SURVEY_TRANSITION;
import static org.nuxeo.ecm.survey.Constants.SURVEY_ANSWERS_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_BEGIN_DATE_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_DOCUMENT_TYPE;
import static org.nuxeo.ecm.survey.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_QUESTION_PROPERTY;
import static org.nuxeo.ecm.survey.SurveyHelper.toSurvey;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public abstract class AbstractSurveyTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected SurveyService surveyService;

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    @Before
    public void cleanupDatabase() throws ClientException {
        ((ActivityStreamServiceImpl) activityStreamService).getOrCreatePersistenceProvider().run(
                true, new PersistenceProvider.RunVoid() {
                    public void runWith(EntityManager em) {
                        Query query = em.createQuery("delete from Activity");
                        query.executeUpdate();
                    }
                });
    }

    protected DocumentModel createWorkspace(String name) throws ClientException {
        DocumentModel doc = session.createDocumentModel(
                "/default-domain/workspaces", name, "Workspace");
        doc.setProperty("dublincore", "title", name);
        doc = session.createDocument(doc);
        session.saveDocument(doc);
        session.save();
        return doc;
    }

    protected Survey createSurvey(DocumentModel superSpace, String name,
            String question, String... answers) throws ClientException {
        return createSurvey(superSpace, name, question, null, null, answers);
    }

    protected Survey createSurvey(DocumentModel superSpace, String name,
            String question, Date beginDate, Date endDate, String... answers)
            throws ClientException {
        DocumentModel survey = session.createDocumentModel(
                surveyService.getSurveysContainer(superSpace).getPathAsString(),
                name, SURVEY_DOCUMENT_TYPE);
        survey.setPropertyValue(SURVEY_QUESTION_PROPERTY, question);
        survey.setPropertyValue(SURVEY_ANSWERS_PROPERTY, answers);
        survey.setPropertyValue(SURVEY_BEGIN_DATE_PROPERTY, beginDate);
        survey.setPropertyValue(SURVEY_END_DATE_PROPERTY, endDate);
        survey = session.createDocument(survey);
        session.save();
        return toSurvey(survey);
    }

    protected Survey createPublishedSurvey(DocumentModel superSpace,
            String name, String question, String... answers)
            throws ClientException {
        return createPublishedSurvey(superSpace, name, question, null, null,
                answers);
    }

    protected Survey createPublishedSurvey(DocumentModel superSpace,
            String name, String question, Date beginDate, Date endDate,
            String... answers) throws ClientException {
        Survey survey = createSurvey(superSpace, name, question, beginDate,
                endDate, answers);
        survey.getSurveyDocument().followTransition(PUBLISH_SURVEY_TRANSITION);
        session.save();
        return survey;
    }

    protected void changeUser(String username) {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchUser(
                username);
    }

}
