package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.common.DataSourceConfig.HikariCPBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxAOP;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Annotation GroupTx unit Test
 *
 * @author Yong Zhu
 * @since 2.0
 */
public class GroupShardTxTest {
	DbContext ctx1 = JBEANBOX.getBean(DbContextBox1.class);
	DbContext ctx2 = JBEANBOX.getBean(DbContextBox2.class);

	public static class ShardUser extends ActiveRecord<ShardUser> {
		@Id
		@ShardDatabase({ "MOD", "2" })
		Integer id; //Note: sharding should not use Identity because 
		String name;

		//@formatter:off
		public Integer getId() {return id;}
		public ShardUser setId(Integer id) {	this.id = id; return this;}
		public String getName() {return name;}
		public ShardUser setName(String name) {this.name = name;return this;}		 
		//@formatter:on
	}

	@Before
	public void init() {
		String[] ddlArray = ctx1.toDropAndCreateDDL(ShardUser.class);
		for (String ddl : ddlArray) {
			ctx1.exe(ddl);
			ctx2.exe(ddl);
		}
		DbContext[] masters = new DbContext[] { ctx1, ctx2 };
		masters[0].setName("0");
		masters[1].setName("1");
		DbContext.getGlobalDbContext().setMasters(masters);
		
		for (int i = 1; i <= 100; i++)
			new ShardUser().setId(i).setName("Foo" + i).insert(); // Sharded!

		Assert.assertEquals(50, ctx1.entityCount(ShardUser.class));
		Assert.assertEquals(50, ctx2.entityCount(ShardUser.class));
	}

	@After
	public void clean() {
		JBEANBOX.close();
	}

	@GrpTX
	public void groupCommitTest() { // test group commit
		new ShardUser().setId(200).setName("Foo").insert();
		new ShardUser().setId(201).setName("Bar").insert();
	}

	@Test
	public void testCommit() {
		GroupShardTxTest t = JBEANBOX.getInstance(GroupShardTxTest.class);
		t.groupCommitTest();
		Assert.assertEquals(51, ctx1.entityCount(ShardUser.class));
		Assert.assertEquals(51, ctx2.entityCount(ShardUser.class));
	}

	@GrpTX
	public void groupRollbackTest() { // test group roll back
		new ShardUser().setId(300).setName("Foo").insert();
		new ShardUser().setId(301).setName("Bar").insert();
		Assert.assertEquals(51, ctx1.entityCount(ShardUser.class));
		Assert.assertEquals(51, ctx2.entityCount(ShardUser.class));
		Systemout.println(1 / 0); // div 0!
	}

	@Test
	public void testRollback() {
		GroupShardTxTest t = JBEANBOX.getInstance(GroupShardTxTest.class);
		try {
			t.groupRollbackTest();
		} catch (Exception e) {
			Systemout.println(e.getMessage());
		}
		Assert.assertEquals(50, ctx1.entityCount(ShardUser.class));
		Assert.assertEquals(50, ctx2.entityCount(ShardUser.class));
	}

	@GrpTX
	public void groupPartialCommitTest() { // simulate partial commit test
		new ShardUser().setId(400).setName("Foo").insert();
		Assert.assertEquals(51, ctx1.entityCount(ShardUser.class));
		new ShardUser().setId(401).setName("Bar").insert();
		Assert.assertEquals(51, ctx2.entityCount(ShardUser.class));
		((HikariDataSource) ctx2.getDataSource()).close();// DS2 is closed, this will cause ctx1 fail
	}

	@Test
	public void testPartialCommit() {// partial commit
		GroupShardTxTest t = JBEANBOX.getInstance(GroupShardTxTest.class);
		try {
			t.groupPartialCommitTest();
		} catch (Exception e) {
			// e.printStackTrace();
			Systemout.println(e.getMessage());
		}
		Assert.assertEquals(51, ctx1.entityCount(ShardUser.class));
	}

	// ========== Singleton settings =======================

	public static class Ds1 extends HikariCPBox {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:GpShardDs1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			injectValue("driverClassName", "org.h2.Driver");
			injectValue("username", "sa");
			injectValue("password", "");
		}
	}

	public static class Ds2 extends Ds1 {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:GpShardDs2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		}
	}

	public static class DbContextBox1 extends BeanBox {
		public Object create() {
			DbContext ctx = new DbContext((DataSource) JBEANBOX.getBean(Ds1.class));
			ctx.setConnectionManager(GroupTxConnectionManager.instance());
			return ctx;
		}
	}

	public static class DbContextBox2 extends BeanBox {
		public Object create() {
			DbContext ctx = new DbContext((DataSource) JBEANBOX.getBean(Ds2.class));
			ctx.setConnectionManager(GroupTxConnectionManager.instance());
			return ctx;
		}
	}

	// ========== AOP settings =======================
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface GrpTX {
		public Class<?> value() default GroupTxAOP.class;
	}

	public static void a(String f, Object... ss) {

		for (Object s : ss) {
			Systemout.println(s);
		}
	}
}