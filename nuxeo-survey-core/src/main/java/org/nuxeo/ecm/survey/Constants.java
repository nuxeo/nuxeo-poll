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

/**
 * Survey constants class.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class Constants {

    private Constants() {
        // Constants class
    }

    public static final String SURVEY_DOCUMENT_TYPE = "Survey";

    public static final String SURVEY_QUESTION_PROPERTY = "dc:title";

    public static final String SURVEY_ANSWERS_PROPERTY = "survey:answers";

    public static final String SURVEY_BEGIN_DATE_PROPERTY = "survey:begin_date";

    public static final String SURVEY_END_DATE_PROPERTY = "dc:expired";

    // Life cycle
    public static final String SURVEY_PROJECT_STATE = "project";

    public static final String SURVEY_PUBLISHED_STATE = "published";

    public static final String SURVEY_CLOSED_STATE = "closed";

    public static final String PUBLISH_SURVEY_TRANSITION = "publish";

    public static final String CLOSE_SURVEY_TRANSITION = "close";

    // Activity stream
    public static final String ANSWER_SURVEY_VERB = "answer survey";

}
