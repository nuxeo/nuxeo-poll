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

import static org.nuxeo.ecm.poll.Constants.SURVEY_ANSWERS_PROPERTY;
import static org.nuxeo.ecm.poll.Constants.SURVEY_DOCUMENT_TYPE;
import static org.nuxeo.ecm.poll.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.poll.Constants.SURVEY_QUESTION_PROPERTY;
import static org.nuxeo.ecm.poll.Constants.SURVEY_START_DATE_PROPERTY;
import static org.nuxeo.ecm.poll.PollHelper.toPoll;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.junit.Before;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.EventServiceAdmin;
import org.nuxeo.ecm.core.persistence.PersistenceProvider;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public abstract class AbstractPollTest {

    @Inject
    protected FeaturesRunner featuresRunner;

    @Inject
    protected ActivityStreamService activityStreamService;

    @Inject
    protected PollService pollService;

    @Inject
    protected CoreSession session;

    @Inject
    protected EventService eventService;

    @Inject
    protected EventServiceAdmin eventServiceAdmin;

    @Before
    public void disableActivityStreamListener() {
        eventServiceAdmin.setListenerEnabledFlag("activityStreamListener",
                false);
    }

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

    protected Poll createPoll(DocumentModel superSpace, String name,
            String question, String... answers) throws ClientException {
        return createPoll(superSpace, name, question, null, null, answers);
    }

    protected Poll createPoll(DocumentModel superSpace, String name,
            String question, Date startDate, Date endDate, String... answers)
            throws ClientException {
        DocumentModel poll = session.createDocumentModel(
                pollService.getPollsContainer(superSpace).getPathAsString(),
                name, SURVEY_DOCUMENT_TYPE);
        poll.setPropertyValue(SURVEY_QUESTION_PROPERTY, question);
        poll.setPropertyValue(SURVEY_ANSWERS_PROPERTY, answers);
        poll.setPropertyValue(SURVEY_START_DATE_PROPERTY, startDate);
        poll.setPropertyValue(SURVEY_END_DATE_PROPERTY, endDate);
        poll = session.createDocument(poll);
        session.save(); // fire post commit event listener
        session.save(); // flush the session to retrieve updated poll
        poll = session.getDocument(poll.getRef());
        return toPoll(poll);
    }

    protected Poll createOpenPoll(DocumentModel superSpace, String name,
            String question, String... answers) throws ClientException {
        DateTime now = new DateTime();
        DateTime twoDaysBefore = now.minusDays(2);
        return createPoll(superSpace, name, question, twoDaysBefore.toDate(),
                null, answers);
    }

    protected void changeUser(String username) {
        featuresRunner.getFeature(CoreFeature.class).getRepository().switchUser(
                username);
    }

}
