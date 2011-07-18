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

import static org.nuxeo.ecm.survey.SurveyHelper.toJSON;

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
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.ecm.survey.SurveyResult;
import org.nuxeo.ecm.survey.SurveyService;

/**
 * Operation to get all the published surveys, or just the unanswered ones.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetPublishedSurveys.ID, category = Constants.CAT_SERVICES, label = "Get published surveys", description = "Get published surveys."
        + "Default is to get all published surveys."
        + "The 'onlyUnansweredSurveys' parameter can "
        + "be used to retrieve only the unanswered surveys.")
public class GetPublishedSurveys {

    public static final String ID = "Services.GetPublishedSurveys";

    @Context
    protected CoreSession session;

    @Context
    protected SurveyService surveyService;

    @Param(name = "onlyUnansweredSurveys", required = false)
    protected Boolean onlyUnansweredSurveys = false;

    @OperationMethod
    public Blob run() throws ClientException, IOException {
        JSONArray array = new JSONArray();
        List<Survey> availableSurveys = surveyService.getPublishedSurveys(session);
        for (Survey survey : availableSurveys) {
            writeSurvey(array, survey);
        }

        JSONObject object = new JSONObject();
        object.put("surveys", array);
        return new StringBlob(object.toString(), "application/json");
    }

    protected void writeSurvey(JSONArray array, Survey survey)
            throws ClientException, IOException {
        NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        if (surveyService.hasUserAnswered(principal.getName(), survey)) {
            if (!onlyUnansweredSurveys) {
                writeAnsweredSurvey(array, survey);
            }
        } else {
            writeUnansweredSurvey(array, survey);
        }
    }

    protected void writeAnsweredSurvey(JSONArray array, Survey survey)
            throws ClientException, IOException {
        SurveyResult surveyResult = surveyService.getResultFor(survey);
        array.add(toJSON(survey, surveyResult, true));
    }

    protected void writeUnansweredSurvey(JSONArray array, Survey survey)
            throws IOException, ClientException {
        array.add(toJSON(survey));
    }

}
