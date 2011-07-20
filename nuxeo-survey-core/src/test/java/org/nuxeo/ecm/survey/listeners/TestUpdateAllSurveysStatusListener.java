package org.nuxeo.ecm.survey.listeners;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.survey.SurveyHelper.toSurvey;
import static org.nuxeo.ecm.survey.listeners.UpdateAllSurveysStatusListener.UPDATE_SURVEYS_STATUS_EVENT;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.survey.AbstractSurveyTest;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.runtime.api.Framework;
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
@Features({ CoreFeature.class })
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = DefaultRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.core.persistence", "org.nuxeo.ecm.activity",
        "org.nuxeo.ecm.survey.core" })
@LocalDeploy("org.nuxeo.ecm.survey.core:survey-test.xml")
public class TestUpdateAllSurveysStatusListener extends AbstractSurveyTest {

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    @Before
    public void disableNotNeededListener() {
        eventServiceAdmin.setListenerEnabledFlag("updateSurveyStatusListener",
                false);
    }

    @Test
    public void shouldUpdateStatusToPublishedThroughListener() throws Exception {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Survey surveyToBePublished = createSurvey(ws, "surveyToBePublished",
                "Question 1", twoDaysBefore.toDate(), null, "Yes", "No");
        assertNotNull(surveyToBePublished);
        assertTrue(surveyToBePublished.isInProject());

        Survey surveyToStayInProject = createSurvey(ws,
                "surveyToStayInProject", "Question 2", twoDaysAfter.toDate(),
                null, "A", "B", "C");
        assertNotNull(surveyToStayInProject);
        assertTrue(surveyToStayInProject.isInProject());

        EventService eventService = Framework.getService(EventService.class);
        EventContext context = new EventContextImpl(session,
                session.getPrincipal());
        eventService.fireEvent(UPDATE_SURVEYS_STATUS_EVENT, context);
        session.save();
        eventService.waitForAsyncCompletion();

        DocumentModel surveyToBePublishedDoc = session.getDocument(surveyToBePublished.getSurveyDocument().getRef());
        surveyToBePublished = toSurvey(surveyToBePublishedDoc);
        assertTrue(surveyToBePublished.isPublished());

        DocumentModel surveyToStayInProjectDoc = session.getDocument(surveyToStayInProject.getSurveyDocument().getRef());
        surveyToStayInProject = toSurvey(surveyToStayInProjectDoc);
        assertFalse(surveyToStayInProject.isPublished());
        assertTrue(surveyToStayInProject.isInProject());
    }

    @Test
    public void shouldUpdateStatusToClosedThroughListener() throws Exception {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);
        DateTime twoDaysAfter = now.plusDays(2);

        DocumentModel ws = createWorkspace("ws");
        Survey surveyToBeClosed = createSurvey(ws, "surveyToBeClosed",
                "Question 1", twoDaysBefore.toDate(), twoDaysBefore.toDate(),
                "Yes", "No");
        surveyToBeClosed = surveyService.updateSurveyStatus(surveyToBeClosed,
                new Date());
        assertNotNull(surveyToBeClosed);
        assertTrue(surveyToBeClosed.isPublished());

        Survey surveyToStayPublished = createSurvey(ws,
                "surveyToStayPublished", "Question 1", twoDaysBefore.toDate(),
                twoDaysAfter.toDate(), "Yes", "No");
        surveyToStayPublished = surveyService.updateSurveyStatus(
                surveyToStayPublished, new Date());
        assertNotNull(surveyToStayPublished);
        assertTrue(surveyToStayPublished.isPublished());

        Survey surveyToStayInProject = createSurvey(ws,
                "surveyToStayInProject", "Question 1", twoDaysAfter.toDate(),
                null, "Yes", "No");
        assertNotNull(surveyToStayInProject);
        assertTrue(surveyToStayInProject.isInProject());

        EventService eventService = Framework.getService(EventService.class);
        EventContext context = new EventContextImpl(session,
                session.getPrincipal());
        eventService.fireEvent(UPDATE_SURVEYS_STATUS_EVENT, context);
        session.save();
        eventService.waitForAsyncCompletion();

        DocumentModel surveyToBeClosedDoc = session.getDocument(surveyToBeClosed.getSurveyDocument().getRef());
        surveyToBeClosed = toSurvey(surveyToBeClosedDoc);
        assertTrue(surveyToBeClosed.isClosed());
        assertFalse(surveyToBeClosed.isPublished());
        assertFalse(surveyToBeClosed.isInProject());

        DocumentModel surveyToStayPublishedDoc = session.getDocument(surveyToStayPublished.getSurveyDocument().getRef());
        surveyToStayPublished = toSurvey(surveyToStayPublishedDoc);
        assertTrue(surveyToStayPublished.isPublished());
        assertFalse(surveyToStayPublished.isInProject());
        assertFalse(surveyToStayPublished.isClosed());

        DocumentModel surveyToStayInProjectDoc = session.getDocument(surveyToStayInProject.getSurveyDocument().getRef());
        surveyToStayInProject = toSurvey(surveyToStayInProjectDoc);
        assertTrue(surveyToStayInProject.isInProject());
        assertFalse(surveyToStayInProject.isPublished());
        assertFalse(surveyToStayInProject.isClosed());
    }

}
