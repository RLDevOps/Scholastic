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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.commons.controllers.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * The Month model
 * <P>
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author gwassmann
 */
public class Month {

	private int month;
	private List<Dated> items;

	/**
	 * Constructor
	 * 
	 * @param month
	 */
	Month(int month) {
		this.month = month;
		items = new ArrayList<Dated>();
	}

	/**
	 * @return The items
	 */
	List<? extends Dated> getItems() {
		return items;
	}

	/**
	 * @param item The item to add
	 */
	void add(Dated item) {
		items.add(item);
	}

	/**
	 * @param item The item to remove
	 */
	void remove(Dated item) {
		items.remove(item);
	}

	/**
	 * @return The number of items in this month
	 */
	public int itemsCount() {
		return items.size();
	}

	/**
	 * @return The number of the month as string
	 */
	public String getName() {
		return Integer.toString(month);
	}

	/**
	 * @return The month
	 */
	int getMonth() {
		return month;
	}
}
