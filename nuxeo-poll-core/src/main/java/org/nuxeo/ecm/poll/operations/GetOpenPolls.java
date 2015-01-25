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

package org.nuxeo.ecm.poll.operations;

import static org.nuxeo.ecm.poll.PollHelper.toJSON;

import java.io.IOException;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.poll.Poll;
import org.nuxeo.ecm.poll.PollResult;
import org.nuxeo.ecm.poll.PollService;

/**
 * Operation to get all the open polls, or just the unanswered open polls.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetOpenPolls.ID, category = Constants.CAT_SERVICES, label = "Get open polls", description = "Get open polls."
        + "Default is to get all open polls."
        + "The 'onlyUnansweredPolls' parameter can "
        + "be used to retrieve only the unanswered polls.")
public class GetOpenPolls {

    public static final String ID = "Services.GetOpenPolls";

    @Context
    protected CoreSession session;

    @Context
    protected PollService pollService;

    @Param(name = "onlyUnansweredPolls", required = false)
    protected Boolean onlyUnansweredPolls = false;

    @OperationMethod
    public Blob run() throws ClientException, IOException {
        JSONArray array = new JSONArray();
        List<Poll> availablePolls;

        if (onlyUnansweredPolls) {
            availablePolls = pollService.getUnansweredOpenPolls(session);
            for (Poll poll : availablePolls) {
                writeUnansweredPoll(array, poll);
            }
        } else {
            availablePolls = pollService.getOpenPolls(session);
            for (Poll poll : availablePolls) {
                writePoll(array, poll);
            }
        }

        JSONObject object = new JSONObject();
        object.put("polls", array);
        return Blobs.createBlob(object.toString(), "application/json");
    }

    protected void writePoll(JSONArray array, Poll poll) throws ClientException, IOException {
        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        if (pollService.hasUserAnswered(principal.getName(), poll)) {
            writeAnsweredPoll(array, poll);
        } else {
            writeUnansweredPoll(array, poll);
        }
    }

    protected void writeAnsweredPoll(JSONArray array, Poll poll) throws ClientException, IOException {
        PollResult pollResult = pollService.getResultFor(poll);
        array.add(toJSON(poll, pollResult, true));
    }

    protected void writeUnansweredPoll(JSONArray array, Poll poll) throws IOException, ClientException {
        array.add(toJSON(poll));
    }

}
