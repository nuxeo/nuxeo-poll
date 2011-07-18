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

import java.util.HashMap;
import java.util.Map;

/**
 * Object storing the result of a given survey.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public final class SurveyResult {

    private String surveyId;

    private Map<String, Long> resultsByAnswer = new HashMap<String, Long>();

    public SurveyResult(String surveyId, Map<String, Long> resultsByAnswer) {
        this.surveyId = surveyId;
        this.resultsByAnswer = resultsByAnswer;
    }

    public Map<String, Long> getResultsByAnswer() {
        return resultsByAnswer;
    }

    public long getResultsCount() {
        long resultsCount = 0;
        for (Long result : resultsByAnswer.values()) {
            resultsCount += result;
        }
        return resultsCount;
    }

    public String getSurveyId() {
        return surveyId;
    }

}
