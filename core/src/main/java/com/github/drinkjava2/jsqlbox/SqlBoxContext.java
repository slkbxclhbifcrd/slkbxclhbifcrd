/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.Dialect;

/**
 * SqlBoxContext is extended from DbContext
 * 
 * @author Yong Zhu
 * @since 1.0.0
 * @Deprecated From V4.0 Suggest use DbContext
 */
@Deprecated
public class SqlBoxContext extends DbContext {//NOSONAR
	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds, dialect);
	}

	public static void setGlobalSqlBoxContext(DbContext dc) {
		DbContext.setGlobalDbContext(dc);
	}
}