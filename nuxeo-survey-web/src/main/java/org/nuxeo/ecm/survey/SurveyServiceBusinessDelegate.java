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

import static org.jboss.seam.ScopeType.SESSION;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.runtime.api.Framework;

/**
 * Business delegate exposing the {@link SurveyService} as a seam component.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Name("surveyService")
@Scope(SESSION)
public class SurveyServiceBusinessDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SurveyServiceBusinessDelegate.class);

    protected SurveyService surveyService;

    /**
     * Acquires a new {@link SurveyService} reference. The related service may
     * be deployed on a local or remote AppServer.
     *
     * @throws ClientException
     */
    @Unwrap
    public SurveyService getService() throws ClientException {
        if (surveyService == null) {
            try {
                surveyService = Framework.getService(SurveyService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to SurveyService. "
                        + e.getMessage();
                throw new ClientException(errMsg, e);
            }
            if (surveyService == null) {
                throw new ClientException("SurveyService service not bound");
            }
        }
        return surveyService;
    }

    @Destroy
    public void destroy() {
        if (surveyService != null) {
            surveyService = null;
        }
        log.debug("Destroyed the seam component");
    }
}
