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

package org.olat.ims.qti.container.qtielements;

import org.dom4j.Element;

/**
 * Initial Date: 24.11.2004
 * 
 * @author Mike Stock
 */

public class AssessFeedback extends GenericQTIElement {

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "assessfeedback";

	/**
	 * @param el_assessfeedback
	 */
	public AssessFeedback(final Element el_assessfeedback) {
		super(el_assessfeedback);
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.GenericQTIElement#render(java.lang.StringBuilder, org.olat.ims.qti.container.itemelements.RenderInstructions)
	 */
	@Override
	public void render(final StringBuilder buffer, final RenderInstructions ri) {
		buffer.append("<div class=\"o_qti_item_assessfeedback\">");
		super.render(buffer, ri);
		buffer.append("</div>");
	}

}
