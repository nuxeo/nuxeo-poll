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

import java.util.List;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Service handling Surveys.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public interface SurveyService {

    /**
     * Returns the surveys container located inside the given {@code doc}.
     * <p>
     * The container is created if it does not exist.
     *
     * @param doc the document where to create the container
     */
    DocumentModel getSurveysContainer(DocumentModel doc);

    /**
     * Returns all published surveys available with the given {@code session}.
     */
    List<Survey> getPublishedSurveys(CoreSession session);

    /**
     * Returns {@code true} if the user with the given {@code username} has
     * already answer the {@code survey}, {@code false otherwise}.
     */
    boolean hasUserAnswered(String username, Survey survey);

    /**
     * Store that the user with the given @{code username} has answered the
     * {@code survey} with the answer number {@code answerIndex}.
     */
    void answer(String username, Survey survey, int answerIndex);

    /**
     * Returns the current {@code SurveyResult} for the given {@code survey}.
     */
    SurveyResult getResultFor(Survey survey);

    // activity summary for a survey

}
