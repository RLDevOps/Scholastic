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
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core.gui.components.table;

import java.util.Locale;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * Description: <BR>
 * classes implementing this interface can determine how cells of a table look. right now, if action != null, the whole cell (the value the method render of this class
 * produces) is embraced by an <a href... tag. TODO: let the renderer access the prebuilt uri for having this limitation removed
 * <P/>
 * 
 * @author Felix Jost
 */

public interface CustomCellRenderer {

	/**
	 * this method is called by CustomRenderColumnDescriptor.
	 * 
	 * @param sb the StringOuput to put the rendering into
	 * @param renderer the renderer to use
	 * @param val the value from the tablemodel to be rendered.
	 * @param locale the locale to use
	 * @param alignment the alignment (see ColumnDescriptor.ALIGNMENT...)
	 * @param action the action. if not null, then the output should be clickable and produce a command uri with action
	 */
	void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action);

}