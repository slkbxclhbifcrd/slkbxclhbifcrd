package test.function_test.crud_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

import test.TestBase;
import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.User;

/**
 * This is to test jSqlBoxContext class
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ContextTest extends TestBase {

	/**
	 * Demo how to create context and use it
	 */
	@Test
	public void insertUser1() {
		HikariDataSource ds = new HikariDataSource();// Datasource pool setting
		DataSourceBox dsSetting = new DataSourceBox();
		ds.setUsername((String) dsSetting.getProperty("username"));
		ds.setPassword((String) dsSetting.getProperty("password"));
		ds.setJdbcUrl((String) dsSetting.getProperty("jdbcUrl"));
		ds.setDriverClassName((String) dsSetting.getProperty("driverClassName"));

		SqlBoxContext ctx = new SqlBoxContext(ds);// create a new context

		User u = ctx.createEntity(User.class);
		Assert.assertNotEquals(Dao.getDefaultContext(), u.box().getSqlBoxContext());

		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.USERNAME(), "=", q("User1")));
		ds.close();
	}

	// CtxBox is a SqlBoxContent singleton
	public static class AnotherSqlBoxContextBox extends BeanBox {
		public SqlBoxContext create() {
			SqlBoxContext ctx = new SqlBoxContext();
			ctx.setDataSource((DataSource) BeanBox.getBean(DataSourceBox.class));
			return ctx;
		}
	}

	/**
	 * Demo how to use IOC tool like BeanBox to create a context
	 */
	@Test
	public void insertFromAntoherContext() {
		SqlBoxContext ctx = BeanBox.getBean(AnotherSqlBoxContextBox.class);
		User u = ctx.createEntity(User.class);
		Assert.assertNotEquals(Dao.getDefaultContext(), u.box().getSqlBoxContext());
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.insert();
		Assert.assertEquals(111, (int) Dao.queryForInteger("select ", u.PHONENUMBER(), " from ", u.table(), " where ",
				u.USERNAME(), "=", q("User1")));
	}

	/**
	 * Test dynamic bind context at runtime, first on board, then buy ticket
	 */
	@Test
	public void dynamicBindContext() {
		Dao.getDefaultContext().setShowSql(true);
		User u = new User();
		u.setUserName("Sam");
		u.setAddress("BeiJing");
		u.insert();

		Dao.executeQuiet("drop table users2");
		Dialect d = Dao.getDialect();
		String ddl = "create table users2 " //
				+ "(" + d.VARCHAR("id", 32) //
				+ "," + d.VARCHAR("username", 50) //
				+ "," + d.VARCHAR("newAddress", 50) //
				+ "," + d.INTEGER("Age") //
				+ ")" + d.engine();
		Dao.execute(ddl);
		Dao.refreshMetaData();

		SqlBoxContext ctx = BeanBox.getBean(AnotherSqlBoxContextBox.class);
		SqlBox box = new SqlBox(ctx);
		box.configTable("users2");
		box.configColumnName(u.fieldID(u.ADDRESS()), "newAddress");
		ctx.bind(u, box); // u be bound to new context ctx

		// Or use below:
		// u.box().setContext(ctx);
		// u.box().configColumnName(u.fieldID(u.ADDRESS()), "newAddress");
		// u.box().configTable("Users2");

		u.insert();
		Assert.assertEquals("BeiJing",
				Dao.queryForString("select ", u.ADDRESS(), " from ", u.table(), " where ", u.USERNAME("Sam"), "=?"));
		Assert.assertNotEquals(Dao.getDefaultContext(), u.box().getSqlBoxContext());
		Dao.executeQuiet("drop table users2");
	}

}