package com.github.drinkjava2.functionstest.jtransactions;

import static com.github.drinkjava2.jbeanbox.JBEANBOX.inject;
import static com.github.drinkjava2.jbeanbox.JBEANBOX.value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * TinyTx is a tiny and clean declarative transaction tool, in this unit test
 * use jBeanBox's annotation configuration.
 * 
 * To make jSqlBox core unit test clean, I put Spring TX demos in jSqlBox's demo
 * folder.
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyTxTest {
	SqlBoxContext ctx;
	{
		SqlBoxContext.resetGlobalVariants();
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setConnectionManager(TinyTxConnectionManager.instance());
		ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class), config);
	}

	@TX
	public void tx_Insert1() {
		ctx.nExecute("insert into user_tb (id) values('123')");
	}

	@TX
	public void tx_Insert2() {
		ctx.nExecute("insert into user_tb (id) values('456')");
		Assert.assertEquals(2, ctx.nQueryForLongValue("select count(*) from user_tb "));
		System.out.println("Now have 2 records in user_tb, but will roll back to 1");
		System.out.println(1 / 0);
	}

	@Test
	public void doTest() {
		TinyTxTest tester = BeanBox.getBean(TinyTxTest.class);
		ctx.quiteExecute("drop table user_tb");
		ctx.nExecute("create table user_tb (id varchar(40))engine=InnoDB");

		Assert.assertEquals(0L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		try {
			tester.tx_Insert1();// this one inserted 1 record
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			tester.tx_Insert2();// this one did not insert, roll back
		} catch (Exception e) {
			// e.printStackTrace();
			Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));
			System.out.println("div/0 exception found, tx_Insert2 should roll back");
		}
		Assert.assertEquals(1L, ctx.nQueryForLongValue("select count(*) from user_tb "));

		ctx.nExecute("drop table user_tb");
		JBEANBOX.close();// Release DataSource Pool
	}

	/*
	 * AOP is an annotation for annotation, the annotated annotation's value(target)
	 * is a class implemented AOP alliance's MethodInterceptor interface
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX {
		public Class<?> value() default TheTxBox.class;
	}

	public static class TheTxBox extends BeanBox {
		{
			this.injectConstruct(TinyTx.class, DataSource.class, Integer.class, inject(DataSourceBox.class),
					value(Connection.TRANSACTION_READ_COMMITTED));
		}
	}

}