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

package org.nuxeo.ecm.survey;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Helper methods to deal with {@link Survey} and {@link SurveyResult}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SurveyHelper {

    private SurveyHelper() {
        // helper class
    }

    /**
     * Returns a {@link Survey} object from the given {@code surveyDocument}.
     */
    public static Survey toSurvey(DocumentModel surveyDocument) {
        return surveyDocument.getAdapter(Survey.class);
    }

    /**
     * Returns a JSONObject for the given {@code survey}.
     */
    public static JSONObject toJSON(Survey survey, boolean answered) {
        JSONObject object = new JSONObject();
        object.put("surveyId", survey.getId());
        object.put("answered", answered);
        object.put("question", survey.getQuestion());
        object.put("answers", survey.getAnswers());
        return object;
    }

    /**
     * Returns a JSONObject for the given unanswered {@code survey}.
     */
    public static JSONObject toJSON(Survey survey) {
        return toJSON(survey, false);
    }

    /**
     * Returns a JSONObject for the given {@code surveyResult}.
     */
    public static JSONObject toJSON(SurveyResult surveyResult) {
        JSONObject object = new JSONObject();
        object.put("resultsCount", surveyResult.getResultsCount());

        JSONArray resultsByAnswer = new JSONArray();
        for (Map.Entry<String, Long> entry : surveyResult.getResultsByAnswer().entrySet()) {
            JSONObject answer = new JSONObject();
            answer.put("answer", entry.getKey());
            answer.put("result", entry.getValue());
            resultsByAnswer.add(answer);
        }

        object.put("resultsByAnswer", resultsByAnswer);
        return object;
    }

    /**
     * Returns a JSONObject for the given {@code survey} and
     * {@code surveyResult}.
     */
    public static JSONObject toJSON(Survey survey, SurveyResult surveyResult,
            boolean answered) {
        JSONObject object = toJSON(survey, answered);
        object.put("result", toJSON(surveyResult));
        return object;
    }

    /**
     * Returns a JSONObject for the given unanswered {@code survey} and
     * {@code surveyResult}.
     */
    public static JSONObject toJSON(Survey survey, SurveyResult surveyResult) {
        return toJSON(survey, surveyResult, false);
    }

}
