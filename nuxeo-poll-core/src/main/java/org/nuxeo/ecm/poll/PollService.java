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

import java.util.Date;
import java.util.List;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Service handling Polls.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public interface PollService {

    /**
     * Returns the polls container located inside the given {@code doc}.
     * <p>
     * The container is created if it does not exist.
     *
     * @param doc the document where to create the container
     */
    DocumentModel getPollsContainer(DocumentModel doc);

    /**
     * Returns all open polls available with the given {@code session}.
     */
    List<Poll> getOpenPolls(CoreSession session);

    /**
     * Returns all unanswered open polls available with the given
     * {@code session}.
     */
    List<Poll> getUnansweredOpenPolls(CoreSession session);

    /**
     * Returns {@code true} if the user with the given {@code username} has
     * already answer the {@code poll}, {@code false otherwise}.
     */
    boolean hasUserAnswered(String username, Poll poll);

    /**
     * Store that the user with the given @{code username} has answered the
     * {@code poll} with the answer number {@code answerIndex}.
     */
    void answer(String username, Poll poll, int answerIndex);

    /**
     * Returns the current {@code PollResult} for the given {@code poll}.
     */
    PollResult getResultFor(Poll poll);

    /**
     * Open the {@code poll}. The poll will be available through the
     * {@link #getOpenPolls} method.
     * <p>
     * The start date of the poll is set to now.
     *
     * @return the updated Poll
     */
    Poll openPoll(Poll poll);

    /**
     * Close the {@code poll}.
     * <p>
     * The end date of the poll is set to now.
     *
     * @return the updated Poll
     */
    Poll closePoll(Poll poll);

    /**
     * Update the status of the {@code poll} according to the given
     * {@code date}.
     * <p>
     * The poll can be open or closed according to its start date and end
     * date.
     */
    Poll updatePollStatus(Poll poll, Date date);

    // activity summary for a poll

}
