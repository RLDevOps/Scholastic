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
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.logging;

/**
 * Description:<br>
 * TODO:
 * <P>
 * Initial Date: 01.11.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface OLog {

	public boolean isDebug();

	public abstract void error(String logMsg, Throwable cause);

	public abstract void error(String logMsg);

	/**
	 * See package.html for proper usage!
	 */
	public abstract void warn(String logMsg, Throwable cause);

	/**
	 * See package.html for proper usage!
	 */
	public abstract void warn(String logMsg);

	/**
	 * Add debug log entry. Always use together with if (log.isDebug()) log.debug(...) to let the compiler optimize it for a performance gain
	 */
	public abstract void debug(String logMsg, String userObj);

	/**
	 * Add debug log entry
	 */
	public abstract void debug(String logMsg);

	public abstract void info(String logMsg, String userObject);

	public abstract void info(String logMsg);

	/**
	 * Add audit log entry.
	 */
	public abstract void audit(String logMsg);

	/**
	 * Add audit log entry with a user object.
	 */
	public abstract void audit(String logMsg, String userObj);

}