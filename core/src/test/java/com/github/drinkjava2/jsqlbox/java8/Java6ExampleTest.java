package com.github.drinkjava2.jsqlbox.java8;

import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.alias;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.c_alias;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.clean;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.col;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.createAliasProxy;
import static com.github.drinkjava2.jsqlbox.AliasProxyUtil.table;
import static com.github.drinkjava2.jsqlbox.DB.exe;
import static com.github.drinkjava2.jsqlbox.DB.gctx;
import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.qry;
import static com.github.drinkjava2.jsqlbox.DB.qryLongValue;
import static com.github.drinkjava2.jsqlbox.DB.qryMapList;

import java.util.List;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * This is a demo shows how to write refactor-support SQL in Java6 and Java7.
 */
public class Java6ExampleTest {

	@Before
	public void init() {
		DbContext ctx = new DbContext(JdbcConnectionPool
				.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", ""));
		DbContext.setGlobalDbContext(ctx);
		for (String ddl : ctx.toCreateDDL(User.class))
			exe(ddl);
		for (int i = 0; i < 100; i++)
			new User().putField("name", "Foo" + i, "age", i).insert();
		Assert.assertEquals(100, qryLongValue("select count(*) from usertb"));
	}

	@After
	public void cleanup() {
		for (String ddl : gctx().toDropDDL(User.class))
			gctx().exe(ddl);
	}

	@Test
	public void normalTest() {
		// ctx.setAllowShowSQL(true);
		List<User> totalUsers = qry(new EntityListHandler(), User.class, "select * from usertb");
		Assert.assertEquals(100, totalUsers.size());

		User u = createAliasProxy(User.class);
		List<Map<String, Object>> list = qryMapList(clean(), //
				"select "//
				, alias(u.getId())//
				, c_alias(u.getAddress())//
				, c_alias(u.getName())//
				, " from ", table(u), " where "//
				, col(u.getName()), ">=?", par("Foo90") //
				, " and ", col(u.getAge()), ">?", par(1) //
		);
		Assert.assertEquals(10, list.size());

		u = createAliasProxy(User.class, "u");
		List<User> list2 = qry(new EntityListHandler(), User.class, clean(), //
				"select * from ", table(u), " where "//
				, col(u.getName()), ">=?", par("Foo90") //
				, " and ", col(u.getAge()), ">?", par(1) //
		);
		Assert.assertEquals(10, list2.size());
	}
}