/*
 * Copyright (C) 2012 the diamond:dogs|group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.diamonddogs.data.adapter.database;

import java.util.Arrays;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Pair;

/**
 * A Query implementation to be used in conjunction with {@link DatabaseAdapter}
 */
public class Query {

	/**
	 * Optional Content Uri
	 */
	public Uri uri;
	/**
	 * An array containing the field names of all where parameters
	 */
	public String[] whereFields;
	/**
	 * An array containing the values to be checked
	 */
	public String[] whereValues;
	/**
	 * An array containing all operators for corresponding whereValues and
	 * whereFields
	 */
	public String[] whereOperators;
	/**
	 * An array containing AND or OR operators.
	 */
	public String[] whereConditionOperators;
	/**
	 * Projection array
	 */
	public String[] projection = null;
	/**
	 * A sortorder
	 */
	public String sortOrder = null;

	private Pair<String, Boolean> validate() {
		// TODO: not entirly corrent -> what if one of the two is null and the
		// other isn't

		if ((whereFields != null && whereFields != null) && (whereFields.length != whereValues.length)) {
			return new Pair<String, Boolean>("whereFields / whereValues length mismatch", false);
		}
		if (whereOperators != null && whereOperators.length != whereFields.length) {
			return new Pair<String, Boolean>("whereField / whereOperators length mismatch", false);
		}
		if (whereConditionOperators != null && whereConditionOperators.length != (whereFields.length - 1)) {
			return new Pair<String, Boolean>("whereConditionOperators / whereFields mismatch", false);
		}
		return new Pair<String, Boolean>("", true);
	}

	/**
	 * Create the selection parameter for
	 * {@link ContentResolver#query(android.net.Uri, String[], String, String[], String)}
	 * 
	 * @return a {@link String} containing the where clause
	 */
	public String createSelection() {
		Pair<String, Boolean> valid = validate();
		if (!valid.second) {
			throw new RuntimeException("Query invalid: " + valid.first);
		}
		if (whereFields == null && whereValues == null) {
			return null;
		}

		if (whereOperators == null) {
			whereOperators = new String[whereFields.length];
			Arrays.fill(whereOperators, 0, whereOperators.length, "=");
		}

		String ret = "";
		for (int i = 0; i < whereFields.length; i++) {
			if (whereOperators[i].equalsIgnoreCase("like")) {
				ret += whereFields[i] + " " + whereOperators[i] + "'%' || ? || '%'";
			} else {
				ret += whereFields[i] + whereOperators[i] + "?";
			}
			if ((i + 1) < whereFields.length) {
				if (whereConditionOperators == null) {
					ret += " AND ";
				} else {
					ret += " " + whereConditionOperators[i] + " ";
				}
			}
		}
		return ret;

	}
}
