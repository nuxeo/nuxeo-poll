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

package org.nuxeo.ecm.poll;

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
 * Business delegate exposing the {@link PollService} as a seam component.
 *
 * @author <a href="mailto:troger@nuxeo.com">Thomas Roger</a>
 * @since 5.4.3
 */
@Name("pollService")
@Scope(SESSION)
public class PollServiceBusinessDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(PollServiceBusinessDelegate.class);

    protected PollService pollService;

    /**
     * Acquires a new {@link PollService} reference. The related service may be deployed on a local or remote AppServer.
     *
     * @throws ClientException
     */
    @Unwrap
    public PollService getService() throws ClientException {
        if (pollService == null) {
            try {
                pollService = Framework.getService(PollService.class);
            } catch (Exception e) {
                final String errMsg = "Error connecting to PollService. " + e.getMessage();
                throw new ClientException(errMsg, e);
            }
            if (pollService == null) {
                throw new ClientException("PollService service not bound");
            }
        }
        return pollService;
    }

    @Destroy
    public void destroy() {
        if (pollService != null) {
            pollService = null;
        }
        log.debug("Destroyed the seam component");
    }
}
