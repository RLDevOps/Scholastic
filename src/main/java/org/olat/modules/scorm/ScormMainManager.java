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
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.manager.BasicManager;

/**
 * Description:<br>
 * TODO:
 * <P>
 * Initial Date: 08.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ScormMainManager extends BasicManager {
	private static ScormMainManager INSTANCE = new ScormMainManager();

	private ScormMainManager() {
		// singleton
	}

	public static ScormMainManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @param ureq
	 * @param wControl
	 * @param showMenu if true, the ims cp menu is shown
	 * @param apiCallback the callback to where lmssetvalue data is mirrored, or null if no callback is desired
	 * @param cpRoot
	 * @param resourceId
	 * @param lesson_mode add null for the default value or "normal", "browse" or "review"
	 * @param credit_mode add null for the default value or "credit", "no-credit"
	 */
	public ScormAPIandDisplayController createScormAPIandDisplayController(final UserRequest ureq, final WindowControl wControl, final boolean showMenu,
			final ScormAPICallback apiCallback, final File cpRoot, final String resourceId, final String courseId, final String lesson_mode, final String credit_mode,
			final boolean previewMode, final boolean activate) {
		return new ScormAPIandDisplayController(ureq, wControl, showMenu, apiCallback, cpRoot, resourceId, courseId, lesson_mode, credit_mode, previewMode, activate);
	}

}
