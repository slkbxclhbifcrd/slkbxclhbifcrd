package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.createAliasProxy;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.table;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giExecute;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQuery;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForMapList;

import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.LambdSqlItemPreparer.ALIAS;
import com.github.drinkjava2.jsqlbox.LambdSqlItemPreparer.COL;
import com.github.drinkjava2.jsqlbox.LambdSqlItemPreparer.C_ALIAS;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * This is a demo shows how to write refactor-support SQL in Java8 by using
 * Lambda
 */

public class Java8EampleTest {

	@Before
	public void init() {
		SqlBoxContext ctx = new SqlBoxContext(JdbcConnectionPool
				.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", ""));
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		for (String ddl : ctx.toCreateDDL(User.class))
			giExecute(ddl);
		for (int i = 0; i < 100; i++)
			new User().put("name", "Foo" + i, "age", i).insert();
		Assert.assertEquals(100, giQueryForLongValue("select count(*) from usertb"));
	}

	@After
	public void cleanup() {
		for (String ddl : gctx().toDropDDL(User.class))
			gctx().iExecute(ddl);
	}
 
	@Test
	public void lambdaTest() {
		Assert.assertEquals(100, giQueryForLongValue("select count(*) from usertb"));

		List<User> totalUsers = giQuery(new EntityListHandler(User.class), "select * from usertb");
		Assert.assertEquals(100, totalUsers.size());

		User u = createAliasProxy(User.class);
		List<?> list1 = giQueryForMapList( //
				"select "//
				, (ALIAS) u::getId//
				, (C_ALIAS) u::getAddress //
				, (C_ALIAS) u::getName //
				, " from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list1.size());

		u = createAliasProxy(User.class, "u");
		List<User> list2 = giQuery(new EntityListHandler(User.class) //
				, "select * from ", table(u), " where "//
				, (COL) u::getName, ">=?", param("Foo90") //
				, " and ", (COL) u::getAge, ">?", param(1) //
		);
		Assert.assertEquals(10, list2.size());
	}

}