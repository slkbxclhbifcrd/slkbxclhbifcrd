/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.gtx;

import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;

/**
 * GtxId used for databases to store a GtxId to tag a global transactions is
 * committed on this database
 */
public class GtxId {
	@Column(length = 32)
	@Id
	private String gid;

	private Integer unlockTry = 0; // unlockTry times

	public GtxId() {// default constructor
	}

	public GtxId(String gid) {// default constructor
		this.gid = gid;
	}

	public String getGid() {
		return gid;
	}

	public GtxId setGid(String gid) {
		this.gid = gid;
		return this;
	}

	public Integer getUnlockTry() {
		return unlockTry;
	}

	public GtxId setUnlockTry(Integer unlockTry) {
		this.unlockTry = unlockTry;
		return this;
	}

}