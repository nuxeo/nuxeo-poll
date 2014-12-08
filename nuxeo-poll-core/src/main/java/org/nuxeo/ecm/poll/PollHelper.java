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

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Helper methods to deal with {@link Poll} and {@link PollResult}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class PollHelper {

    private PollHelper() {
        // helper class
    }

    /**
     * Returns a {@link Poll} object from the given {@code pollDocument}.
     */
    public static Poll toPoll(DocumentModel pollDocument) {
        return pollDocument.getAdapter(Poll.class);
    }

    /**
     * Returns a JSONObject for the given {@code poll}.
     */
    public static JSONObject toJSON(Poll poll, boolean answered) {
        JSONObject object = new JSONObject();
        object.put("pollId", poll.getId());
        object.put("answered", answered);
        object.put("question", poll.getQuestion());
        object.put("answers", poll.getAnswers());
        return object;
    }

    /**
     * Returns a JSONObject for the given unanswered {@code poll}.
     */
    public static JSONObject toJSON(Poll poll) {
        return toJSON(poll, false);
    }

    /**
     * Returns a JSONObject for the given {@code pollResult}.
     */
    public static JSONObject toJSON(PollResult pollResult) {
        JSONObject object = new JSONObject();
        object.put("resultsCount", pollResult.getResultsCount());

        JSONArray resultsByAnswer = new JSONArray();
        for (Map.Entry<String, Long> entry : pollResult.getResultsByAnswer().entrySet()) {
            JSONObject answer = new JSONObject();
            answer.put("answer", entry.getKey());
            answer.put("result", entry.getValue());
            resultsByAnswer.add(answer);
        }

        object.put("resultsByAnswer", resultsByAnswer);
        return object;
    }

    /**
     * Returns a JSONObject for the given {@code poll} and {@code pollResult}.
     */
    public static JSONObject toJSON(Poll poll, PollResult pollResult, boolean answered) {
        JSONObject object = toJSON(poll, answered);
        object.put("result", toJSON(pollResult));
        return object;
    }

    /**
     * Returns a JSONObject for the given unanswered {@code poll} and {@code pollResult}.
     */
    public static JSONObject toJSON(Poll poll, PollResult pollResult) {
        return toJSON(poll, pollResult, false);
    }

}
