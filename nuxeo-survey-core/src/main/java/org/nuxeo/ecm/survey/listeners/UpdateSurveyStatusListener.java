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

package org.nuxeo.ecm.survey.listeners;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;
import static org.nuxeo.ecm.survey.Constants.SURVEY_DOCUMENT_TYPE;
import static org.nuxeo.ecm.survey.SurveyHelper.toSurvey;

import java.util.Date;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.survey.SurveyService;
import org.nuxeo.runtime.api.Framework;

/**
 * Listener updating the status a Survey after creation or modification.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public class UpdateSurveyStatusListener implements PostCommitEventListener {

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        if (events.containsEventName(DOCUMENT_CREATED)
                || events.containsEventName(DOCUMENT_UPDATED)) {
            for (Event event : events) {
                if (DOCUMENT_CREATED.equals(event.getName())
                        || DOCUMENT_UPDATED.equals(event.getName())) {
                    handleEvent(event);
                }
            }
        }
    }

    public void handleEvent(Event event) throws ClientException {
        EventContext eventContext = event.getContext();
        if (eventContext instanceof DocumentEventContext) {
            DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
            DocumentModel doc = docEventContext.getSourceDocument();
            if (SURVEY_DOCUMENT_TYPE.equals(doc.getType())) {
                getSurveyService().updateSurveyStatus(toSurvey(doc), new Date());
            }
        }
    }

    protected SurveyService getSurveyService() throws ClientException {
        try {
            return Framework.getService(SurveyService.class);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

}
