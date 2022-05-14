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

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.valuesQuestions;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardDB;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.shardTB;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.drinkjava2.jdbpro.LinkStyleArrayList;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SingleTonHandlers;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.IdentityIdGenerator;
import com.github.drinkjava2.jdialects.id.SnowflakeCreator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.entitynet.EntityIdUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;
import com.github.drinkjava2.jsqlbox.sharding.ShardingTool;
import com.github.drinkjava2.jsqlbox.sqlitem.EntityKeyItem;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

/**
 * SqlBoxContextUtils is utility class store static methods about SqlBoxContext
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
/**
 * @author Yong Zhu
 * @since 1.7.0
 */
public abstract class SqlBoxContextUtils {// NOSONAR
	/**
	 * Read database Meta info into SqlBox[]
	 */
	public static TableModel[] loadMetaTableModels(SqlBoxContext ctx, Dialect dialect) {
		Connection con = null;
		SQLException sqlException = null;
		try {
			con = ctx.prepareConnection();
			return TableModelUtils.db2Models(con, dialect);
		} catch (SQLException e) {
			sqlException = e;
		} finally {
			try {
				ctx.close(con);
			} catch (SQLException e) {
				if (sqlException != null)
					sqlException.setNextException(e);
				else
					sqlException = e;
			}
		}
		throw new SqlBoxException(sqlException);
	}

	/**
	 * Use current SqlBoxContext's shardingTools to calculate the real shardTable
	 * name
	 */
	public static String getShardedTB(SqlBoxContext ctx, Object entityOrClass, Object... shardKey) {
		if (ctx.getShardingTools() == null || ctx.getShardingTools().length == 0)
			throw new SqlBoxException("No shardingTools be set.");
		TableModel model = SqlBoxContextUtils.findEntityOrClassTableModel(entityOrClass);
		String table = null;
		for (ShardingTool sh : ctx.getShardingTools()) {
			String[] result = sh.handleShardTable(ctx, model, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException(
							"Can not find sharding table for target '" + model.getEntityClass() + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 sharding tables for target '" + model.getEntityClass()
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardTable search condition");
				table = result[0];
				break;
			}
		}
		return table;
	}

	/**
	 * Use current SqlBoxContext's shardingTools to calculate the master
	 * SqlBoxContext
	 */
	public static SqlBoxContext getShardedDB(SqlBoxContext currentCtx, Object entityOrClass, Object... shardKey) {
		if (currentCtx.getMasters() == null || currentCtx.getMasters().length == 0)
			throw new SqlBoxException(
					"Current SqlBoxContext did not set masters property but try do shardDatabase opertation.");
		if (currentCtx.getShardingTools() == null || currentCtx.getShardingTools().length == 0)
			throw new SqlBoxException("No shardingTools be set.");
		TableModel model = SqlBoxContextUtils.findEntityOrClassTableModel(entityOrClass);
		SqlBoxContext masterCtx = null;
		for (ShardingTool sh : currentCtx.getShardingTools()) {
			SqlBoxContext[] result = sh.handleShardDatabase(currentCtx, model, shardKey);
			if (result != null) {
				if (result.length == 0)
					throw new SqlBoxException("Can not find master SqlBoxContext for '" + model.getEntityClass() + "'");
				if (result.length > 1)
					throw new SqlBoxException("Found more than 1 SqlBoxContext tables for target '"
							+ model.getEntityClass()
							+ "', jSqlBox current version do not support auto-join, to solve this issue you need adjust your ShardDatabase search condition.");
				masterCtx = result[0];
				break;
			}
		}
		return masterCtx;
	}

	/**
	 * if optionItems has a TabelModel, use it, otherwise find tablemodel from
	 * entityOrClass
	 */
	public static TableModel findTableModel(Object entityOrClass, Object... optionItems) {
		TableModel model = findFirstModel(optionItems);
		if (model != null)
			return model;
		return findEntityOrClassTableModel(entityOrClass);
	}

	public static TableModel findEntityOrClassTableModel(Object entityOrClass) {
		if (entityOrClass == null)
			throw new SqlBoxException("Can not build TableModel from null entityOrClass");
		else if (entityOrClass instanceof TableModel)
			return (TableModel) entityOrClass;
		else if (entityOrClass instanceof Class)
			return TableModelUtils.entity2ReadOnlyModel((Class<?>) entityOrClass);
		else // it's a entity bean
			return TableModelUtils.entity2ReadOnlyModel(entityOrClass.getClass());
	}

	public static TableModel findFirstModel(Object... optionItems) {// NOSONAR
		for (Object item : optionItems) { // If Model in option items, use it first
			if (item instanceof TableModel)
				return (TableModel) item;
			else if (item instanceof Class)
				return TableModelUtils.entity2ReadOnlyModel((Class<?>) item);
		}
		return null;
	}

	/**
	 * Return first tail TableModel, if not found return null
	 */
	public static TableModel findTailModel(SqlBoxContext ctx, TableModel entityModel, Object... optionItems) {// NOSONAR
		String tailTable = null;
		for (Object item : optionItems)
			if (item instanceof SqlItem && (SqlOption.TAIL.equals(((SqlItem) item).getType()))) {
				if (((SqlItem) item).getParameters().length == 0)
					tailTable = "";
				else
					tailTable = (String) ((SqlItem) item).getParameters()[0];
				break;
			}
		if (tailTable == null)
			return null;
		if ("".equals(tailTable))//
			tailTable = entityModel.getTableName();
		ctx.ensureTailModelLoaded();
		for (TableModel model : ctx.getTailModels()) {
			if (tailTable.equalsIgnoreCase(model.getTableName()))
				return model;
		}
		throw new SqlBoxException("Not found table '" + tailTable + "' in database");
	}

	/**
	 * Extract models from sqlItems
	 */
	public static TableModel[] findAllModels(Object... sqlItems) {// NOSONAR
		List<TableModel> result = new ArrayList<TableModel>();
		doFindAllModels(result, sqlItems);
		return result.toArray(new TableModel[result.size()]);
	}

	private static void doFindAllModels(List<TableModel> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel)
				result.add((TableModel) item);
			else if (item instanceof Class)
				result.add(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			else if (item.getClass().isArray())
				doFindAllModels(result, (Object[]) item);
		}
	}

