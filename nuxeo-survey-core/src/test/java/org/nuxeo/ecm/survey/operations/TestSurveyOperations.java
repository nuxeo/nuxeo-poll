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

package org.nuxeo.ecm.survey.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.survey.AbstractSurveyTest;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.ecm.survey.SurveyService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.survey.core" })
@LocalDeploy("org.nuxeo.ecm.survey.core:survey-test.xml")
public class TestSurveyOperations extends AbstractSurveyTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService service;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected SurveyService surveyService;

    @Inject
    protected EventService eventService;

    @Test
    public void shouldReturnNoPublishedSurvey() throws Exception {
        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(GetPublishedSurveys.ID);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray surveys = object.getJSONArray("surveys");
        assertTrue(surveys.isEmpty());
    }

    @Test
    public void shouldReturnOnlyUnansweredSurveys() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey1 = createPublishedSurvey(ws, "survey1", "Question 1",
                "Yes", "No");
        assertNotNull(survey1);
        Survey survey2 = createPublishedSurvey(ws, "survey2", "Question 2",
                "A", "B", "C");
        assertNotNull(survey2);

        ws = createWorkspace("ws2");
        Survey survey3 = createPublishedSurvey(ws, "survey3", "Question 3",
                "AAA", "BB", "C");
        assertNotNull(survey3);

        // answer third survey
        surveyService.answer(session.getPrincipal().getName(), survey3, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(GetPublishedSurveys.ID).set("onlyUnansweredSurveys", true);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray surveys = object.getJSONArray("surveys");
        assertEquals(2, surveys.size());
    }

    @Test
    public void shouldAnswerSurvey() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey = createPublishedSurvey(ws, "survey", "Question 1",
                "Yes", "No");
        assertNotNull(survey);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(AnswerSurvey.ID).set("surveyId", survey.getId()).set(
                "answerIndex", 1);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(survey.getId(), object.get("surveyId"));
        assertEquals(true, object.get("answered"));
        JSONObject surveyResult = (JSONObject) object.get("result");
        assertEquals(1, surveyResult.get("resultsCount"));

        JSONArray resultsByAnswer = surveyResult.getJSONArray("resultsByAnswer");
        assertEquals(2, resultsByAnswer.size());
        JSONObject answer = (JSONObject) resultsByAnswer.get(0);
        assertEquals(survey.getAnswers()[0], answer.get("answer"));
        assertEquals(0, answer.get("result"));
        answer = (JSONObject) resultsByAnswer.get(1);
        assertEquals(survey.getAnswers()[1], answer.get("answer"));
        assertEquals(1, answer.get("result"));
    }

    @Test
    public void shouldGetSurveyWithoutResult() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey = createPublishedSurvey(ws, "survey", "Question 1",
                "Yes", "No");
        assertNotNull(survey);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(GetSurvey.ID).set("surveyId", survey.getId());
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(survey.getId(), object.get("surveyId"));
        assertEquals(false, object.get("answered"));
        assertEquals(survey.getQuestion(), object.get("question"));
        JSONArray answers = object.getJSONArray("answers");
        assertEquals(2, answers.size());
        assertNull(object.get("result"));
    }

    @Test
    public void shouldGetSurveyWithResult() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Survey survey = createPublishedSurvey(ws, "survey", "Question 1",
                "Yes", "No");
        assertNotNull(survey);

        surveyService.answer(session.getPrincipal().getName(), survey, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(GetSurvey.ID).set("surveyId", survey.getId()).set(
                "withResult", true);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(survey.getId(), object.get("surveyId"));
        assertEquals(true, object.get("answered"));
        assertEquals(survey.getQuestion(), object.get("question"));
        JSONArray answers = object.getJSONArray("answers");
        assertEquals(2, answers.size());

        JSONObject surveyResult = (JSONObject) object.get("result");
        assertNotNull(surveyResult);
        assertEquals(1, surveyResult.get("resultsCount"));
        JSONArray resultsByAnswer = surveyResult.getJSONArray("resultsByAnswer");
        assertEquals(2, resultsByAnswer.size());
        JSONObject answer = (JSONObject) resultsByAnswer.get(0);
        assertEquals(survey.getAnswers()[0], answer.get("answer"));
        assertEquals(1, answer.get("result"));
        answer = (JSONObject) resultsByAnswer.get(1);
        assertEquals(survey.getAnswers()[1], answer.get("answer"));
        assertEquals(0, answer.get("result"));
    }

}
