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
    public void shouldReturnNoAvailableSurvey() throws Exception {
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
        Survey survey1 = createSurvey(ws, "survey1", "Question 1", "Yes", "No");
        assertNotNull(survey1);
        Survey survey2 = createSurvey(ws, "survey2", "Question 2", "A", "B",
                "C");
        assertNotNull(survey2);

        ws = createWorkspace("ws2");
        Survey survey3 = createSurvey(ws, "survey3", "Question 3", "AAA", "BB",
                "C");
        assertNotNull(survey3);

        // answer third survey
        surveyService.answer(session.getPrincipal().getName(), survey3, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testSurveyOperation");
        chain.add(GetPublishedSurveys.ID).set("onlyUnansweredSurveys",
                true);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray surveys = object.getJSONArray("surveys");
        assertEquals(2, surveys.size());
    }

}
