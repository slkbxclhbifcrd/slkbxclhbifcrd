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
package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jdbpro.JDBPRO.USE_BOTH;
import static com.github.drinkjava2.jdbpro.JDBPRO.USE_MASTER;
import static com.github.drinkjava2.jdbpro.JDBPRO.USE_SLAVE;
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.iQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardDB;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardTB;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardTable;
import com.github.drinkjava2.jdialects.annotation.jdia.Snowflake;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Shard according mod strategy test
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ShardingModToolTest {

	final static int MASTER_DATABASE_QTY = 7;
	final static int SLAVE_DATABASE_QTY = 7; // each master has 7 slaves
	final static int TABLE_QTY = 8; // each table has 8 sharding

	SqlBoxContext[] masters = new SqlBoxContext[MASTER_DATABASE_QTY];

	public static class TheUser extends ActiveRecord<TheUser> {
		@ShardTable({ "MOD", "8" })
		@Snowflake
		@Id
		private Long id;

		private String name;

		@Snowflake
		@ShardDatabase({ "MOD", "7" })
		@Id
		private Long databaseId;

		//@formatter:off
		public Long getId() {return id;}
		public void setId(Long id) {this.id = id;}
		public String getName() {return name;}
		public void setName(String name) {this.name = name;}
		public Long getDatabaseId() {return databaseId;}
		public void setDatabaseId(Long databaseId) {this.databaseId = databaseId; } 
		//@formatter:on
	}

	@SuppressWarnings("deprecation")
	@Before
	public void init() {
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			SqlBoxContext[] slaves = new SqlBoxContext[SLAVE_DATABASE_QTY];
			masters[i] = new SqlBoxContext(TestBase.createH2_HikariDataSource("masters" + i));
			masters[i].setMasters(masters);
			masters[i].setSlaves(slaves);
			masters[i].setSnowflakeCreator(new SnowflakeCreator(5, 5, 0, i));
			masters[i].setName("Master" + i);
			for (int j = 0; j < SLAVE_DATABASE_QTY; j++)
				slaves[j] = new SqlBoxContext(TestBase.createH2_HikariDataSource("SlaveDB" + i + "_" + j));
		}

		TableModel model = TableModelUtils.entity2Model(TheUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {// Create master/salve tables
				model.setTableName("TheUser" + "_" + j);
				for (String ddl : masters[i].getDialect().toCreateDDL(model))
					masters[i].iExecute(ddl, USE_BOTH);
			}
		}
	}

	@After
	public void cleanup() {
		TableModel model = TableModelUtils.entity2Model(TheUser.class);
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			for (int j = 0; j < TABLE_QTY; j++) {
				model.setTableName("TheUser" + "_" + j);
				for (String ddl : masters[i].getDialect().toDropDDL(model))
					masters[i].iExecute(ddl, USE_BOTH);
			}
		}
		for (int i = 0; i < MASTER_DATABASE_QTY; i++) {
			((HikariDataSource) masters[i].getDataSource()).close();
			for (int j = 0; j < SLAVE_DATABASE_QTY; j++)
				((HikariDataSource) masters[i].getSlaves()[j].getDataSource()).close();
		}
	}

	@Test
	public void testInsertSQLs() {
		masters[2].iExecute(TheUser.class, "insert into ", shardTB(10), shardDB(3),
				" (id, name, databaseId) values(?,?,?)", param(10, "u1", 3), USE_BOTH, new PrintSqlHandler());
		Assert.assertEquals(1, masters[2].iQueryForLongValue(TheUser.class, "select count(*) from ", shardTB(10),
				shardDB(3), USE_SLAVE, new PrintSqlHandler()));
		Assert.assertEquals(1,
				masters[2].iQueryForLongValue(TheUser.class, "select count(*) from ", shardTB(10), shardDB(3)));
	}

	@Test
	public void testActiveRecord() {// issue XA or TCC transaction needed
		SqlBoxContext.setGlobalSqlBoxContext(masters[4]);// random select one

		// Don't know saved to where
		TheUser u1 = new TheUser().put("name", "Tom").insert(USE_BOTH, new PrintSqlHandler());
		Assert.assertEquals(u1.shardDB(), masters[4].getShardedDB(TheUser.class, u1.getDatabaseId()));
		Assert.assertEquals(u1.shardTB(), masters[4].getShardedTB(TheUser.class, u1.getId()));

		u1.setName("Sam");
		u1.update(USE_BOTH, new PrintSqlHandler());

		TheUser u2 = new TheUser();
		u2.setId(u1.getId());
		u2.setDatabaseId(u1.getDatabaseId());
		u2.load(new PrintSqlHandler(), " and name=?", param("Sam")); // use slave
		Assert.assertEquals("Sam", u2.getName());

		u2.delete(new PrintSqlHandler());// only deleted master
		Assert.assertEquals(0, iQueryForLongValue("select count(*) from ", u2.shardTB(), u2.shardDB(), USE_MASTER));
		Assert.assertEquals(1, iQueryForLongValue("select count(*) from ", u2.shardTB(), u2.shardDB()));// slave exist
	}

}