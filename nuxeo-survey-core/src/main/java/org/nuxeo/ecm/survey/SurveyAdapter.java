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

import static org.nuxeo.ecm.survey.Constants.SURVEY_ANSWERS_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_BEGIN_DATE_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_END_DATE_PROPERTY;
import static org.nuxeo.ecm.survey.Constants.SURVEY_QUESTION_PROPERTY;

import java.io.Serializable;
import java.util.Date;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Default implementation of {@link Survey}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SurveyAdapter implements Survey {

    protected final DocumentModel surveyDoc;

    public SurveyAdapter(DocumentModel survey) {
        this.surveyDoc = survey;
    }

    @Override
    public String getId() {
        return surveyDoc.getId();
    }

    @Override
    public String getQuestion() {
        return (String) getPropertyValue(SURVEY_QUESTION_PROPERTY);
    }

    @Override
    public String[] getAnswers() {
        return (String[]) getPropertyValue(SURVEY_ANSWERS_PROPERTY);
    }

    @Override
    public Date getBeginDate() {
        return (Date) getPropertyValue(SURVEY_BEGIN_DATE_PROPERTY);
    }

    @Override
    public Date getEndDate() {
        return (Date) getPropertyValue(SURVEY_END_DATE_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    protected Serializable getPropertyValue(String xpath) {
        try {
            return surveyDoc.getPropertyValue(xpath);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    @Override
    public DocumentModel getSurveyDocumentModel() {
        return surveyDoc;
    }

}
