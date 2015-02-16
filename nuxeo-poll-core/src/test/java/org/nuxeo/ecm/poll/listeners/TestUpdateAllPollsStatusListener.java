package org.nuxeo.ecm.poll.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.poll.PollHelper.toPoll;
import static org.nuxeo.ecm.poll.listeners.UpdateAllPollsStatusListener.UPDATE_SURVEYS_STATUS_EVENT;

import java.util.Date;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.poll.AbstractPollTest;
import org.nuxeo.ecm.poll.Poll;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class TestUpdateAllPollsStatusListener extends AbstractPollTest {

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    @Before
    public void disableNotNeededListener() {
        eventServiceAdmin.setListenerEnabledFlag("updatePollStatusListener", false);
    }

    @Test
    public void shouldUpdateStatusToOpenThroughListener() throws Exception {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Poll pollToBeOpen = createPoll(ws, "pollToBeOpen", "Question 1", twoDaysBefore.toDate(), null, "Yes", "No");
        assertNotNull(pollToBeOpen);
        assertTrue(pollToBeOpen.isInProject());

        Poll pollToStayInProject = createPoll(ws, "pollToStayInProject", "Question 2", twoDaysAfter.toDate(), null,
                "A", "B", "C");
        assertNotNull(pollToStayInProject);
        assertTrue(pollToStayInProject.isInProject());

        EventService eventService = Framework.getService(EventService.class);
        EventContext context = new EventContextImpl(session, session.getPrincipal());
        eventService.fireEvent(UPDATE_SURVEYS_STATUS_EVENT, context);
        session.save();
        eventService.waitForAsyncCompletion();

        DocumentModel pollToBeOpenDoc = session.getDocument(pollToBeOpen.getPollDocument().getRef());
        pollToBeOpen = toPoll(pollToBeOpenDoc);
        assertTrue(pollToBeOpen.isOpen());

        DocumentModel pollToStayInProjectDoc = session.getDocument(pollToStayInProject.getPollDocument().getRef());
        pollToStayInProject = toPoll(pollToStayInProjectDoc);
        assertFalse(pollToStayInProject.isOpen());
        assertTrue(pollToStayInProject.isInProject());
    }

    @Test
    public void shouldUpdateStatusToClosedThroughListener() throws Exception {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Poll pollToBeClosed = createPoll(ws, "pollToBeClosed", "Question 1", twoDaysBefore.toDate(),
                twoDaysBefore.toDate(), "Yes", "No");
        pollToBeClosed = pollService.updatePollStatus(pollToBeClosed, new Date());
        assertNotNull(pollToBeClosed);
        assertTrue(pollToBeClosed.isOpen());

        Poll pollToStayOpen = createPoll(ws, "pollToStayOpen", "Question 1", twoDaysBefore.toDate(),
                twoDaysAfter.toDate(), "Yes", "No");
        pollToStayOpen = pollService.updatePollStatus(pollToStayOpen, new Date());
        assertNotNull(pollToStayOpen);
        assertTrue(pollToStayOpen.isOpen());

        Poll pollToStayInProject = createPoll(ws, "pollToStayInProject", "Question 1", twoDaysAfter.toDate(), null,
                "Yes", "No");
        assertNotNull(pollToStayInProject);
        assertTrue(pollToStayInProject.isInProject());

        EventService eventService = Framework.getService(EventService.class);
        EventContext context = new EventContextImpl(session, session.getPrincipal());
        eventService.fireEvent(UPDATE_SURVEYS_STATUS_EVENT, context);
        session.save();
        eventService.waitForAsyncCompletion();

        DocumentModel pollToBeClosedDoc = session.getDocument(pollToBeClosed.getPollDocument().getRef());
        pollToBeClosed = toPoll(pollToBeClosedDoc);
        assertTrue(pollToBeClosed.isClosed());
        assertFalse(pollToBeClosed.isOpen());
        assertFalse(pollToBeClosed.isInProject());

        DocumentModel pollToStayOpenDoc = session.getDocument(pollToStayOpen.getPollDocument().getRef());
        pollToStayOpen = toPoll(pollToStayOpenDoc);
        assertTrue(pollToStayOpen.isOpen());
        assertFalse(pollToStayOpen.isInProject());
        assertFalse(pollToStayOpen.isClosed());

        DocumentModel pollToStayInProjectDoc = session.getDocument(pollToStayInProject.getPollDocument().getRef());
        pollToStayInProject = toPoll(pollToStayInProjectDoc);
        assertTrue(pollToStayInProject.isInProject());
        assertFalse(pollToStayInProject.isOpen());
        assertFalse(pollToStayInProject.isClosed());
    }

}
