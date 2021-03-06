/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.ims.qti.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.olat.commons.servlets.pathhandlers.PathHandler;
import org.olat.commons.servlets.util.ResourceDescriptor;
import org.olat.core.util.WebappHelper;

/**
 * Initial Date: 08.09.2003
 * 
 * @author Mike Stock
 */
public class QTIEditorStaticsHandler implements PathHandler {

	/**
	 * 
	 */
	public QTIEditorStaticsHandler() {
		//
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#init(com.anthonyeden.lib.config.Configuration)
	 */
	@Override
	public void init(final String config) {
		//
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#getInputStream(javax.servlet.http.HttpServletRequest, org.olat.commons.servlets.util.ResourceDescriptor)
	 */
	@Override
	public InputStream getInputStream(final HttpServletRequest request, final ResourceDescriptor rd) {
		try {
			final File f = new File(QTIEditorPackage.getTmpBaseDir() + rd.getRelPath());
			return new BufferedInputStream(new FileInputStream(f));
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#getResourceDescriptor(javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public ResourceDescriptor getResourceDescriptor(final HttpServletRequest request, final String relPath) {
		try {
			final ResourceDescriptor rd = new ResourceDescriptor(relPath);
			final File f = new File(QTIEditorPackage.getTmpBaseDir() + relPath);
			rd.setLastModified(f.lastModified());
			rd.setSize(f.length());
			String mimeType = WebappHelper.getMimeType(relPath);
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			rd.setContentType(mimeType);
			return rd;
		} catch (final Exception e) {
			return null;
		}
	}

}