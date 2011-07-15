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

import java.util.Date;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * An object that wraps a {@code DocumentModel} of type {@code Survey}.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
public interface Survey {

    String getId();

    String getQuestion();

    String[] getAnswers();

    Date getBeginDate();

    Date getEndDate();

    DocumentModel getSurveyDocumentModel();

}
