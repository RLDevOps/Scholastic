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

package org.olat.ims.qti.editor.beecom.parser;

import org.dom4j.Element;
import org.olat.ims.qti.editor.beecom.objects.Matvideo;

/**
 * @author rkulow
 */
public class MatvideoParser implements IParser {

	/**
	 * @see org.olat.ims.qti.editor.beecom.parser.IParser#parse(org.dom4j.Element)
	 */
	@Override
	public Object parse(final Element element) {
		// assert element.getName().equalsIgnoreCase("matvideo");
		final String uri = element.attributeValue("uri");
		if (uri != null && uri.length() > 0) {
			final Matvideo mat = new Matvideo(uri);
			final String videotype = element.attributeValue("videotype");
			if (videotype != null) {
				mat.setVideotype(videotype);
			}
			final String width = element.attributeValue("width");
			if (width != null) {
				mat.setWidth(width);
			}
			final String height = element.attributeValue("height");
			if (height != null) {
				mat.setHeight(height);
			}
			return mat;
		}
		return null;
	}

}
