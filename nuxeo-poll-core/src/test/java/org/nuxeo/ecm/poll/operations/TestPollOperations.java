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

package org.nuxeo.ecm.poll.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.poll.AbstractPollTest;
import org.nuxeo.ecm.poll.Poll;
import org.nuxeo.ecm.poll.PollService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Deploy({ "org.nuxeo.ecm.automation.core" })
public class TestPollOperations extends AbstractPollTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService service;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected PollService pollService;

    @Inject
    protected EventService eventService;

    @Test
    public void shouldReturnNoOpenPoll() throws Exception {
        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testPollOperation");
        chain.add(GetOpenPolls.ID);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray polls = object.getJSONArray("polls");
        assertTrue(polls.isEmpty());
    }

    @Test
    public void shouldReturnOnlyUnansweredPolls() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll1 = createOpenPoll(ws, "poll1", "Question 1", "Yes", "No");
        assertNotNull(poll1);
        Poll poll2 = createOpenPoll(ws, "poll2", "Question 2", "A", "B", "C");
        assertNotNull(poll2);

        ws = createWorkspace("ws2");
        Poll poll3 = createOpenPoll(ws, "poll3", "Question 3", "AAA", "BB", "C");
        assertNotNull(poll3);

        // answer third poll
        pollService.answer(session.getPrincipal().getName(), poll3, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testPollOperation");
        chain.add(GetOpenPolls.ID).set("onlyUnansweredPolls", true);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        JSONArray polls = object.getJSONArray("polls");
        assertEquals(2, polls.size());
    }

    @Test
    public void shouldAnswerPoll() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll = createOpenPoll(ws, "poll", "Question 1", "Yes", "No");
        assertNotNull(poll);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testPollOperation");
        chain.add(AnswerPoll.ID).set("pollId", poll.getId()).set("answerIndex", 1);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(poll.getId(), object.get("pollId"));
        assertEquals(true, object.get("answered"));
        JSONObject pollResult = (JSONObject) object.get("result");
        assertEquals(1, pollResult.get("resultsCount"));

        JSONArray resultsByAnswer = pollResult.getJSONArray("resultsByAnswer");
        assertEquals(2, resultsByAnswer.size());
        JSONObject answer = (JSONObject) resultsByAnswer.get(0);
        assertEquals(poll.getAnswers()[0], answer.get("answer"));
        assertEquals(0, answer.get("result"));
        answer = (JSONObject) resultsByAnswer.get(1);
        assertEquals(poll.getAnswers()[1], answer.get("answer"));
        assertEquals(1, answer.get("result"));
    }

    @Test
    public void shouldGetPollWithoutResult() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll = createOpenPoll(ws, "poll", "Question 1", "Yes", "No");
        assertNotNull(poll);

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testPollOperation");
        chain.add(GetPoll.ID).set("pollId", poll.getId());
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(poll.getId(), object.get("pollId"));
        assertEquals(false, object.get("answered"));
        assertEquals(poll.getQuestion(), object.get("question"));
        JSONArray answers = object.getJSONArray("answers");
        assertEquals(2, answers.size());
        assertNull(object.get("result"));
    }

    @Test
    public void shouldGetPollWithResult() throws Exception {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll = createOpenPoll(ws, "poll", "Question 1", "Yes", "No");
        assertNotNull(poll);

        pollService.answer(session.getPrincipal().getName(), poll, 0);
        session.save();
        eventService.waitForAsyncCompletion();

        OperationContext ctx = new OperationContext(session);
        assertNotNull(ctx);

        OperationChain chain = new OperationChain("testPollOperation");
        chain.add(GetPoll.ID).set("pollId", poll.getId()).set("withResult", true);
        Blob result = (Blob) service.run(ctx, chain);
        assertNotNull(result);
        String json = result.getString();
        assertNotNull(json);

        JSONObject object = JSONObject.fromObject(json);
        assertEquals(poll.getId(), object.get("pollId"));
        assertEquals(true, object.get("answered"));
        assertEquals(poll.getQuestion(), object.get("question"));
        JSONArray answers = object.getJSONArray("answers");
        assertEquals(2, answers.size());

        JSONObject pollResult = (JSONObject) object.get("result");
        assertNotNull(pollResult);
        assertEquals(1, pollResult.get("resultsCount"));
        JSONArray resultsByAnswer = pollResult.getJSONArray("resultsByAnswer");
        assertEquals(2, resultsByAnswer.size());
        JSONObject answer = (JSONObject) resultsByAnswer.get(0);
        assertEquals(poll.getAnswers()[0], answer.get("answer"));
        assertEquals(1, answer.get("result"));
        answer = (JSONObject) resultsByAnswer.get(1);
        assertEquals(poll.getAnswers()[1], answer.get("answer"));
        assertEquals(0, answer.get("result"));
    }

}
