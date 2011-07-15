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

package org.nuxeo.ecm.survey.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.ecm.survey.SurveyResult;
import org.nuxeo.ecm.survey.SurveyService;

/**
 * Operation to answer a Survey.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = AnswerSurvey.ID, category = Constants.CAT_SERVICES, label = "Answer a survey", description = "Answer a survey."
        + "The surveyId and answerIndex are required parameters.")
public class AnswerSurvey {

    public static final String ID = "Services.AnswerSurvey";

    @Context
    protected CoreSession session;

    @Context
    protected SurveyService surveyService;

    @Param(name = "surveyId")
    protected String surveyId;

    @Param(name = "answerIndex")
    protected Integer answerIndex;

    @OperationMethod
    public Blob run() throws Exception {
        DocumentModel surveyDocument = session.getDocument(new IdRef(surveyId));
        Survey survey = surveyDocument.getAdapter(Survey.class);
        SurveyResult surveyResult = surveyService.getResultFor(survey);

        surveyService.answer(session.getPrincipal().getName(), survey,
                answerIndex);

        // add the answer to the existing result
        String[] answers = survey.getAnswers();
        String answer = answers[answerIndex];
        Long result = surveyResult.getResultsByAnswer().get(answer);
        if (result == null) {
            result = 0L;
        }
        surveyResult.getResultsByAnswer().put(answer, result + 1);
        return new StringBlob(SurveyHelper.toJSON(surveyResult).toString(),
                "application/json");
    }

}
