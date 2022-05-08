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
package com.github.drinkjava2.config;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Base class of unit test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class TestBase {
	protected DataSource dataSource;
	protected Dialect dialect;
	protected SqlBoxContext ctx;
	protected TableModel[] tablesForTest;

	@Before
	public void init() {
		SqlBoxContext.resetGlobalVariants();
		dataSource = BeanBox.getBean(DataSourceBox.class);
		dialect = Dialect.guessDialect(dataSource);
		Dialect.setGlobalAllowReservedWords(true);

		//SqlBoxContext.setGlobalNextAllowShowSql(true);
		ctx = new SqlBoxContext(dataSource); 
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		if (tablesForTest != null)
			createAndRegTables(tablesForTest);
	}

	@After
	public void cleanUp() {
		if (tablesForTest != null)
			dropTables(tablesForTest);
		tablesForTest = null;
		BeanBox.defaultContext.close(); // IOC tool will close dataSource
		SqlBoxContext.resetGlobalVariants();
	}

	public void executeDDLs(String[] ddls) {
		for (String sql : ddls)
			ctx.nExecute(sql);
	}

	public void quietExecuteDDLs(String[] ddls) {
		for (String sql : ddls) {
			try {
				ctx.nExecute(sql);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Register tables, create and drop will done by TestBase
	 */
	public void regTables(TableModel... tableModels) {
		this.tablesForTest = tableModels;
	}

	/**
	 * Register tables, create and drop will done by TestBase
	 */
	public void regTables(Class<?>... classes) {
		this.tablesForTest = TableModelUtils.entity2ReadOnlyModels(classes);
	}

	public void createAndRegTables(TableModel... tableModels) {
		this.tablesForTest = tableModels;
		createTables(tableModels);
	}

	public void createAndRegTables(Class<?>... classes) {
		this.tablesForTest = TableModelUtils.entity2ReadOnlyModels(classes);
		createTables(tablesForTest);
	}

	public void createTables(TableModel... tableModels) {
		String[] ddls = ctx.toCreateDDL(tableModels);
		executeDDLs(ddls);
	}

	public void createTables(Class<?>... classes) {
		String[] ddls = ctx.toCreateDDL(classes);
		executeDDLs(ddls);
	}

	public void dropTables(TableModel... tableModels) {
		String[] ddls = ctx.toDropDDL(tableModels);
		executeDDLs(ddls);
	}

	public void dropTables(Class<?>... classes) {
		String[] ddls = ctx.toDropDDL(classes);
		executeDDLs(ddls);
	}

	public static void printTimeUsed(long startTimeMillis, String msg) {
		System.out.println(String.format("%50s: %7s s", msg, (System.currentTimeMillis() - startTimeMillis) / 1000.0));
	}

	public static HikariDataSource createH2_HikariDataSource(String h2DbName) {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:" + h2DbName + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(8);
		ds.setConnectionTimeout(2000);
		return ds;
	}

}