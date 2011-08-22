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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.survey.Constants.ANSWER_SURVEY_VERB;
import static org.nuxeo.ecm.survey.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_START_DATE_PROPERTY;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class })
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.survey.core" })
@LocalDeploy("org.nuxeo.ecm.survey.core:survey-test.xml")
public class TestSurveyService extends AbstractSurveyTest {

    @Test
    public void serviceRegistration() throws IOException {
        assertNotNull(surveyService);
    }

    @Test
    public void shouldCreateSurveysContainer() throws ClientException {
        DocumentModel ws = createWorkspace("ws");
        DocumentModel container = surveyService.getSurveysContainer(ws);
        assertNotNull(container);
        assertTrue(session.exists(container.getRef()));
    }

    @Test
    public void surveyShouldStoreQuestionAndAnswers() throws ClientException {
        DocumentModel ws = createWorkspace("ws");
        Survey survey = createOpenSurvey(ws, "survey", "Question 1", "Yes",
                "No");
        assertNotNull(survey);

        String question = survey.getQuestion();
        assertEquals("Question 1", question);
        String[] answers = survey.getAnswers();
        assertEquals(2, answers.length);
        assertEquals("Yes", answers[0]);
        assertEquals("No", answers[1]);
    }

    @Test
    public void shouldNotGetInProjectOrClosedSurveys() throws ClientException {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey1 = createSurvey(ws, "survey1", "Question 1", "Yes", "No");
        assertNotNull(survey1);
        Survey survey2 = createSurvey(ws, "survey2", "Question 2", "A", "B",
                "C");
        assertNotNull(survey2);

        List<Survey> surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(0, surveys.size());

        Survey survey3 = createOpenSurvey(ws, "survey3", "Question 3", "AAA",
                "BB", "C");
        assertNotNull(survey3);
        surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(1, surveys.size());

        surveyService.openSurvey(survey2);
        surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(2, surveys.size());

        surveyService.closeSurvey(survey3);
        surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(1, surveys.size());
    }

    @Test
    public void openingASurveyShouldSetItsStartDate() throws ClientException {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey = createSurvey(ws, "survey1", "Question 1", "Yes", "No");
        assertNotNull(survey);

        assertNull(survey.getStartDate());
        assertNull(survey.getEndDate());

        survey = surveyService.openSurvey(survey);
        assertNotNull(survey.getStartDate());
        assertNull(survey.getEndDate());
    }

    @Test
    public void closingASurveyShouldSetItsEndDate() throws ClientException {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey = createOpenSurvey(ws, "survey1", "Question 1", "Yes",
                "No");
        assertNotNull(survey);

        assertNotNull(survey.getStartDate());
        assertNull(survey.getEndDate());

        survey = surveyService.closeSurvey(survey);
        assertNotNull(survey.getStartDate());
        assertNotNull(survey.getEndDate());
    }

    @Test
    public void shouldGetAllOpenSurveys() throws ClientException {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey1 = createOpenSurvey(ws, "survey1", "Question 1", "Yes",
                "No");
        assertNotNull(survey1);
        Survey survey2 = createOpenSurvey(ws, "survey2", "Question 2", "A",
                "B", "C");
        assertNotNull(survey2);

        ws = createWorkspace("ws2");
        addRight(ws, SecurityConstants.READ, "bender");
        Survey survey3 = createOpenSurvey(ws, "survey3", "Question 3", "AAA",
                "BB", "C");
        assertNotNull(survey3);
        Survey survey4 = createOpenSurvey(ws, "survey4", "Question 4", "A",
                "BBB", "CCC");
        assertNotNull(survey4);

        List<Survey> surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(4, surveys.size());

        changeUser("bender");
        surveys = surveyService.getOpenSurveys(session);
        assertNotNull(surveys);
        assertEquals(2, surveys.size());

        changeUser("Administrator");
    }

    protected void addRight(DocumentModel doc, String right,
            String... usersOrGroups) throws ClientException {
        ACP acp = doc.getACP();
        ACL acl = acp.getOrCreateACL();
        for (String userOrGroup : usersOrGroups) {
            acl.add(new ACE(userOrGroup, right, true));
        }
        doc.setACP(acp, true);
        session.save();
    }

