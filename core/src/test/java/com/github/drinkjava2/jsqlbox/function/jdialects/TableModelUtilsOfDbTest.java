package com.github.drinkjava2.jsqlbox.function.jdialects;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TableModelUtilsOfDbTest extends TestBase {
	{
		regTables(studentSample.class, DbSample.class);
	}

	@Table(name = "student_sample")
	public static class studentSample {
		@Id
		String stName;

		@Id
		String stAddr;

		public String getStName() {
			return stName;
		}

		public void setStName(String stName) {
			this.stName = stName;
		}

		public String getStAddr() {
			return stAddr;
		}

		public void setStAddr(String stAddr) {
			this.stAddr = stAddr;
		}

	}

	@FKey(name = "fkey1", ddl = true, columns = { "name", "address" }, refs = { "student_sample", "stName", "stAddr" })
	public static class DbSample {
		@Id
		@UUID25
		String id;

		@Id
		String name;

		String address;

		String email;

		@SingleFKey(refs = { "student_sample", "stAddr" })
		String address2;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getAddress2() {
			return address2;
		}

		public void setAddress2(String address2) {
			this.address2 = address2;
		}

	}

	public static class DbSample2 {
		@Id
		String id;

	}

	@Test
	public void doDbToModelTest() throws Exception {
		DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
		Connection conn = null;
		conn = ds.getConnection();
		Dialect dialect = Dialect.guessDialect(conn);
		TableModel[] models = TableModelUtils.db2Models(conn, dialect);
		for (TableModel model : models) {
			Systemout.println("\n\n\n\n");
			Systemout.println(TableModelUtils.model2JavaSrc(model, true, true, "somepackage"));
		}
		conn.close();
	}

	@Test
	public void doDbToJavaSrcFiles() {
		DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
		TableModelUtils.db2JavaSrcFiles(ds, Dialect.MySQLDialect, true, true, "temp", "c:/temp");
	}

}