	/**
	 * Find model and alias items from sqlItems
	 */
	public static Object[] findModelAlias(Object... sqlItems) {// NOSONAR
		List<Object> result = new ArrayList<Object>();
		dofindModelAlias(result, sqlItems);
		return result.toArray(new Object[result.size()]);
	}

	private static void dofindModelAlias(List<Object> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel) {
				result.add((TableModel) item);
			} else if (item instanceof Class) {
				result.add(TableModelUtils.entity2ReadOnlyModel((Class<?>) item));
			} else if (item.getClass().isArray()) {
				dofindModelAlias(result, (Object[]) item);
			} else if (item instanceof SqlItem) {
				SqlItem sqItem = (SqlItem) item;
				SqlOption sqlItemType = sqItem.getType();
				if (SqlOption.ALIAS.equals(sqlItemType)) {
					result.add(item);
				}
			}
		}
	}

	/**
	 * Find not model/alias items from sqlItems
	 */
	public static Object[] findNotModelAlias(Object... sqlItems) {// NOSONAR
		List<Object> result = new ArrayList<Object>();
		dofindNotModelAlias(result, sqlItems);
		return result.toArray(new Object[result.size()]);
	}

	private static void dofindNotModelAlias(List<Object> result, Object... sqlItems) {
		for (Object item : sqlItems) { // If Model in option items, use it first
			if (item instanceof TableModel || item instanceof Class) {// NOSONAR
			} else if (item.getClass().isArray()) {
				dofindNotModelAlias(result, (Object[]) item);
			} else if (item instanceof SqlItem) {
				if (!SqlOption.ALIAS.equals(((SqlItem) item).getType()))
					result.add(item);
			} else
				result.add(item);
		}
	}

	/**
	 * Create auto Alias name based on capital letters of class name in models of
	 * PreparedSQL, if alias already exists, put a number at end, for example: <br/>
	 * User ->u <br/>
	 * User, UserRole, UserOther, Order, Order_a -> u, u1, u2, o, o1
	 * 
	 * if no EntityClass, alais will be created by table name, for example: <br/>
	 * user ->u <br/>
	 * user_role -> UR
	 */
	public static void createLastAutoAliasName(PreparedSQL ps) {
		if (ps.getModels() == null || ps.getModels().length == 0)
			throw new SqlBoxException("No tableModel found");
		TableModel model = (TableModel) ps.getModels()[ps.getModels().length - 1];
		String alias;
		StringBuilder sb = new StringBuilder();
		char[] chars;
		if (model.getEntityClass() != null)
			chars = model.getEntityClass().getSimpleName().toCharArray();
		else
			chars = model.getTableName().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i == 0 //
					|| (c >= 'A' && c <= 'Z') //
					|| ((i > 0) && (chars[i - 1]) == '_') //
			)
				sb.append(c);
		}
		alias = sb.toString().toLowerCase();
		String[] aliases = ps.getAliases();
		int count = 1;
		String newAlias = alias;
		boolean found = false;
		do {
			for (int i = 0; i < aliases.length - 2; i++) {
				if (newAlias.equals(aliases[i])) {
					newAlias = alias + count++;
					found = true;
					break;
				}
			}
		} while (found);
		ps.setLastAliases(newAlias);
	}

	/** Convert one row data into EntityBean */
	public static <T> T mapToEntityBean(TableModel model, Map<String, Object> oneRow) {// NOSONAR
		if (oneRow == null || oneRow.isEmpty())
			throw new SqlBoxException("Can not use null or empty row to convert to EntityBean");
		SqlBoxException.assureNotNull(model.getEntityClass(), "Can not find entityClass setting in model.");
		@SuppressWarnings("unchecked")
		T bean = (T) ClassCacheUtils.createNewEntity(model.getEntityClass());
		for (Entry<String, Object> entry : oneRow.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			ColumnModel col = model.getColumnByColName(key);
			if (col == null) {
				if (bean instanceof TailType)
					((TailType) bean).tails().put(key, value);
			} else
				writeValueToBeanFieldOrTail(bean, col, value);
		}
		return bean;
	}

	/**
	 * Transfer Object to TableModel, object can be SqlBox instance, entityClass or
	 * entity Bean
	 * 
	 * <pre>
	 * 1. TableModel instance, will use it
	 * 2. SqlBox instance, will use its tableModel
	 * 3. Class, will call TableModelUtils.entity2Model to create tableModel
	 * 4. Object, will call TableModelUtils.entity2Model(entityOrClass.getClass()) to create a SqlBox instance
	 * </pre>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T entityOrClassToBean(Object entityOrClass) {
		if (entityOrClass == null)
			throw new SqlBoxException("Can build Bean for null entityOrClass");
		if (entityOrClass instanceof Class)
			try {
				return ((Class<T>) entityOrClass).newInstance();
			} catch (Exception e) {
				throw new SqlBoxException("Can not create new instance for '" + entityOrClass + "'");
			}
		else // it's a bean
			return (T) entityOrClass;
	}

	private static void notAllowSharding(ColumnModel col) {
		if (col.getShardTable() != null || col.getShardDatabase() != null)
			throw new SqlBoxException(
					"Fail to execute entity CRUD operation because found sharding column is not included in prime Key columns");
	}

	/**
	 * Based on PreparedSQL's models and alias, automatically build and append a SQL
	 * like below:
	 * 
	 * <pre>
	 * select a.**, b.**, c.**... from xxx a  
	 * left join xxx b on a.bid=b.id  
	 * left join xxx c on b.cid=c.id ...
	 * </pre>
	 */
	@SuppressWarnings("all")
	protected static void appendLeftJoinSQL(PreparedSQL ps) {
		Object[] m = ps.getModels();
		String[] a = ps.getAliases();
		SqlBoxException.assureTrue(m != null && a != null && m.length == a.length);

		StringBuilder sb = new StringBuilder(" select ");
		boolean ifFirst = true;
		for (int i = 0; i < m.length; i++) {// NOSONAR

			TableModel md = (TableModel) m[i];
			for (ColumnModel col : md.getColumns()) {
				if (col.getTransientable())
					continue;
				if (ifFirst)
					ifFirst = false;
				else
					sb.append(", ");
				sb.append(a[i]).append(".").append(col.getColumnName()).append(" as ").append(a[i]).append("_")
						.append(col.getColumnName());
			}
		}
		sb.append(" from ");
		sb.append(((TableModel) m[0]).getTableName()).append(" ").append(a[0]).append(" ");
		for (int i = 1; i < m.length; i++) {
			sb.append(" left join ");
			sb.append(((TableModel) m[i]).getTableName()).append(" ").append(a[i]);
			sb.append(" on ");
			appendKeyEquelsSqlPiece(sb, a[i - 1], ((TableModel) m[i - 1]), a[i], ((TableModel) m[i]));
		}
		ps.addSql(sb.toString());
	}

	/**
	 * Find relationship of 2 classes, build "a.bid1=b.id1 and a.bid2=b.id2..." SQL
	 * piece
	 */
	private static void appendKeyEquelsSqlPiece(StringBuilder sb, String a1, TableModel m1, String a2, TableModel m2) {
		List<FKeyModel> fkeys = m1.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m2.getTableName())) {// m2 is parent
				realDoAppendKeyEquelsSqlPiece(sb, a1, a2, fkey);
				return;
			}
		}
		fkeys = m2.getFkeyConstraints();
		for (FKeyModel fkey : fkeys) {
			String refTable = fkey.getRefTableAndColumns()[0];
			if (refTable.equalsIgnoreCase(m1.getTableName())) {// m1 is parent
				realDoAppendKeyEquelsSqlPiece(sb, a2, a1, fkey);
				return;
			}
		}
		throw new SqlBoxException("Not found relationship(foreign key) setting between '" + m1.getEntityClass()
				+ "' and '" + m2.getEntityClass() + "'");
	}

	/** Build a.bid1=b.id1 and a.bid2=b.id2 SQL piece */
	private static void realDoAppendKeyEquelsSqlPiece(StringBuilder sb, String a, String b, FKeyModel fkey) {
		int i = 0;
		for (String col : fkey.getColumnNames()) {
			if (i > 0)
				sb.append("and ");
			sb.append(a).append(".").append(col).append("=").append(b).append(".")
					.append(fkey.getRefTableAndColumns()[i + 1]).append(" ");
			i++;
		}
	}

	/** Read value from entityBean field or tail */
	public static Object readValueFromBeanFieldOrTail(Object entityBean, ColumnModel columnModel) {
		SqlBoxException.assureNotNull(columnModel, "columnModel can not be null");
		if (columnModel.getTransientable())
			return null;
		String fieldName = columnModel.getEntityField();
		if (fieldName == null) {
			if (entityBean instanceof TailType) {
				return ((TailType) entityBean).tails().get(columnModel.getColumnName());
			} else
				throw new DialectException("Can not read tail value from instance which is not TailSupport");
		} else {
			Method readMethod = ClassCacheUtils.getClassFieldReadMethod(entityBean.getClass(), fieldName);
			SqlBoxException.assureNotNull(readMethod, "No read method for '" + fieldName + "'");
			try {
				return readMethod.invoke(entityBean);
			} catch (Exception e) {
				throw new DialectException(e);
			}
		}
	}

	/** write value to entityBean field or tail */
	public static void writeValueToBeanFieldOrTail(Object entityBean, ColumnModel columnModel, Object value) {
		SqlBoxException.assureNotNull(columnModel, "columnModel can not be null");
		if (columnModel.getTransientable())
			return;
		String fieldName = columnModel.getEntityField();
		if (fieldName == null) {
			if (entityBean instanceof TailType) {
				((TailType) entityBean).tails().put(columnModel.getColumnName(), value);
			} else
				throw new DialectException("Can not write tail value for entity which is not TailSupport");
		} else
			try {
				Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityBean.getClass(), fieldName);
				writeMethod.invoke(entityBean, value);
			} catch (Exception e) {
				throw new DialectException("FieldName '" + fieldName + "' can not write with value '" + value + "'", e);
			}
	}

	@SuppressWarnings("unused")
	private static void crudMethods___________________________________() {
	}

	/**
	 * Insert entityBean into database, and change ID fields to values generated by
	 * IdGenerator (identity or sequence or UUID...), return row affected
	 */
	public static int entityInsertTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		ColumnModel identityCol = null;
		Type identityType = null;
		Boolean ignoreNull = null;
		jSQL.append(" (");
		boolean foundColumnToInsert = false;
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (ColumnModel col : cols.values()) {
			if (col == null || col.getTransientable() || !col.getInsertable())
				continue;
			if (col.getIdGenerationType() != null || !StrUtils.isEmpty(col.getIdGeneratorName())) {
				if (col.getIdGenerator() == null)
					throw new SqlBoxException("No IdGenerator found for column '" + col.getColumnName() + "'");
				IdGenerator idGen = col.getIdGenerator();
				if (GenerationType.IDENTITY.equals(idGen.getGenerationType())) {// Identity
					if (identityCol != null)
						throw new SqlBoxException(
								"More than 1 identity field found for table '" + model.getTableName() + "'");
					identityType = col.getColumnType();
					identityCol = col;
				} else if (GenerationType.SNOWFLAKE.equals(idGen.getGenerationType())) {// Snow
					jSQL.append(col.getColumnName());
					SnowflakeCreator snow = ctx.getSnowflakeCreator();
					if (snow == null)
						throw new SqlBoxException(
								"Current SqlBoxContext no SnowflakeCreator found when try to create a Snowflake value");
					Object id = snow.nextId();
					jSQL.append(param(id));
					jSQL.append(", ");
					foundColumnToInsert = true;
					writeValueToBeanFieldOrTail(entityBean, col, id);
				} else {// Normal Id Generator
					jSQL.append(col.getColumnName());
					Object id = idGen.getNextID(ctx, ctx.getDialect(), col.getColumnType());
					jSQL.append(param(id));
					jSQL.append(", ");
					foundColumnToInsert = true;
					writeValueToBeanFieldOrTail(entityBean, col, id);
				}
			} else {
				Object value = readValueFromBeanFieldOrTail(entityBean, col);
				if (value == null && ignoreNull == null) {
					for (Object itemObject : optionItems)
						if (SqlOption.IGNORE_NULL.equals(itemObject)) {
							ignoreNull = true;
							break;
						}
					if (ignoreNull == null)
						ignoreNull = false;
				}
				if (ignoreNull == null || !ignoreNull || value != null) {
					jSQL.append(col.getColumnName());
					jSQL.append(new SqlItem(SqlOption.PARAM, value));
					jSQL.append(", ");
					foundColumnToInsert = true;
				}
			}
			if (col.getPkey()) {
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(readValueFromBeanFieldOrTail(entityBean, col));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(readValueFromBeanFieldOrTail(entityBean, col));
			} else
				notAllowSharding(col);
		}

		if (foundColumnToInsert)
			jSQL.remove(jSQL.size() - 1);// delete the last ", "

		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(tableName);
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.frontAdd("insert into ");// insert into xxx (
		jSQL.append(") "); // insert into xxx ()
		jSQL.append(valuesQuestions()); // insert into xxx () values(?,?)

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
				jSQL.append(item);

		if (optionModel == null)// No optional model, force use entity's
			jSQL.frontAdd(model);

		int result = ctx.iUpdate(jSQL.toArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		if (identityCol != null) {// write identity id to Bean field
			Object identityId = IdentityIdGenerator.INSTANCE.getNextID(ctx, ctx.getDialect(), identityType);
			writeValueToBeanFieldOrTail(entityBean, identityCol, identityId);
		}
		return result;
	}

	/** Update entityBean according primary key, return row affected */
	public static int entityUpdateTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		Boolean ignoreNull = null;

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable() || !col.getUpdatable())
				continue;
			String fieldName = col.getEntityField();
			if (StrUtils.isEmpty(fieldName))
				fieldName = col.getColumnName();
			SqlBoxException.assureNotEmpty(fieldName,
					"Found a column not mapped to entity field or DB column in model '" + model + "'");
			Object value = readValueFromBeanFieldOrTail(entityBean, col);
			if (col.getPkey()) {
				if (!where.isEmpty())
					where.append(" and ");// NOSONAR
				where.append(col.getColumnName()).append("=?");
				where.append(param(value));
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(readValueFromBeanFieldOrTail(entityBean, col));
				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(readValueFromBeanFieldOrTail(entityBean, col));

			} else {
				notAllowSharding(col);
				if (value == null && ignoreNull == null) {
					for (Object itemObject : optionItems)
						if (SqlOption.IGNORE_NULL.equals(itemObject)) {
							ignoreNull = true;
							break;
						}
					if (ignoreNull == null)
						ignoreNull = false;
				}
				if (ignoreNull == null || !ignoreNull || value != null) {
					if (!jSQL.isEmpty())
						jSQL.append(", ");
					jSQL.append(col.getColumnName()).append("=? ");
					jSQL.append(param(value));
				}
			}
		}

		jSQL.frontAdd(" set ");
		if (shardTableItem != null)
			jSQL.frontAdd(shardTableItem);
		else
			jSQL.frontAdd(tableName);
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.frontAdd("update ");
		jSQL.append(" where ");// NOSONAR
		jSQL.addAll(where);

		if (optionItems != null) // optional SqlItems put at end
			for (Object item : optionItems)
				jSQL.append(item);

		if (optionModel == null)
			jSQL.frontAdd(model);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/**
	 * Delete entityBean in database according primary key value, return row
	 * affected
	 */
	public static int entityDeleteTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		return entityDeleteByIdTry(ctx, entityBean.getClass(), entityBean, optionItems);
	}

	/**
	 * Try delete entity by Id, return row affected, return row affected
	 */
	public static int entityDeleteByIdTry(SqlBoxContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (id instanceof TailType) {
			tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) id).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		for (ColumnModel col : cols.values()) {
			if (col == null || col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, col);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, col));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
			} else
				notAllowSharding(col);
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("delete from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(tableName);
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.append(" where ").addAll(where);

		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);

		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayHandler);
		int rowAffected = ctx.iUpdate(jSQL.toObjectArray());
		if (ctx.isBatchEnabled())
			return 1; // in batch mode, direct return 1
		return rowAffected;
	}

	/** Load entity according entity's id fields, return row affected */
	public static int entityLoadTry(SqlBoxContext ctx, Object entityBean, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (entityBean instanceof TailType) {
			tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) entityBean).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		List<ColumnModel> effectColumns = new ArrayList<ColumnModel>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				where.append(col.getColumnName()).append("=?")
						.append(param(readValueFromBeanFieldOrTail(entityBean, col))).append(" and ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(readValueFromBeanFieldOrTail(entityBean, col));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(readValueFromBeanFieldOrTail(entityBean, col));
			} else
				notAllowSharding(col);
			jSQL.append(col.getColumnName()).append(", ");
			effectColumns.add(col);
		}
		jSQL.remove(jSQL.size() - 1);// delete the last ", "
		if (where.isEmpty())
			throw new SqlBoxException("No PKey column found from tableModel '" + model.getTableName() + "'");
		where.remove(where.size() - 1);// delete the last " and"

		jSQL.frontAdd("select ").append(" from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(tableName);
		if (shardDbItem != null)
			jSQL.append(shardDbItem);

		jSQL.append(" where ").addAll(where);

		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);
		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());

		if (valuesList == null || valuesList.isEmpty())
			return 0;
		Object[] values = valuesList.get(0);
		for (int i = 0; i < values.length; i++)
			SqlBoxContextUtils.writeValueToBeanFieldOrTail(entityBean, effectColumns.get(i), values[i]);
		return valuesList.size();
	}

	/**
	 * Create a new Entity, load from DB according given ID, return null if entity
	 * does not exist in DB
	 */
	public static <T> T entityLoadByIdTry(SqlBoxContext ctx, Class<T> entityClass, Object id, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				if (col != null)
					cols.put(col.getColumnName().toLowerCase(), col);

		T bean = SqlBoxContextUtils.entityOrClassToBean(entityClass);
		bean = EntityIdUtils.setEntityIdValues(bean, id, cols.values());
		int result = entityLoadTry(ctx, bean, optionItems);
		if (result != 1)
			return null;
		else
			return bean;
	}

	/**
	 * Check if entityBean exist in database by its id
	 */
	public static boolean entityExist(SqlBoxContext ctx, Object entityBean, Object... optionItems) {
		return entityExistById(ctx, entityBean.getClass(), entityBean, optionItems);
	}

	/**
	 * Try delete entity by Id, return row affected
	 */
	public static boolean entityExistById(SqlBoxContext ctx, Class<?> entityClass, Object id, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		if (id instanceof TailType) {
			tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
			if (tailModel != null)
				for (String colName : ((TailType) id).tails().keySet()) {
					ColumnModel col = tailModel.getColumnByColName(colName);
					if (col != null)
						cols.put(col.getColumnName().toLowerCase(), col);
				}
		}
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		LinkStyleArrayList<Object> where = new LinkStyleArrayList<Object>();
		SqlItem shardTableItem = null;
		SqlItem shardDbItem = null;
		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				Object value = EntityIdUtils.readFeidlValueFromEntityId(id, col);
				if (!where.isEmpty())
					where.append(" and ");
				where.append(param(value));
				where.append(col.getColumnName()).append("=? ");
				if (col.getShardTable() != null) // Sharding Table?
					shardTableItem = shardTB(EntityIdUtils.readFeidlValueFromEntityId(id, col));

				if (col.getShardDatabase() != null) // Sharding DB?
					shardDbItem = shardDB(EntityIdUtils.readFeidlValueFromEntityId(id, col));
			} else
				notAllowSharding(col);
		}
		if (where.isEmpty())
			throw new SqlBoxException("No primary key found for entityBean");

		jSQL.append("select count(1) from ");
		if (shardTableItem != null)
			jSQL.append(shardTableItem);
		else
			jSQL.append(tableName);
		if (shardDbItem != null)
			jSQL.append(shardDbItem);
		jSQL.append(" where ").addAll(where);

		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);

		if (optionModel == null)
			jSQL.frontAdd(model);

		long result = ctx.iQueryForLongValue(jSQL.toObjectArray());
		if (result == 1)
			return true;
		else if (result == 0)
			return false;
		else
			throw new SqlBoxException(
					"Fail to check entity exist because found " + result + " rows record in database");
	}

	/** Count quantity of all entity, this method does not support sharding */
	public static int entityCountAll(SqlBoxContext ctx, Class<?> entityClass, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				cols.put(col.getColumnName().toLowerCase(), col);
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if ((col.getShardTable() != null || col.getShardDatabase() != null))
				throw new SqlBoxException("Fail to count all entity because sharding columns exist.");
		}

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		jSQL.append("select count(1) from ").append(tableName);
		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);
		if (optionModel == null)
			jSQL.frontAdd(model);
		return ((Number) ctx.iQueryForObject(jSQL.toObjectArray())).intValue();// NOSONAR
	}

	public static <T> List<T> entityFindAll(SqlBoxContext ctx, Class<T> entityClass, Object... optionItems) {// NOSONAR
		TableModel optionModel = SqlBoxContextUtils.findFirstModel(optionItems);
		TableModel model = optionModel;
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityClass);
		Map<String, ColumnModel> cols = new HashMap<String, ColumnModel>();
		for (ColumnModel col : model.getColumns())
			cols.put(col.getColumnName().toLowerCase(), col);
		TableModel tailModel = null;
		tailModel = SqlBoxContextUtils.findTailModel(ctx, model, optionItems);
		if (tailModel != null)
			for (ColumnModel col : tailModel.getColumns())
				cols.put(col.getColumnName().toLowerCase(), col);
		String tableName = model.getTableName();
		if (tailModel != null)
			tableName = tailModel.getTableName();

		LinkStyleArrayList<Object> jSQL = new LinkStyleArrayList<Object>();
		List<ColumnModel> effectColumns = new ArrayList<ColumnModel>();

		for (ColumnModel col : cols.values()) {
			if (col.getTransientable())
				continue;
			if ((col.getShardTable() != null || col.getShardDatabase() != null))
				throw new SqlBoxException("Fail to load all entity because sharding columns exist.");
			jSQL.append(col.getColumnName()).append(", ");
			effectColumns.add(col);
		}
		jSQL.remove(jSQL.size() - 1);// delete the last ", "
		jSQL.frontAdd("select ").append(" from ");
		jSQL.append(tableName);
		if (optionItems != null)
			for (Object item : optionItems)
				jSQL.append(item);
		if (optionModel == null)
			jSQL.frontAdd(model);

		jSQL.append(SingleTonHandlers.arrayListHandler);
		List<Object[]> valuesList = ctx.iQuery(jSQL.toObjectArray());

		List<T> result = new ArrayList<T>();
		if (valuesList == null || valuesList.isEmpty())
			return result;
		for (Object[] values : valuesList) {
			T bean = SqlBoxContextUtils.entityOrClassToBean(entityClass);
			for (int i = 0; i < effectColumns.size(); i++)
				SqlBoxContextUtils.writeValueToBeanFieldOrTail(bean, effectColumns.get(i), values[i]);
			result.add(bean);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> entityFindBySample(SqlBoxContext ctx, Object sampleBean, Object... sqlItems) {
		return (List<T>) entityFindAll(ctx, sampleBean.getClass(),
				new SampleItem(sampleBean).sql(" where ").notNullFields(), sqlItems);
	}

	@SuppressWarnings("unused")
	private static void ormQueryMethods___________________________________() {
	}

	public static EntityNet entityAutoNet(SqlBoxContext ctx, Class<?>... entityClasses) {
		TableModel[] models = findAllModels((Object[]) entityClasses);
		PreparedSQL ps = ctx.iPrepare(SqlOption.QUERY, new EntityNetHandler(), models, AUTO_SQL);
		SqlBoxException.assureTrue(ps.getAliases() != null && ps.getAliases().length > 1);
		String firstAlias = ps.getAliases()[0];
		for (int i = 1; i < entityClasses.length; i++)
			ps.giveBoth(firstAlias, ps.getAliases()[i]);
		return (EntityNet) ctx.runPreparedSQL(ps);
	}

	public static <E> E entityFindRelatedOne(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		List<E> list = entityFindRelatedList(ctx, entity, sqlItems);
		if (list.size() != 1)
			throw new SqlBoxException("Expect 1 entity but found " + list.size() + " records");
		return list.get(0);
	}

	@SuppressWarnings("unchecked")
	public static <E> List<E> entityFindRelatedList(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedList(ctx, entity, sqlItems);

		SqlBoxException.assureNotNull(entity);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (List<E>) net.pickEntityList(models[models.length - 1].getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public static <E> Set<E> entityFindRelatedSet(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedSet(ctx, entity, sqlItems);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (Set<E>) net.pickEntitySet(models[models.length - 1].getEntityClass());
	}

	@SuppressWarnings("unchecked")
	public static <E> Map<Object, E> entityFindRelatedMap(SqlBoxContext ctx, Object entity, Object... sqlItems) {
		if (sqlItems.length == 0)
			throw new SqlBoxException("Target entity class is required");
		for (Object item : sqlItems)
			if (item instanceof EntityNet)
				return ((EntityNet) item).findRelatedMap(ctx, entity, sqlItems);
		TableModel[] models = findAllModels(sqlItems);
		Object[] modelsAlias = findModelAlias(sqlItems);
		Object[] notModelAlias = findNotModelAlias(sqlItems);
		EntityNet net = ctx.iQuery(SqlOption.QUERY, new EntityNetHandler(), modelsAlias, AUTO_SQL, " where ",
				new EntityKeyItem(entity), notModelAlias);
		return (Map<Object, E>) net.pickEntityMap(models[models.length - 1].getEntityClass());
	}
}