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

import java.util.Date;
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

    /**
     * Publish the {@code survey}. The survey will be available through the
     * {@link #getPublishedSurveys} method.
     * <p>
     * The begin date of the survey is set to now.
     *
     * @return the updated Survey
     */
    Survey publishSurvey(Survey survey);

    /**
     * Close the {@code survey}.
     * <p>
     * The end date of the survey is set to now.
     *
     * @return the updated Survey
     */
    Survey closeSurvey(Survey survey);

    /**
     * Update the status of the {@code survey} according to the given
     * {@code date}.
     * <p>
     * The survey can be published or closed according to its begin date and end
     * date.
     */
    Survey updateSurveyStatus(Survey survey, Date date);

    // activity summary for a survey

}
