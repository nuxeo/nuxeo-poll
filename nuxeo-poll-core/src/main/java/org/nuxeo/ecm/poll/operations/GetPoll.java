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
import static org.nuxeo.ecm.poll.PollHelper.toPoll;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.poll.Poll;
import org.nuxeo.ecm.poll.PollResult;
import org.nuxeo.ecm.poll.PollService;

/**
 * Operation to get a Poll.
 * <p>
 * Returns also the {@link PollResult} for the poll, If the param {@code withResult} is set to {@code true}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetPoll.ID, category = Constants.CAT_SERVICES, label = "Get a poll", description = "Get a poll based on its id."
        + "Returns also the poll result if the parameter 'withResult' is 'true'.")
public class GetPoll {

    public static final String ID = "Services.GetPoll";

    @Context
    protected CoreSession session;

    @Context
    protected PollService pollService;

    @Param(name = "pollId")
    protected String pollId;

    @Param(name = "withResult", required = false)
    protected Boolean withResult = false;

    @OperationMethod
    public Blob run() throws Exception {
        DocumentModel pollDocument = session.getDocument(new IdRef(pollId));
        Poll poll = toPoll(pollDocument);

        boolean alreadyAnswered = pollService.hasUserAnswered(session.getPrincipal().getName(), poll);

        JSONObject json;
        if (withResult) {
            PollResult pollResult = pollService.getResultFor(poll);
            json = toJSON(poll, pollResult, alreadyAnswered);
        } else {
            json = toJSON(poll, alreadyAnswered);
        }
        return Blobs.createBlob(json.toString(), "application/json");
    }

}
