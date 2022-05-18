package com.github.drinkjava2.jsqlbox.function;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.IGNORE_NULL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.TAIL;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * CRUD test for entity have Compound Id
 * 
 * @since 1.7.0
 */
public class CrudCompoundIdTest extends TestBase {
	{
		regTables(CpdUser.class);
	}

	// name and sex are compound primary keys
	public static class CpdUser extends ActiveRecord<CpdUser> {
		@Id
		String name;

		@Id
		Boolean sex;// true male, false female

		String address;

		Integer age;

		public CpdUser() {
		}

		public CpdUser(String name, Boolean sex, String address) {
			this.name = name;
			this.sex = sex;
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Boolean getSex() {
			return sex;
		}

		public void setSex(Boolean sex) {
			this.sex = sex;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}

	/**
	 * Test below CRUD methods:cc
	 * 
	 * insert,update,tryUpdate,delete,tryDelete,deleteById,tryDeleteById,load,
	 * tryLoad,loadById,tryLoadById,findAll,findAllByIds,countAll,exist,existById,
	 * findRelated
	 */
	@Test
	public void crudTest() {
		// ======insert
		CpdUser u1 = new CpdUser("Name1", true, "Address1");
		CpdUser u2 = new CpdUser("Name2", true, "Address2");
		CpdUser u3 = new CpdUser("Name3", true, "Address3");
		CpdUser u4 = new CpdUser("Name4", true, "Address4");
		ctx.eInsert(u1);
		ctx.eInsert(u2, IGNORE_NULL);
		u3.insert();
		u4.insert(IGNORE_NULL);

		// ======update
		u1.setAddress("NewAddress1");
		u2.setAddress("NewAddress2");
		u3.setAddress("NewAddress3");
		u4.setAddress("NewAddress4");
		ctx.eUpdate(u1);
		u2.update();
		Assert.assertEquals(1, ctx.eUpdateTry(u3));
		Assert.assertEquals(1, u4.updateTry());

		// =======load
		Assert.assertEquals("NewAddress1", ctx.eLoad(u1).getAddress());
		Assert.assertEquals("NewAddress2", u2.load().getAddress());
		Assert.assertEquals(1, ctx.eLoadTry(u3));
		Assert.assertEquals("NewAddress3", u3.getAddress());
		Assert.assertEquals(1, u4.loadTry());
		Assert.assertEquals("NewAddress4", u4.getAddress()); 

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("sex", true);
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CpdUser.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CpdUser.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(mp).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CpdUser.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CpdUser.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.loadByIdTry(u4).getAddress());

		// =======findAll
		Assert.assertEquals(4, ctx.eFindAll(CpdUser.class).size());
		Assert.assertEquals(4, new CpdUser().findAll().size());

		// ========findBySql
		Assert.assertEquals(4, ctx.iQueryForEntityList(CpdUser.class, "select * from CpdUser").size());
		Assert.assertEquals(4, u1.findBySQL("select * from CpdUser").size());

		// =======countAll
		Assert.assertEquals(4, ctx.eCountAll(CpdUser.class));
		Assert.assertEquals(4, new CpdUser().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.eExist(u1));
		Assert.assertEquals(true, u2.existId());
		
		// =======existStrict
		Assert.assertTrue(u1.existStrict());
		u1.setAge(1000);
		Assert.assertFalse(u1.existStrict());

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("sex", true);
		m1.put("name", "Name1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("sex", false);
		m2.put("name", "Name2");
		Assert.assertEquals(true, ctx.eExistById(CpdUser.class, m1));
		Assert.assertEquals(false, u1.existById(m2));

		// =======existById (id is Tail)
		CpdUser t1 = new CpdUser().putTail("name", "Name1", "sex", true);
		CpdUser t2 = new CpdUser().putTail("name", "Name2", "sex", true);
		Assert.assertEquals(true, ctx.eExistById(CpdUser.class, t1, TAIL));
		Assert.assertEquals(true, u1.existById(t2, TAIL));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.eExistById(CpdUser.class, u1));
		Assert.assertEquals(true, u1.existById(u2));

		// =======delete
		ctx.eDelete(u1);
		u2.delete();
		Assert.assertEquals(1, ctx.eDeleteTry(u3));
		Assert.assertEquals(0, ctx.eDeleteTry(u3));
		Assert.assertEquals(1, u4.deleteTry());
		Assert.assertEquals(0, u4.deleteTry());
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Map)
		new CpdUser("Name1", true, "Address1").insert();
		new CpdUser("Name2", true, "Address2").insert();
		new CpdUser("Name3", true, "Address3").insert();
		new CpdUser("Name4", true, "Address4").insert();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("sex", true);
		m.put("name", "Name1");
		ctx.eDeleteById(CpdUser.class, m);
		m.put("name", "Name2");
		u1.deleteById(m);
		m.put("name", "Name3");
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CpdUser.class, m));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CpdUser.class, m));
		m.put("name", "Name4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CpdUser("Name1", true, "Address1").insert();
		u2 = new CpdUser("Name2", true, "Address2").insert();
		u3 = new CpdUser("Name3", true, "Address3").insert();
		u4 = new CpdUser("Name4", true, "Address4").insert();
		ctx.eDeleteById(CpdUser.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CpdUser.class, u3));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CpdUser.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