    @Test
    public void answeringASurveyShouldCreateANewActivity()
            throws ClientException {
        DocumentModel ws = createWorkspace("ws");
        Survey survey1 = createOpenSurvey(ws, "survey1", "Question 1", "Yes",
                "No");
        assertNotNull(survey1);

        surveyService.answer(session.getPrincipal().getName(), survey1, 1);

        session.save();
        eventService.waitForAsyncCompletion();

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        System.out.println(activities.toString());
        assertEquals(1, activities.size());
        Activity activity = activities.get(0);
        assertEquals(ANSWER_SURVEY_VERB, activity.getVerb());
        assertEquals(survey1.getId(), activity.getTarget());
        assertEquals("No", activity.getObject());
        assertEquals(ActivityHelper.createUserActivityObject(session.getPrincipal()), activity.getActor());

        assertTrue(surveyService.hasUserAnswered(
                session.getPrincipal().getName(), survey1));
    }

    @Test
    public void differentUsersCanAnswerASurvey() throws ClientException {
        DocumentModel ws = createWorkspace("ws");
        Survey survey1 = createOpenSurvey(ws, "survey1", "Question 1", "Yes",
                "No");
        assertNotNull(survey1);

        surveyService.answer(session.getPrincipal().getName(), survey1, 1);
        session.save();
        eventService.waitForAsyncCompletion();

        changeUser("bender");
        surveyService.answer(session.getPrincipal().getName(), survey1, 1);
        session.save();
        eventService.waitForAsyncCompletion();

        changeUser("fry");
        surveyService.answer(session.getPrincipal().getName(), survey1, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        SurveyResult surveyResult = surveyService.getResultFor(survey1);
        assertNotNull(surveyResult);
        assertEquals(survey1.getId(), surveyResult.getSurveyId());
        assertEquals(3, surveyResult.getResultsCount());
        Map<String, Long> resultsByAnswer = surveyResult.getResultsByAnswer();
        assertEquals(2, resultsByAnswer.size());
        assertTrue(2L == resultsByAnswer.get("No"));
        assertTrue(1L == resultsByAnswer.get("Yes"));

        changeUser("Administrator");
    }

    @Test
    public void shouldChangeStatusToOpenIfDateAfterStartDate()
            throws ClientException {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Survey survey1 = createSurvey(ws, "survey1", "Question 1", "Yes", "No");
        assertNotNull(survey1);
        assertTrue(survey1.isInProject());

        survey1.getSurveyDocument().setPropertyValue(
                SURVEY_START_DATE_PROPERTY, twoDaysBefore.toDate());
        survey1 = surveyService.updateSurveyStatus(survey1, now.toDate());
        assertTrue(survey1.isOpen());

    }

    @Test
    public void shouldChangeStatusToClosedIfDateAfterEndDate()
            throws ClientException {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Survey survey1 = createSurvey(ws, "survey1", "Question 1",
                twoDaysBefore.toDate(), null, "Yes", "No");
        assertNotNull(survey1);
        assertTrue(survey1.isOpen());

        survey1.getSurveyDocument().setPropertyValue(SURVEY_END_DATE_PROPERTY,
                twoDaysBefore.toDate());
        survey1 = surveyService.updateSurveyStatus(survey1, now.toDate());
        assertTrue(survey1.isClosed());
    }

    @Test
    public void shouldNotUpdateStatus() throws ClientException {
        DateTime now = new DateTime();
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Survey survey1 = createSurvey(ws, "survey1", "Question 1",
                twoDaysAfter.toDate(), null, "Yes", "No");
        assertNotNull(survey1);

        assertTrue(survey1.isInProject());

        survey1 = surveyService.updateSurveyStatus(survey1, now.toDate());
        assertTrue(survey1.isInProject());
        assertFalse(survey1.isOpen());
        assertFalse(survey1.isClosed());

        Survey survey2 = createSurvey(ws, "survey2", "Question 2", null,
                twoDaysAfter.toDate(), "Yes", "No");
        assertNotNull(survey2);
        assertTrue(survey2.isInProject());
        surveyService.openSurvey(survey2);
        assertFalse(survey2.isInProject());
        assertTrue(survey2.isOpen());

        survey2 = surveyService.updateSurveyStatus(survey2, now.toDate());
        assertTrue(survey2.isOpen());
        assertFalse(survey2.isInProject());
        assertFalse(survey2.isClosed());
    }

}
