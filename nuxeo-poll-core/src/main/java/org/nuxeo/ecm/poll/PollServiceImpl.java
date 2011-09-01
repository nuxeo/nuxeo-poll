/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.ecm.poll;

import static org.nuxeo.ecm.poll.Constants.ANSWER_SURVEY_VERB;
import static org.nuxeo.ecm.poll.Constants.CLOSE_SURVEY_TRANSITION;
import static org.nuxeo.ecm.poll.Constants.OPEN_SURVEY_TRANSITION;
import static org.nuxeo.ecm.poll.Constants.SURVEY_DOCUMENT_TYPE;
import static org.nuxeo.ecm.poll.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.poll.Constants.SURVEY_OPEN_STATE;
import static org.nuxeo.ecm.poll.Constants.SURVEY_START_DATE_PROPERTY;
import static org.nuxeo.ecm.poll.PollActivityStreamFilter.ACTOR_PARAMETER;
import static org.nuxeo.ecm.poll.PollActivityStreamFilter.QUERY_TYPE_PARAMETER;
import static org.nuxeo.ecm.poll.PollActivityStreamFilter.QueryType.ACTOR_ANSWERS_FOR_SURVEY;
import static org.nuxeo.ecm.poll.PollActivityStreamFilter.QueryType.ALL_ANSWERS_FOR_SURVEY;
import static org.nuxeo.ecm.poll.PollActivityStreamFilter.SURVEY_ID_PARAMETER;
import static org.nuxeo.ecm.poll.PollHelper.toPoll;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of {@link PollService}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class PollServiceImpl implements PollService {

    public static final String SURVEYS_CONTAINER_NAME = "pollsContainer";

    protected ActivityStreamService activityStreamService;

    @Override
    public DocumentModel getPollsContainer(DocumentModel doc) {
        try {
            Path pollsContainerPath = doc.getPath().append(
                    SURVEYS_CONTAINER_NAME);
            PathRef pollsContainerRef = new PathRef(
                    pollsContainerPath.toString());
            CoreSession session = doc.getCoreSession();
            if (session.exists(pollsContainerRef)) {
                return session.getDocument(pollsContainerRef);
            } else {
                return createPollsContainer(session, doc);
            }
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    protected DocumentModel createPollsContainer(CoreSession session,
            DocumentModel doc) {
        try {
            DocumentModel pollsContainer = session.createDocumentModel(
                    doc.getPathAsString(), SURVEYS_CONTAINER_NAME,
                    "HiddenFolder");
            pollsContainer.setPropertyValue("dc:title", "Polls");
            pollsContainer = session.createDocument(pollsContainer);
            session.save();
            return pollsContainer;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public List<Poll> getOpenPolls(CoreSession session) {
        try {
            String query = "SELECT * FROM Document WHERE ecm:primaryType = '%s'"
                    + " AND ecm:currentLifeCycleState = '%s' ORDER BY %s";
            List<DocumentModel> docs = session.query(String.format(query,
                    SURVEY_DOCUMENT_TYPE, SURVEY_OPEN_STATE,
                    SURVEY_START_DATE_PROPERTY));

            List<Poll> polls = new ArrayList<Poll>();
            for (DocumentModel doc : docs) {
                polls.add(toPoll(doc));
            }
            return polls;
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public List<Poll> getUnansweredOpenPolls(CoreSession session) {
        List<Poll> polls = getOpenPolls(session);
        for (Iterator<Poll> it = polls.iterator(); it.hasNext();) {
            Poll poll = it.next();
            if (hasUserAnswered(session.getPrincipal().getName(), poll)) {
                it.remove();
            }
        }
        return polls;
    }

    @Override
    public boolean hasUserAnswered(String username, Poll poll) {
        ActivityStreamService activityStreamService = getActivityStreamService();

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ACTOR_ANSWERS_FOR_SURVEY);
        parameters.put(SURVEY_ID_PARAMETER, poll.getId());
        parameters.put(ACTOR_PARAMETER,
                ActivityHelper.createUserActivityObject(username));
        List<Activity> activities = activityStreamService.query(
                PollActivityStreamFilter.ID, parameters);
        return !activities.isEmpty();
    }

    @Override
    public void answer(String username, Poll poll, int answerIndex) {
        Activity activity = new ActivityBuilder().verb(ANSWER_SURVEY_VERB).actor(
                ActivityHelper.createUserActivityObject(username)).target(
                poll.getId()).object(getAnswer(poll, answerIndex)).build();

        getActivityStreamService().addActivity(activity);
    }

    private String getAnswer(Poll poll, int answerIndex) {
        String[] answers = poll.getAnswers();
        return answers.length > answerIndex ? answers[answerIndex] : null;
    }

    @Override
    public PollResult getResultFor(Poll poll) {
        ActivityStreamService activityStreamService = getActivityStreamService();

        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_TYPE_PARAMETER, ALL_ANSWERS_FOR_SURVEY);
        parameters.put("pollId", poll.getId());
        List<Activity> activities = activityStreamService.query(
                PollActivityStreamFilter.ID, parameters);

        Map<String, Long> resultsByAnswer = new LinkedHashMap<String, Long>();
        String[] answers = poll.getAnswers();
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

        return new PollResult(poll.getId(), resultsByAnswer);
    }

    @Override
    public Poll openPoll(Poll poll) {
        try {
            DocumentModel pollDocument = poll.getPollDocument();
            CoreSession session = pollDocument.getCoreSession();
            pollDocument.followTransition(OPEN_SURVEY_TRANSITION);
            pollDocument.setPropertyValue(SURVEY_START_DATE_PROPERTY,
                    new Date());
            pollDocument = session.saveDocument(pollDocument);
            session.save();
            return toPoll(pollDocument);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public Poll closePoll(Poll poll) {
        try {
            DocumentModel pollDocument = poll.getPollDocument();
            CoreSession session = pollDocument.getCoreSession();
            pollDocument.followTransition(CLOSE_SURVEY_TRANSITION);
            pollDocument.setPropertyValue(SURVEY_END_DATE_PROPERTY,
                    new Date());
            pollDocument = session.saveDocument(pollDocument);
            session.save();
            return toPoll(pollDocument);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public Poll updatePollStatus(Poll poll, Date date) {
        Date startDate = poll.getStartDate();
        Date endDate = poll.getEndDate();
        if (poll.isInProject()) {
            if (startDate != null && date.after(startDate)) {
                poll = openPoll(poll);
            }
        } else if (poll.isOpen()) {
            if (endDate != null && date.after(endDate)) {
                poll = closePoll(poll);
            }
        }
        return poll;
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

}
