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

package org.nuxeo.ecm.survey.operations;

import static org.nuxeo.ecm.survey.SurveyHelper.toJSON;
import static org.nuxeo.ecm.survey.SurveyHelper.toSurvey;

import java.io.ByteArrayInputStream;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.InputStreamBlob;
import org.nuxeo.ecm.survey.Survey;
import org.nuxeo.ecm.survey.SurveyResult;
import org.nuxeo.ecm.survey.SurveyService;

/**
 * Operation to get a Survey.
 * <p>
 * Returns also the {@link SurveyResult} for the survey, If the param
 * {@code withResult} is set to {@code true}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Operation(id = GetSurvey.ID, category = Constants.CAT_SERVICES, label = "Get a survey", description = "Get a survey based on its id."
        + "Returns also the survey result if the parameter 'withResult' is 'true'.")
public class GetSurvey {

    public static final String ID = "Services.GetSurvey";

    @Context
    protected CoreSession session;

    @Context
    protected SurveyService surveyService;

    @Param(name = "surveyId")
    protected String surveyId;

    @Param(name = "withResult", required = false)
    protected Boolean withResult = false;

    @OperationMethod
    public Blob run() throws Exception {
        DocumentModel surveyDocument = session.getDocument(new IdRef(surveyId));
        Survey survey = toSurvey(surveyDocument);

        boolean alreadyAnswered = surveyService.hasUserAnswered(
                session.getPrincipal().getName(), survey);

        JSONObject json;
        if (withResult) {
            SurveyResult surveyResult = surveyService.getResultFor(survey);
            json = toJSON(survey, surveyResult, alreadyAnswered);
        } else {
            json = toJSON(survey, alreadyAnswered);
        }
        return new InputStreamBlob(new ByteArrayInputStream(
                json.toString().getBytes("UTF-8")), "application/json");
    }

}
