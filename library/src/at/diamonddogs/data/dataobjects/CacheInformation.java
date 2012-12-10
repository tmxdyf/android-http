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
package at.diamonddogs.data.dataobjects;

import at.diamonddogs.contentprovider.CacheContentProvider;

/**
 * Represents {@link CacheInformation} of a file. Is used by
 * {@link CacheContentProvider}.
 */
public class CacheInformation {

	/** No caching */
	public static final long CACHE_NO = -1;
	/** 1 minute caching */
	public static final long CACHE_1M = 60000l;
	/** 1 hour caching */
	public static final long CACHE_1H = 3600000;
	/** 1 day caching */
	public static final long CACHE_24H = 86400000;
	/** 7 days caching */
	public static final long CACHE_7D = 604800000;
	/** 1 month caching */
	public static final long CACHE_1MO = 2419200000l;
	/** Cache forever */
	public static final long CACHE_FOREVER = -2;

	/**
	 * Primary key, will be -1 if not assigned
	 */
	private int _id = -1;

	/**
	 * Creation time stamp of the cache file
	 */
	private long creationTimeStamp;

	/**
	 * Time after which a file will be deleted
	 */
	private long cacheTime;

	/**
	 * Must be the md5 hash of the URL containing the original data.
	 * Utils.getMD5Hash(urlString)
	 */
	private String fileName;

	/**
	 * The path to the root directory of the file
	 */
	private String filePath;

	/**
	 * Constructor to privide all information on {@link CacheInformation}
	 * 
	 * @param creationTimeStamp
	 * @param cacheTime
	 * @param fileName
	 * @param filePath
	 */
	public CacheInformation(long creationTimeStamp, long cacheTime, String fileName, String filePath) {
		this.creationTimeStamp = creationTimeStamp;
		this.cacheTime = cacheTime;
		this.fileName = fileName;
		this.filePath = filePath;
	}

	/**
	 * Default constructor
	 */
	public CacheInformation() {

	}

	@SuppressWarnings("javadoc")
	public int get_id() {
		return _id;
	}

	@SuppressWarnings("javadoc")
	public void set_id(int _id) {
		this._id = _id;
	}

	@SuppressWarnings("javadoc")
	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}

	@SuppressWarnings("javadoc")
	public void setCreationTimeStamp(long creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	@SuppressWarnings("javadoc")
	public long getCacheTime() {
		return cacheTime;
	}

	@SuppressWarnings("javadoc")
	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	@SuppressWarnings("javadoc")
	public String getFileName() {
		return fileName;
	}

	@SuppressWarnings("javadoc")
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@SuppressWarnings("javadoc")
	public String getFilePath() {
		return filePath;
	}

	@SuppressWarnings("javadoc")
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
