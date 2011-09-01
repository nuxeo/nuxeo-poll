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

package org.nuxeo.ecm.poll.listeners;

import static org.nuxeo.ecm.core.api.LifeCycleConstants.DELETED_STATE;
import static org.nuxeo.ecm.poll.Constants.SURVEY_CLOSED_STATE;
import static org.nuxeo.ecm.poll.PollHelper.toPoll;

import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.poll.Poll;
import org.nuxeo.ecm.poll.PollService;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener updating the status of all in project or open Polls.
 * <p>
 * {@code project} -> {@code open} -> {@code closed}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class UpdateAllPollsStatusListener implements EventListener {

    public static final String UPDATE_SURVEYS_STATUS_EVENT = "updatePollsStatus";

    @Override
    public void handleEvent(Event event) throws ClientException {
        if (!UPDATE_SURVEYS_STATUS_EVENT.equals(event.getName())) {
            return;
        }

        try {
            RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);
            Repository repository = repositoryManager.getDefaultRepository();
            UpdatePollsStatus updatePollsStatus = new UpdatePollsStatus(
                    repository.getName());
            updatePollsStatus.runUnrestricted();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    /**
     * {@code UnrestrictedSessionRunner} updating the status of all Polls.
     */
    public static class UpdatePollsStatus extends UnrestrictedSessionRunner {

        public UpdatePollsStatus(String repositoryName) {
            super(repositoryName);
        }

        @Override
        public void run() throws ClientException {
            String query = "SELECT * FROM Poll WHERE ecm:currentLifeCycleState <> '%s' AND ecm:currentLifeCycleState <> '%s'";
            List<DocumentModel> polls = session.query(String.format(query,
                    SURVEY_CLOSED_STATE, DELETED_STATE));
            PollService pollService = getPollService();
            Date now = new Date();
            for (DocumentModel poll : polls) {
                updateStatus(pollService, toPoll(poll), now);
            }
            session.save();
        }

        protected void updateStatus(PollService pollService, Poll poll,
                Date now) throws ClientException {
            pollService.updatePollStatus(poll, now);
        }

        protected PollService getPollService() throws ClientException {
            try {
                return Framework.getService(PollService.class);
            } catch (Exception e) {
                throw new ClientException(e);
            }
        }

    }

}
