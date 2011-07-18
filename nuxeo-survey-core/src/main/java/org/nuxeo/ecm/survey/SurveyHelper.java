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

    public static Survey toSurvey(DocumentModel surveyDocument) {
        return surveyDocument.getAdapter(Survey.class);
    }

    /**
     * Returns a JSONObject for the given {@code survey}.
     */
    public static JSONObject toJSON(Survey survey) {
        JSONObject object = new JSONObject();
        object.put("surveyId", survey.getId());
        object.put("answered", false);
        object.put("question", survey.getQuestion());
        object.put("answers", survey.getAnswers());
        return object;
    }

    /**
     * Returns a JSONObject for the given {@code surveyResult}.
     */
    public static JSONObject toJSON(SurveyResult surveyResult) {
        JSONObject object = new JSONObject();
        object.put("surveyId", surveyResult.getSurveyId());
        object.put("answered", true);
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
    public static JSONObject toJSON(Survey survey, SurveyResult SurveyResult) {
        JSONObject object = new JSONObject();
        object.put("survey", toJSON(survey));
        object.put("surveyResult", toJSON(SurveyResult));
        return object;
    }

}
