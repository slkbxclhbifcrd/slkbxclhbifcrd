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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;

/**
 * Guess and execute the SQL for a annotated ActiveRecord entity's method
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
@SuppressWarnings("unchecked")
public class SqlMapperDefaultGuesser implements SqlMapperGuesser {
	public static final SqlMapperGuesser instance = new SqlMapperDefaultGuesser();

	@Override
	@SuppressWarnings("all")
	public <T> T guess(SqlBoxContext ctx, Object entity, Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.SqlMapperDefaultGuesser".equals(stack.getClassName())
					&& "guess".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos + 1].getClassName();
		String callerMethodName = stacks[callerPos + 1].getMethodName();
		if (callerClassName.endsWith(SqlMapperUtils.CHILD_SUFFIX))
			callerClassName = callerClassName.substring(0,
					callerClassName.length() - SqlMapperUtils.CHILD_SUFFIX.length());
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		PreparedSQL ps = buildPreparedSQL(ctx, callerClassName, callerMethod, params);
		return (T) ctx.runPreparedSQL(ps);
	}

	@Override
	public String guessSQL(SqlBoxContext ctx, Object ac) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guessSQL".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos].getClassName();
		String callerMethodName = stacks[callerPos].getMethodName();
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		return SqlMapperUtils.getSqlOfMethod(callerClassName, callerMethod);
	}

	@Override
	public PreparedSQL doGuessPreparedSQL(SqlBoxContext ctx, Object ac, Object... params) {// NOSONAR
		int callerPos = 0;
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (StackTraceElement stack : stacks) {
			callerPos++;
			if ("com.github.drinkjava2.jsqlbox.ActiveRecord".equals(stack.getClassName())
					&& "guessPreparedSQL".equals(stack.getMethodName()))
				break;
		}
		String callerClassName = stacks[callerPos].getClassName();
		String callerMethodName = stacks[callerPos].getMethodName();
		Class<?> callerClass = ClassCacheUtils.checkClassExist(callerClassName);
		if (callerClass == null)
			throw new SqlBoxException("Can not find class '" + callerClassName + "'");
		Method callerMethod = ClassCacheUtils.checkMethodExist(callerClass, callerMethodName);
		if (callerMethod == null)
			throw new SqlBoxException("Can not find method '" + callerMethodName + "' in '" + callerClassName + "'");
		return buildPreparedSQL(ctx, callerClassName, callerMethod, params);
	}

	private PreparedSQL buildPreparedSQL(SqlBoxContext ctx, String callerClassName, Method callerMethod,
			Object... params) {
		List<Object> realParamList = new ArrayList<Object>();
		String sql = SqlMapperUtils.getSqlOfMethod(callerClassName, callerMethod);
		SqlBoxException.assureNotEmpty(sql, "Can not find sql text for method '" + callerMethod.getName() + "'");
		realParamList.add(sql);
		SqlMapperUtils.dealNewIocModelAliasAnnotation(realParamList, callerMethod);
		for (Object para : params)
			realParamList.add(para);

		PreparedSQL ps = ctx.pPrepare(realParamList.toArray(new Object[realParamList.size()]));
		if (ps.getOperationType() == null)
			if (StrUtils.startsWithIgnoreCase(ps.getSql(), "select"))
				ps.setOperationType(SqlOption.QUERY);
			else if (StrUtils.startsWithIgnoreCase(ps.getSql(), "delete"))
				ps.setOperationType(SqlOption.UPDATE);
			else if (StrUtils.startsWithIgnoreCase(ps.getSql(), "update"))
				ps.setOperationType(SqlOption.UPDATE);
			else if (StrUtils.startsWithIgnoreCase(ps.getSql(), "insert"))
				ps.setOperationType(SqlOption.UPDATE);
			else
				throw new SqlBoxException(
						"Can not guess SqlType, only can guess SQL started with select/delete/update/insert, need manually set SqlOption type");
		// Guess method always use template style
		ps.ifNullSetUseTemplate(sql.indexOf(':') > -1 || sql.indexOf('{') > -1 || sql.indexOf('[') > -1);
		return ps;
	}

}