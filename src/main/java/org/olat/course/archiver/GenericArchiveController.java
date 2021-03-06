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

package org.olat.course.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.NodeTableDataModel;
import org.olat.course.nodes.CourseNode;
import org.olat.user.UserManager;

/**
 * @author schnider Comment: Archives the User selected wiki's to the personal folder of this user.
 */
public class GenericArchiveController extends BasicController {

	private static final String CMD_SELECT_NODE = "cmd.select.node";
	private final Panel main;
	private final VelocityContainer nodeChoose;
	private NodeTableDataModel nodeTableModel;
	private TableController nodeListCtr;
	protected CourseNode currentCourseNode;
	private final CourseNode nodeType;
	protected OLATResourceable ores;

	/**
	 * Constructor for the assessment tool controller.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public GenericArchiveController(final UserRequest ureq, final WindowControl wControl, final OLATResourceable ores, final CourseNode nodeType) {
		super(ureq, wControl);

		this.ores = ores;
		this.nodeType = nodeType;
		main = new Panel("main");
		nodeChoose = createVelocityContainer("nodechoose");
		nodeChoose.contextPut("nodeType", nodeType.getType());
		doNodeChoose(ureq);
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Component source, final Event event) {
		// no interesting events
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(final UserRequest ureq, final Controller source, final Event event) {
		if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				final ICourse course = CourseFactory.loadCourse(ores);
				final TableEvent te = (TableEvent) event;
				final String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					final int rowid = te.getRowId();
					final Map nodeData = (Map) nodeTableModel.getObject(rowid);
					final CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = node;
					final boolean successfullyArchived = archiveNode(ureq);
					if (successfullyArchived) {
						showInfo("archive." + nodeType.getType() + ".successfully");
					} else {
						showWarning("archive." + nodeType.getType() + ".notsuccessfully");
					}
				}
			}
		}
	}

	/**
	 * @param ureq
	 */
	private void doNodeChoose(final UserRequest ureq) {
		// table configuraton
		final TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);

		removeAsListenerAndDispose(nodeListCtr);
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);

		// table columns
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT,
				new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, ureq.getLocale()));

		// get list of course node data and populate table data model
		final ICourse course = CourseFactory.loadCourse(ores);
		final CourseNode rootNode = course.getRunStructure().getRootNode();
		final List nodesTableObjectArrayList = addNodesAndParentsToList(0, rootNode);

		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			nodeChoose.contextPut("hasNodes", Boolean.FALSE);
		} else {
			nodeChoose.contextPut("hasNodes", Boolean.TRUE);
			nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
		}

		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);

	}

	/**
	 * Recursive method that adds nodes and all its parents to a list
	 * 
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	@SuppressWarnings("unchecked")
	private List addNodesAndParentsToList(final int recursionLevel, final CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		final List childrenData = new ArrayList();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			final CourseNode child = (CourseNode) courseNode.getChildAt(i);
			final List childData = addNodesAndParentsToList((recursionLevel + 1), child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}

		final String nodent = nodeType.getType();
		if (childrenData.size() > 0 || courseNode.getType().equals(nodeType.getType())) {
			// Store node data in map. This map array serves as data model for
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			final Map nodeData = new HashMap();
			// indent
			nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
			// course node data
			nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
			nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
			nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
			nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());

			if (courseNode.getType().equals(nodeType.getType())) {
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
			} else {
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
			}

			final List nodeAndChildren = new ArrayList();
			nodeAndChildren.add(nodeData);

			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}

	protected boolean archiveNode(final UserRequest ureq) {
		final ICourse course = CourseFactory.loadCourse(ores);
		final File exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		final UserManager um = UserManager.getInstance();
		final String charset = um.getUserCharset(ureq.getIdentity());
		final boolean successfullyArchived = currentCourseNode.archiveNodeData(ureq.getLocale(), course, exportDir, charset);
		return successfullyArchived;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}

}
