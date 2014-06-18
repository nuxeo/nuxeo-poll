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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.poll.Constants.ANSWER_SURVEY_VERB;
import static org.nuxeo.ecm.poll.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.poll.Constants.SURVEY_START_DATE_PROPERTY;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class TestPollService extends AbstractPollTest {

    @Test
    public void serviceRegistration() throws IOException {
        assertNotNull(pollService);
    }

    @Test
    public void shouldCreatePollsContainer() throws ClientException {
        DocumentModel ws = createWorkspace("ws");
        DocumentModel container = pollService.getPollsContainer(ws);
        assertNotNull(container);
        assertTrue(session.exists(container.getRef()));
    }

    @Test
    public void pollShouldStoreQuestionAndAnswers() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws");
        Poll poll = createOpenPoll(ws, "poll", "Question 1", "Yes",
                "No");
        assertNotNull(poll);

        String question = poll.getQuestion();
        assertEquals("Question 1", question);
        String[] answers = poll.getAnswers();
        assertEquals(2, answers.length);
        assertEquals("Yes", answers[0]);
        assertEquals("No", answers[1]);
    }

    @Test
    public void shouldNotGetInProjectOrClosedPolls() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll1 = createPoll(ws, "poll1", "Question 1", "Yes", "No");
        assertNotNull(poll1);
        Poll poll2 = createPoll(ws, "poll2", "Question 2", "A", "B",
                "C");
        assertNotNull(poll2);

        List<Poll> polls = pollService.getOpenPolls(session);
        assertNotNull(polls);
        assertEquals(0, polls.size());

        Poll poll3 = createOpenPoll(ws, "poll3", "Question 3", "AAA",
                "BB", "C");
        assertNotNull(poll3);
        polls = pollService.getOpenPolls(session);
        assertNotNull(polls);
        assertEquals(1, polls.size());

        pollService.openPoll(poll2);
        polls = pollService.getOpenPolls(session);
        assertNotNull(polls);
        assertEquals(2, polls.size());

        pollService.closePoll(poll3);
        polls = pollService.getOpenPolls(session);
        assertNotNull(polls);
        assertEquals(1, polls.size());
    }

    @Test
    public void openingAPollShouldSetItsStartDate() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll = createPoll(ws, "poll1", "Question 1", "Yes", "No");
        assertNotNull(poll);

        assertNull(poll.getStartDate());
        assertNull(poll.getEndDate());

        poll = pollService.openPoll(poll);
        assertNotNull(poll.getStartDate());
        assertNull(poll.getEndDate());
    }

    @Test
    public void closingAPollShouldSetItsEndDate() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll = createOpenPoll(ws, "poll1", "Question 1", "Yes",
                "No");
        assertNotNull(poll);

        assertNotNull(poll.getStartDate());
        assertNull(poll.getEndDate());

        poll = pollService.closePoll(poll);
        assertNotNull(poll.getStartDate());
        assertNotNull(poll.getEndDate());
    }

    @Test
    public void shouldGetAllOpenPolls() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws1");
        Poll poll1 = createOpenPoll(ws, "poll1", "Question 1", "Yes",
                "No");
        assertNotNull(poll1);
        Poll poll2 = createOpenPoll(ws, "poll2", "Question 2", "A",
                "B", "C");
        assertNotNull(poll2);

        ws = createWorkspace("ws2");
        addRight(ws, SecurityConstants.READ, "bender");
        Poll poll3 = createOpenPoll(ws, "poll3", "Question 3", "AAA",
                "BB", "C");
        assertNotNull(poll3);
        Poll poll4 = createOpenPoll(ws, "poll4", "Question 4", "A",
                "BBB", "CCC");
        assertNotNull(poll4);

        List<Poll> polls = pollService.getOpenPolls(session);
        assertNotNull(polls);
        assertEquals(4, polls.size());

        try (CoreSession newSession = openSessionAs("bender")) {
            polls = pollService.getOpenPolls(newSession);
            assertNotNull(polls);
            assertEquals(2, polls.size());
        }
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
    public void answeringAPollShouldCreateANewActivity()
            throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws");
        Poll poll1 = createOpenPoll(ws, "poll1", "Question 1", "Yes",
                "No");
        assertNotNull(poll1);

        pollService.answer(session.getPrincipal().getName(), poll1, 1);

        session.save();
        eventService.waitForAsyncCompletion();

        List<Activity> activities = activityStreamService.query(
                ActivityStreamService.ALL_ACTIVITIES, null);
        assertNotNull(activities);
        System.out.println(activities.toString());
        assertEquals(1, activities.size());
        Activity activity = activities.get(0);
        assertEquals(ANSWER_SURVEY_VERB, activity.getVerb());
        assertEquals(poll1.getId(), activity.getTarget());
        assertEquals("No", activity.getObject());
        assertEquals(ActivityHelper.createUserActivityObject(session.getPrincipal()), activity.getActor());

        assertTrue(pollService.hasUserAnswered(
                session.getPrincipal().getName(), poll1));
    }

    @Test
    public void differentUsersCanAnswerAPoll() throws ClientException, InterruptedException {
        DocumentModel ws = createWorkspace("ws");
        Poll poll1 = createOpenPoll(ws, "poll1", "Question 1", "Yes",
                "No");
        assertNotNull(poll1);

        pollService.answer(session.getPrincipal().getName(), poll1, 1);
        session.save();
        eventService.waitForAsyncCompletion();

        try (CoreSession newSession = openSessionAs("bender")) {
            pollService.answer(newSession.getPrincipal().getName(), poll1, 1);
            newSession.save();
            eventService.waitForAsyncCompletion();
        }

        try (CoreSession newSession = openSessionAs("fry")) {
            pollService.answer(newSession.getPrincipal().getName(), poll1, 0);
            newSession.save();
            eventService.waitForAsyncCompletion();
        }

        PollResult pollResult = pollService.getResultFor(poll1);
        assertNotNull(pollResult);
        assertEquals(poll1.getId(), pollResult.getPollId());
        assertEquals(3, pollResult.getResultsCount());
        Map<String, Long> resultsByAnswer = pollResult.getResultsByAnswer();
        assertEquals(2, resultsByAnswer.size());
        assertTrue(2L == resultsByAnswer.get("No"));
        assertTrue(1L == resultsByAnswer.get("Yes"));
    }

    @Test
    public void shouldChangeStatusToOpenIfDateAfterStartDate()
            throws ClientException, InterruptedException {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Poll poll1 = createPoll(ws, "poll1", "Question 1", "Yes", "No");
        assertNotNull(poll1);
        assertTrue(poll1.isInProject());

        poll1.getPollDocument().setPropertyValue(
                SURVEY_START_DATE_PROPERTY, twoDaysBefore.toDate());
        poll1 = pollService.updatePollStatus(poll1, now.toDate());
        assertTrue(poll1.isOpen());

    }

    @Test
    public void shouldChangeStatusToClosedIfDateAfterEndDate()
            throws ClientException, InterruptedException {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Poll poll1 = createPoll(ws, "poll1", "Question 1",
                twoDaysBefore.toDate(), null, "Yes", "No");
        assertNotNull(poll1);
        assertTrue(poll1.isOpen());

        poll1.getPollDocument().setPropertyValue(SURVEY_END_DATE_PROPERTY,
                twoDaysBefore.toDate());
        poll1 = pollService.updatePollStatus(poll1, now.toDate());
        assertTrue(poll1.isClosed());
    }

    @Test
    public void shouldNotUpdateStatus() throws ClientException, InterruptedException {
        DateTime now = new DateTime();
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Poll poll1 = createPoll(ws, "poll1", "Question 1",
                twoDaysAfter.toDate(), null, "Yes", "No");
        assertNotNull(poll1);

        assertTrue(poll1.isInProject());

        poll1 = pollService.updatePollStatus(poll1, now.toDate());
        assertTrue(poll1.isInProject());
        assertFalse(poll1.isOpen());
        assertFalse(poll1.isClosed());

        Poll poll2 = createPoll(ws, "poll2", "Question 2", null,
                twoDaysAfter.toDate(), "Yes", "No");
        assertNotNull(poll2);
        assertTrue(poll2.isInProject());
        pollService.openPoll(poll2);
        assertFalse(poll2.isInProject());
        assertTrue(poll2.isOpen());

        poll2 = pollService.updatePollStatus(poll2, now.toDate());
        assertTrue(poll2.isOpen());
        assertFalse(poll2.isInProject());
        assertFalse(poll2.isClosed());
    }

}
