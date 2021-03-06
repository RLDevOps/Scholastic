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

package org.olat.course.assessment;

import java.util.List;
import java.util.Map;

/**
 * Description:<br>
 * Data structure that represents the users current state of a course. All assessment information are beeing kept in the efficiency statement as an course independet
 * list. This means, that when deleting a course, the course efficiency statement is still available to users who participated in this course.
 * <P>
 * Initial Date: 11.08.2005 <br>
 * 
 * @author gnaegi
 */
public class EfficiencyStatement {
	private List<Map<String, Object>> assessmentNodes;
	private String courseTitle;
	private Long courseRepoEntryKey;
	private String displayableUserInfo;
	private long lastUpdated;

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(final long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<Map<String, Object>> getAssessmentNodes() {
		return assessmentNodes;
	}

	public void setAssessmentNodes(final List<Map<String, Object>> assessmentNodes) {
		this.assessmentNodes = assessmentNodes;
	}

	public Long getCourseRepoEntryKey() {
		return courseRepoEntryKey;
	}

	public void setCourseRepoEntryKey(final Long courseRepoEntryKey) {
		this.courseRepoEntryKey = courseRepoEntryKey;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(final String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getDisplayableUserInfo() {
		return displayableUserInfo;
	}

	public void setDisplayableUserInfo(final String displayableUserInfo) {
		this.displayableUserInfo = displayableUserInfo;
	}

}
