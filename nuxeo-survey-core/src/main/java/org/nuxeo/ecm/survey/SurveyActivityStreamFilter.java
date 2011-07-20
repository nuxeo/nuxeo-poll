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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityStreamFilter;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.activity.ActivityStreamServiceImpl;

/**
 * Activity Stream filter handling survey activities.
 * <p>
 * The different queries this filter can handle are defined in the
 * {@link QueryType} enum.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class SurveyActivityStreamFilter implements ActivityStreamFilter {

    public static final String ID = "SurveyActivityStreamFilter";

    public enum QueryType {
        ALL_ANSWERS_FOR_SURVEY, ACTOR_ANSWERS_FOR_SURVEY
    }

    public static final String QUERY_TYPE_PARAMETER = "queryTypeParameter";

    public static final String SURVEY_ID_PARAMETER = "surveyId";

    public static final String ACTOR_PARAMETER = "actor";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isInterestedIn(Activity activity) {
        return Constants.ANSWER_SURVEY_VERB.equals(activity.getVerb());
    }

    @Override
    public void handleNewActivity(ActivityStreamService activityStreamService,
            Activity activity) {
        // nothing to do for now
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Activity> query(ActivityStreamService activityStreamService,
            Map<String, Serializable> parameters, Integer pageSize,
            Integer currentPage) {
        EntityManager em = ((ActivityStreamServiceImpl) activityStreamService).getEntityManager();
        QueryType queryType = (QueryType) parameters.get(QUERY_TYPE_PARAMETER);
        if (queryType == null) {
            return Collections.emptyList();
        }

        Query query = null;
        String surveyId = (String) parameters.get(SURVEY_ID_PARAMETER);
        String actor = (String) parameters.get(ACTOR_PARAMETER);
        switch (queryType) {
        case ALL_ANSWERS_FOR_SURVEY:
            query = em.createQuery("select activity from Activity activity where activity.target = :surveyId");
            query.setParameter(SURVEY_ID_PARAMETER, surveyId);
            break;
        case ACTOR_ANSWERS_FOR_SURVEY:
            query = em.createQuery("select activity from Activity activity where activity.target = :surveyId and activity.actor = :actor");
            query.setParameter(SURVEY_ID_PARAMETER, surveyId);
            query.setParameter(ACTOR_PARAMETER, actor);
            break;
        }

        if (query == null) {
            return Collections.emptyList();
        }

        if (pageSize > 0) {
            query.setMaxResults(pageSize);
            if (currentPage > 0) {
                query.setFirstResult((currentPage - 1) * pageSize);
            }
        }
        return query.getResultList();
    }

}
