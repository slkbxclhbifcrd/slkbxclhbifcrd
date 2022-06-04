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

import com.github.drinkjava2.jdialects.springsrc.utils.StringUtils;

/**
 * JSQLBOX store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class JSQLBOX extends DB {
	public static final String EQ_P        = " = ? ";
	public static final String EQ          = " = ";
	public static final String LTEQ_P      = " <= ? ";
	public static final String LTEQ        = " <= ";
	public static final String GTEQ_P      = " >= ? ";
	public static final String GTEQ        = " >= ";
	public static final String NOTEQ        = " != ";
	public static final String NOTEQ_P        = " != ? ";
	public static final String IS_NULL     = " IS NULL ";
	public static final String IS_NOT_NULL = " IS NOT NULL ";
	public static final String NOT_IN      = " NOT IN ";
	public static final String ORDER_BY    = " ORDER BY ";
	public static final String LIMIT_1     = " LIMIT 1";
	public static final String SELECT_STAR = " SELECT * ";
	public static final String DELETE_FROM = " DELETE FROM ";
	public static final String INSERT_INTO = " INSERT INTO ";
	// sql 关键字
	public static final String ABSOLUTE = " ABSOLUTE ";
	public static final String ACCESS = " ACCESS ";
	public static final String ACTION = " ACTION ";
	public static final String ADA = " ADA ";
	public static final String ADD = " ADD ";
	public static final String ADMIN = " ADMIN ";
	public static final String AFTER = " AFTER ";
	public static final String AGGREGATE = " AGGREGATE ";
	public static final String ALIAS = " ALIAS ";
	public static final String ALL = " ALL ";
	public static final String ALLOCATE = " ALLOCATE ";
	public static final String ALTER = " ALTER ";
	public static final String ANALYSE = " ANALYSE ";
	public static final String ANALYZE = " ANALYZE ";
	public static final String AND = " AND ";
	public static final String ANY = " ANY ";
	public static final String ARE = " ARE ";
	public static final String ARRAY = " ARRAY ";
	public static final String AS = " AS ";
	public static final String ASC = " ASC ";
	public static final String ASENSITIVE = " ASENSITIVE ";
	public static final String ASSERTION = " ASSERTION ";
	public static final String ASSIGNMENT = " ASSIGNMENT ";
	public static final String ASYMMETRIC = " ASYMMETRIC ";
	public static final String AT = " AT ";
	public static final String ATOMIC = " ATOMIC ";
	public static final String AUTHORIZATION = " AUTHORIZATION ";
	public static final String AVG = " AVG ";
	public static final String BACKWARD = " BACKWARD ";
	public static final String BEFORE = " BEFORE ";
	public static final String BEGIN = " BEGIN ";
	public static final String BETWEEN = " BETWEEN ";
	public static final String BINARY = " BINARY ";
	public static final String BIT = " BIT ";
	public static final String BITVAR = " BITVAR ";
	public static final String BIT_LENGTH = " BIT_LENGTH ";
	public static final String BLOB = " BLOB ";
	public static final String BOOLEAN = " BOOLEAN ";
	public static final String BOTH = " BOTH ";
	public static final String BREADTH = " BREADTH ";
	public static final String BY = " BY ";
	public static final String C = " C ";
	public static final String CACHE = " CACHE ";
	public static final String CALL = " CALL ";
	public static final String CALLED = " CALLED ";
	public static final String CARDINALITY = " CARDINALITY ";
	public static final String CASCADE = " CASCADE ";
	public static final String CASCADED = " CASCADED ";
	public static final String CASE = " CASE ";
	public static final String CAST = " CAST ";
	public static final String CATALOG = " CATALOG ";
	public static final String CATALOG_NAME = " CATALOG_NAME ";
	public static final String CHAIN = " CHAIN ";
	public static final String CHAR = " CHAR ";
	public static final String CHARACTER = " CHARACTER ";
	public static final String CHARACTERISTICS = " CHARACTERISTICS ";
	public static final String CHARACTER_LENGTH = " CHARACTER_LENGTH ";
	public static final String CHARACTER_SET_CATALOG = " CHARACTER_SET_CATALOG ";
	public static final String CHARACTER_SET_NAME = " CHARACTER_SET_NAME ";
	public static final String CHARACTER_SET_SCHEMA = " CHARACTER_SET_SCHEMA ";
	public static final String CHAR_LENGTH = " CHAR_LENGTH ";
	public static final String CHECK = " CHECK ";
	public static final String CHECKED = " CHECKED ";
	public static final String CHECKPOINT = " CHECKPOINT ";
	public static final String CLASS = " CLASS ";
	public static final String CLASS_ORIGIN = " CLASS_ORIGIN ";
	public static final String CLOB = " CLOB ";
	public static final String CLOSE = " CLOSE ";
	public static final String CLUSTER = " CLUSTER ";
	public static final String COALESCE = " COALESCE ";
	public static final String COBOL = " COBOL ";
	public static final String COLLATE = " COLLATE ";
	public static final String COLLATION = " COLLATION ";
	public static final String COLLATION_CATALOG = " COLLATION_CATALOG ";
	public static final String COLLATION_NAME = " COLLATION_NAME ";
	public static final String COLLATION_SCHEMA = " COLLATION_SCHEMA ";
	public static final String COLUMN = " COLUMN ";
	public static final String COLUMN_NAME = " COLUMN_NAME ";
	public static final String COMMAND_FUNCTION = " COMMAND_FUNCTION ";
	public static final String COMMAND_FUNCTION_CODE = " COMMAND_FUNCTION_CODE ";
	public static final String COMMENT = " COMMENT ";
	public static final String COMMIT = " COMMIT ";
	public static final String COMMITTED = " COMMITTED ";
	public static final String COMPLETION = " COMPLETION ";
	public static final String CONDITION_NUMBER = " CONDITION_NUMBER ";
	public static final String CONNECT = " CONNECT ";
	public static final String CONNECTION = " CONNECTION ";
	public static final String CONNECTION_NAME = " CONNECTION_NAME ";
	public static final String CONSTRAINT = " CONSTRAINT ";
	public static final String CONSTRAINTS = " CONSTRAINTS ";
	public static final String CONSTRAINT_CATALOG = " CONSTRAINT_CATALOG ";
	public static final String CONSTRAINT_NAME = " CONSTRAINT_NAME ";
	public static final String CONSTRAINT_SCHEMA = " CONSTRAINT_SCHEMA ";
	public static final String CONSTRUCTOR = " CONSTRUCTOR ";
	public static final String CONTAINS = " CONTAINS ";
	public static final String CONTINUE = " CONTINUE ";
	public static final String CONVERT = " CONVERT ";
	public static final String COPY = " COPY ";
	public static final String CORRESPONDING = " CORRESPONDING ";
	public static final String COUNT = " COUNT ";
	public static final String CREATE = " CREATE ";
	public static final String CREATEDB = " CREATEDB ";
	public static final String CREATEUSER = " CREATEUSER ";
	public static final String CROSS = " CROSS ";
	public static final String CUBE = " CUBE ";
	public static final String CURRENT = " CURRENT ";
	public static final String CURRENT_DATE = " CURRENT_DATE ";
	public static final String CURRENT_PATH = " CURRENT_PATH ";
	public static final String CURRENT_ROLE = " CURRENT_ROLE ";
	public static final String CURRENT_TIME = " CURRENT_TIME ";
	public static final String CURRENT_TIMESTAMP = " CURRENT_TIMESTAMP ";
	public static final String CURRENT_USER = " CURRENT_USER ";
	public static final String CURSOR = " CURSOR ";
	public static final String CURSOR_NAME = " CURSOR_NAME ";
	public static final String CYCLE = " CYCLE ";
	public static final String DATA = " DATA ";
	public static final String DATABASE = " DATABASE ";
	public static final String DATE = " DATE ";
	public static final String DATETIME_INTERVAL_CODE = " DATETIME_INTERVAL_CODE ";
	public static final String DATETIME_INTERVAL_PRECISION = " DATETIME_INTERVAL_PRECISION ";
	public static final String DAY = " DAY ";
	public static final String DEALLOCATE = " DEALLOCATE ";
	public static final String DEC = " DEC ";
	public static final String DECIMAL = " DECIMAL ";
	public static final String DECLARE = " DECLARE ";
	public static final String DEFAULT = " DEFAULT ";
	public static final String DEFERRABLE = " DEFERRABLE ";
	public static final String DEFERRED = " DEFERRED ";
	public static final String DEFINED = " DEFINED ";
	public static final String DEFINER = " DEFINER ";
	public static final String DELETE = " DELETE ";
	public static final String DELIMITERS = " DELIMITERS ";
	public static final String DEPTH = " DEPTH ";
	public static final String DEREF = " DEREF ";
	public static final String DESC = " DESC ";
	public static final String DESCRIBE = " DESCRIBE ";
	public static final String DESCRIPTOR = " DESCRIPTOR ";
	public static final String DESTROY = " DESTROY ";
	public static final String DESTRUCTOR = " DESTRUCTOR ";
	public static final String DETERMINISTIC = " DETERMINISTIC ";
	public static final String DIAGNOSTICS = " DIAGNOSTICS ";
	public static final String DICTIONARY = " DICTIONARY ";
	public static final String DISCONNECT = " DISCONNECT ";
	public static final String DISPATCH = " DISPATCH ";
	public static final String DISTINCT = " DISTINCT ";
	public static final String DO = " DO ";
	public static final String DOMAIN = " DOMAIN ";
	public static final String DOUBLE = " DOUBLE ";
	public static final String DROP = " DROP ";
	public static final String DYNAMIC = " DYNAMIC ";
	public static final String DYNAMIC_FUNCTION = " DYNAMIC_FUNCTION ";
	public static final String DYNAMIC_FUNCTION_CODE = " DYNAMIC_FUNCTION_CODE ";
	public static final String EACH = " EACH ";
	public static final String ELSE = " ELSE ";
	public static final String ENCODING = " ENCODING ";
	public static final String ENCRYPTED = " ENCRYPTED ";
	public static final String END = " END ";
	public static final String END_EXEC = " END-EXEC ";
	public static final String EQUALS = " EQUALS ";
	public static final String ESCAPE = " ESCAPE ";
	public static final String EVERY = " EVERY ";
	public static final String EXCEPT = " EXCEPT ";
	public static final String EXCEPTION = " EXCEPTION ";
	public static final String EXCLUSIVE = " EXCLUSIVE ";
	public static final String EXEC = " EXEC ";
	public static final String EXECUTE = " EXECUTE ";
	public static final String EXISTING = " EXISTING ";
	public static final String EXISTS = " EXISTS ";
	public static final String EXPLAIN = " EXPLAIN ";
	public static final String EXTERNAL = " EXTERNAL ";
	public static final String EXTRACT = " EXTRACT ";
	public static final String FALSE = " FALSE ";
	public static final String FETCH = " FETCH ";
	public static final String FINAL = " FINAL ";
	public static final String FIRST = " FIRST ";
	public static final String FLOAT = " FLOAT ";
	public static final String FOR = " FOR ";
	public static final String FORCE = " FORCE ";
	public static final String FOREIGN = " FOREIGN ";
	public static final String FORTRAN = " FORTRAN ";
	public static final String FORWARD = " FORWARD ";
	public static final String FOUND = " FOUND ";
	public static final String FREE = " FREE ";
	public static final String FREEZE = " FREEZE ";
	public static final String FROM = " FROM ";
	public static final String FULL = " FULL ";
	public static final String FUNCTION = " FUNCTION ";
	public static final String G = " G ";
	public static final String GENERAL = " GENERAL ";
	public static final String GENERATED = " GENERATED ";
	public static final String GET = " GET ";
	public static final String GLOBAL = " GLOBAL ";
	public static final String GO = " GO ";
	public static final String GOTO = " GOTO ";
	public static final String GRANT = " GRANT ";
	public static final String GRANTED = " GRANTED ";
	public static final String GROUP = " GROUP ";
	public static final String GROUPING = " GROUPING ";
	public static final String HANDLER = " HANDLER ";
	public static final String HAVING = " HAVING ";
	public static final String HIERARCHY = " HIERARCHY ";
	public static final String HOLD = " HOLD ";
	public static final String HOST = " HOST ";
	public static final String HOUR = " HOUR ";
	public static final String IDENTITY = " IDENTITY ";
	public static final String IGNORE = " IGNORE ";
	public static final String ILIKE = " ILIKE ";
	public static final String IMMEDIATE = " IMMEDIATE ";
	public static final String IMPLEMENTATION = " IMPLEMENTATION ";
	public static final String IN = " IN ";
	public static final String INCREMENT = " INCREMENT ";
	public static final String INDEX = " INDEX ";
	public static final String INDICATOR = " INDICATOR ";
	public static final String INFIX = " INFIX ";
	public static final String INHERITS = " INHERITS ";
	public static final String INITIALIZE = " INITIALIZE ";
	public static final String INITIALLY = " INITIALLY ";
	public static final String INNER = " INNER ";
	public static final String INOUT = " INOUT ";
	public static final String INPUT = " INPUT ";
	public static final String INSENSITIVE = " INSENSITIVE ";
	public static final String INSERT = " INSERT ";
	public static final String INSTANCE = " INSTANCE ";
	public static final String INSTANTIABLE = " INSTANTIABLE ";
	public static final String INSTEAD = " INSTEAD ";
	public static final String INT = " INT ";
	public static final String INTEGER = " INTEGER ";
	public static final String INTERSECT = " INTERSECT ";
	public static final String INTERVAL = " INTERVAL ";
	public static final String INTO = " INTO ";
	public static final String INVOKER = " INVOKER ";
	public static final String IS = " IS ";
	public static final String ISNULL = " ISNULL ";
	public static final String ISOLATION = " ISOLATION ";
	public static final String ITERATE = " ITERATE ";
	public static final String JOIN = " JOIN ";
	public static final String K = " K ";
	public static final String KEY = " KEY ";
	public static final String KEY_MEMBER = " KEY_MEMBER ";
	public static final String KEY_TYPE = " KEY_TYPE ";
	public static final String LANCOMPILER = " LANCOMPILER ";
	public static final String LANGUAGE = " LANGUAGE ";
	public static final String LARGE = " LARGE ";
	public static final String LAST = " LAST ";
	public static final String LATERAL = " LATERAL ";
	public static final String LEADING = " LEADING ";
	public static final String LEFT = " LEFT ";
	public static final String LENGTH = " LENGTH ";
	public static final String LESS = " LESS ";
	public static final String LEVEL = " LEVEL ";
	public static final String LIKE = " LIKE ";
	public static final String LIMIT = " LIMIT ";
	public static final String LISTEN = " LISTEN ";
	public static final String LOAD = " LOAD ";
	public static final String LOCAL = " LOCAL ";
	public static final String LOCALTIME = " LOCALTIME ";
	public static final String LOCALTIMESTAMP = " LOCALTIMESTAMP ";
	public static final String LOCATION = " LOCATION ";
	public static final String LOCATOR = " LOCATOR ";
	public static final String LOCK = " LOCK ";
	public static final String LOWER = " LOWER ";
	public static final String M = " M ";
	public static final String MAP = " MAP ";
	public static final String MATCH = " MATCH ";
	public static final String MAX = " MAX ";
	public static final String MAXVALUE = " MAXVALUE ";
	public static final String MESSAGE_LENGTH = " MESSAGE_LENGTH ";
	public static final String MESSAGE_OCTET_LENGTH = " MESSAGE_OCTET_LENGTH ";
	public static final String MESSAGE_TEXT = " MESSAGE_TEXT ";
	public static final String METHOD = " METHOD ";
	public static final String MIN = " MIN ";
	public static final String MINUTE = " MINUTE ";
	public static final String MINVALUE = " MINVALUE ";
	public static final String MOD = " MOD ";
	public static final String MODE = " MODE ";
	public static final String MODIFIES = " MODIFIES ";
	public static final String MODIFY = " MODIFY ";
	public static final String MODULE = " MODULE ";
	public static final String MONTH = " MONTH ";
	public static final String MORE = " MORE ";
	public static final String MOVE = " MOVE ";
	public static final String MUMPS = " MUMPS ";
	public static final String NAME = " NAME ";
	public static final String NAMES = " NAMES ";
	public static final String NATIONAL = " NATIONAL ";
	public static final String NATURAL = " NATURAL ";
	public static final String NCHAR = " NCHAR ";
	public static final String NCLOB = " NCLOB ";
	public static final String NEW = " NEW ";
	public static final String NEXT = " NEXT ";
	public static final String NO = " NO ";
	public static final String NOCREATEDB = " NOCREATEDB ";
	public static final String NOCREATEUSER = " NOCREATEUSER ";
	public static final String NONE = " NONE ";
	public static final String NOT = " NOT ";
	public static final String NOTHING = " NOTHING ";
	public static final String NOTIFY = " NOTIFY ";
	public static final String NOTNULL = " NOTNULL ";
	public static final String NULL = " NULL ";
	public static final String NULLABLE = " NULLABLE ";
	public static final String NULLIF = " NULLIF ";
	public static final String NUMBER = " NUMBER ";
	public static final String NUMERIC = " NUMERIC ";
	public static final String OBJECT = " OBJECT ";
	public static final String OCTET_LENGTH = " OCTET_LENGTH ";
	public static final String OF = " OF ";
	public static final String OFF = " OFF ";
	public static final String OFFSET = " OFFSET ";
	public static final String OIDS = " OIDS ";
	public static final String OLD = " OLD ";
	public static final String ON = " ON ";
	public static final String ONLY = " ONLY ";
	public static final String OPEN = " OPEN ";
	public static final String OPERATION = " OPERATION ";
	public static final String OPERATOR = " OPERATOR ";
	public static final String OPTION = " OPTION ";
	public static final String OPTIONS = " OPTIONS ";
	public static final String OR = " OR ";
	public static final String ORDER = " ORDER ";
	public static final String ORDINALITY = " ORDINALITY ";
	public static final String OUT = " OUT ";
	public static final String OUTER = " OUTER ";
	public static final String OUTPUT = " OUTPUT ";
	public static final String OVERLAPS = " OVERLAPS ";
	public static final String OVERLAY = " OVERLAY ";
	public static final String OVERRIDING = " OVERRIDING ";
	public static final String OWNER = " OWNER ";
	public static final String PAD = " PAD ";
	public static final String PARAMETER = " PARAMETER ";
	public static final String PARAMETERS = " PARAMETERS ";
	public static final String PARAMETER_MODE = " PARAMETER_MODE ";
	public static final String PARAMETER_NAME = " PARAMETER_NAME ";
	public static final String PARAMETER_ORDINAL_POSITION = " PARAMETER_ORDINAL_POSITION ";
	public static final String PARAMETER_SPECIFIC_CATALOG = " PARAMETER_SPECIFIC_CATALOG ";
	public static final String PARAMETER_SPECIFIC_NAME = " PARAMETER_SPECIFIC_NAME ";
	public static final String PARAMETER_SPECIFIC_SCHEMA = " PARAMETER_SPECIFIC_SCHEMA ";
	public static final String PARTIAL = " PARTIAL ";
	public static final String PASCAL = " PASCAL ";
	public static final String PASSWORD = " PASSWORD ";
	public static final String PATH = " PATH ";
	public static final String PENDANT = " PENDANT ";
	public static final String PLI = " PLI ";
	public static final String POSITION = " POSITION ";
	public static final String POSTFIX = " POSTFIX ";
	public static final String PRECISION = " PRECISION ";
	public static final String PREFIX = " PREFIX ";
	public static final String PREORDER = " PREORDER ";
	public static final String PREPARE = " PREPARE ";
	public static final String PRESERVE = " PRESERVE ";
	public static final String PRIMARY = " PRIMARY ";
	public static final String PRIOR = " PRIOR ";
	public static final String PRIVILEGES = " PRIVILEGES ";
	public static final String PROCEDURAL = " PROCEDURAL ";
	public static final String PROCEDURE = " PROCEDURE ";
	public static final String PUBLIC = " PUBLIC ";
	public static final String READ = " READ ";
	public static final String READS = " READS ";
	public static final String REAL = " REAL ";
	public static final String RECURSIVE = " RECURSIVE ";
	public static final String REF = " REF ";
	public static final String REFERENCES = " REFERENCES ";
	public static final String REFERENCING = " REFERENCING ";
	public static final String REINDEX = " REINDEX ";
	public static final String RELATIVE = " RELATIVE ";
	public static final String RENAME = " RENAME ";
	public static final String REPEATABLE = " REPEATABLE ";
	public static final String REPLACE = " REPLACE ";
	public static final String RESET = " RESET ";
	public static final String RESTRICT = " RESTRICT ";
	public static final String RESULT = " RESULT ";
	public static final String RETURN = " RETURN ";
	public static final String RETURNED_LENGTH = " RETURNED_LENGTH ";
	public static final String RETURNED_OCTET_LENGTH = " RETURNED_OCTET_LENGTH ";
	public static final String RETURNED_SQLSTATE = " RETURNED_SQLSTATE ";
	public static final String RETURNS = " RETURNS ";
	public static final String REVOKE = " REVOKE ";
	public static final String RIGHT = " RIGHT ";
	public static final String ROLE = " ROLE ";
	public static final String ROLLBACK = " ROLLBACK ";
	public static final String ROLLUP = " ROLLUP ";
	public static final String ROUTINE = " ROUTINE ";
	public static final String ROUTINE_CATALOG = " ROUTINE_CATALOG ";
	public static final String ROUTINE_NAME = " ROUTINE_NAME ";
	public static final String ROUTINE_SCHEMA = " ROUTINE_SCHEMA ";
	public static final String ROW = " ROW ";
	public static final String ROWS = " ROWS ";
	public static final String ROW_COUNT = " ROW_COUNT ";
	public static final String RULE = " RULE ";
	public static final String SAVEPOINT = " SAVEPOINT ";
	public static final String SCALE = " SCALE ";
	public static final String SCHEMA = " SCHEMA ";
	public static final String SCHEMA_NAME = " SCHEMA_NAME ";
	public static final String SCOPE = " SCOPE ";
	public static final String SCROLL = " SCROLL ";
	public static final String SEARCH = " SEARCH ";
	public static final String SECOND = " SECOND ";
	public static final String SECTION = " SECTION ";
	public static final String SECURITY = " SECURITY ";
	public static final String SELECT = " SELECT ";
	public static final String SELF = " SELF ";
	public static final String SENSITIVE = " SENSITIVE ";
	public static final String SEQUENCE = " SEQUENCE ";
	public static final String SERIALIZABLE = " SERIALIZABLE ";
	public static final String SERVER_NAME = " SERVER_NAME ";
	public static final String SESSION = " SESSION ";
	public static final String SESSION_USER = " SESSION_USER ";
	public static final String SET = " SET ";
	public static final String SETOF = " SETOF ";
	public static final String SETS = " SETS ";
	public static final String SHARE = " SHARE ";
	public static final String SHOW = " SHOW ";
	public static final String SIMILAR = " SIMILAR ";
	public static final String SIMPLE = " SIMPLE ";
	public static final String SIZE = " SIZE ";
	public static final String SMALLINT = " SMALLINT ";
	public static final String SOME = " SOME ";
	public static final String SOURCE = " SOURCE ";
	public static final String SPACE = " SPACE ";
	public static final String SPECIFIC = " SPECIFIC ";
	public static final String SPECIFICTYPE = " SPECIFICTYPE ";
	public static final String SPECIFIC_NAME = " SPECIFIC_NAME ";
	public static final String SQL = " SQL ";
	public static final String SQLCODE = " SQLCODE ";
	public static final String SQLERROR = " SQLERROR ";
	public static final String SQLEXCEPTION = " SQLEXCEPTION ";
	public static final String SQLSTATE = " SQLSTATE ";
	public static final String SQLWARNING = " SQLWARNING ";
	public static final String START = " START ";
	public static final String STATE = " STATE ";
	public static final String STATEMENT = " STATEMENT ";
	public static final String STATIC = " STATIC ";
	public static final String STATISTICS = " STATISTICS ";
	public static final String STDIN = " STDIN ";
	public static final String STDOUT = " STDOUT ";
	public static final String STRUCTURE = " STRUCTURE ";
	public static final String STYLE = " STYLE ";
	public static final String SUBCLASS_ORIGIN = " SUBCLASS_ORIGIN ";
	public static final String SUBLIST = " SUBLIST ";
	public static final String SUBSTRING = " SUBSTRING ";
	public static final String SUM = " SUM ";
	public static final String SYMMETRIC = " SYMMETRIC ";
	public static final String SYSID = " SYSID ";
	public static final String SYSTEM = " SYSTEM ";
	public static final String SYSTEM_USER = " SYSTEM_USER ";
	public static final String TABLE = " TABLE ";
	public static final String TEMP = " TEMP ";
	public static final String TEMPLATE = " TEMPLATE ";
	public static final String TEMPORARY = " TEMPORARY ";
	public static final String TERMINATE = " TERMINATE ";
	public static final String THAN = " THAN ";
	public static final String THEN = " THEN ";
	public static final String TIME = " TIME ";
	public static final String TIMESTAMP = " TIMESTAMP ";
	public static final String TIMEZONE_HOUR = " TIMEZONE_HOUR ";
	public static final String TIMEZONE_MINUTE = " TIMEZONE_MINUTE ";
	public static final String TO = " TO ";
	public static final String TOAST = " TOAST ";
	public static final String TRAILING = " TRAILING ";
	public static final String TRANSACTION = " TRANSACTION ";
	public static final String TRANSACTIONS_COMMITTED = " TRANSACTIONS_COMMITTED ";
	public static final String TRANSACTIONS_ROLLED_BACK = " TRANSACTIONS_ROLLED_BACK ";
	public static final String TRANSACTION_ACTIVE = " TRANSACTION_ACTIVE ";
	public static final String TRANSFORM = " TRANSFORM ";
	public static final String TRANSFORMS = " TRANSFORMS ";
	public static final String TRANSLATE = " TRANSLATE ";
	public static final String TRANSLATION = " TRANSLATION ";
	public static final String TREAT = " TREAT ";
	public static final String TRIGGER = " TRIGGER ";
	public static final String TRIGGER_CATALOG = " TRIGGER_CATALOG ";
	public static final String TRIGGER_NAME = " TRIGGER_NAME ";
	public static final String TRIGGER_SCHEMA = " TRIGGER_SCHEMA ";
	public static final String TRIM = " TRIM ";
	public static final String TRUE = " TRUE ";
	public static final String TRUNCATE = " TRUNCATE ";
	public static final String TRUSTED = " TRUSTED ";
	public static final String TYPE = " TYPE ";
	public static final String UNCOMMITTED = " UNCOMMITTED ";
	public static final String UNDER = " UNDER ";
	public static final String UNENCRYPTED = " UNENCRYPTED ";
	public static final String UNION = " UNION ";
	public static final String UNIQUE = " UNIQUE ";
	public static final String UNKNOWN = " UNKNOWN ";
	public static final String UNLISTEN = " UNLISTEN ";
	public static final String UNNAMED = " UNNAMED ";
	public static final String UNNEST = " UNNEST ";
	public static final String UNTIL = " UNTIL ";
	public static final String UPDATE = " UPDATE ";
	public static final String UPPER = " UPPER ";
	public static final String USAGE = " USAGE ";
	public static final String USER = " USER ";
	public static final String USER_DEFINED_TYPE_CATALOG = " USER_DEFINED_TYPE_CATALOG ";
	public static final String USER_DEFINED_TYPE_NAME = " USER_DEFINED_TYPE_NAME ";
	public static final String USER_DEFINED_TYPE_SCHEMA = " USER_DEFINED_TYPE_SCHEMA ";
	public static final String USING = " USING ";
	public static final String VACUUM = " VACUUM ";
	public static final String VALID = " VALID ";
	public static final String VALUE = " VALUE ";
	public static final String VALUES = " VALUES ";
	public static final String VARCHAR = " VARCHAR ";
	public static final String VARIABLE = " VARIABLE ";
	public static final String VARYING = " VARYING ";
	public static final String VERBOSE = " VERBOSE ";
	public static final String VERSION = " VERSION ";
	public static final String VIEW = " VIEW ";
	public static final String WHEN = " WHEN ";
	public static final String WHENEVER = " WHENEVER ";
	public static final String WHERE = " WHERE ";
	public static final String WITH = " WITH ";
	public static final String WITHOUT = " WITHOUT ";
	public static final String WORK = " WORK ";
	public static final String WRITE = " WRITE ";
	public static final String YEAR = " YEAR ";
	public static final String ZONE = " ZONE ";

	// usefull function
	public static String simpleReplaceDangerous(String str){
		str=str.replaceAll(";","")
		       .replaceAll("&","&amp;")
		       .replaceAll("<","&lt;")
		       .replaceAll(">","&gt;")
		       .replaceAll("'","''")
		       .replaceAll("--","")
		       .replaceAll("/","")
		       .replaceAll("%","");
		return str;
	}
	public static final String array(String... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (String str : arr) {
			if (StringUtils.isEmpty(str) ) {
				builder.append("'',");
			} else {
				builder.append("'").append(simpleReplaceDangerous(str)).append("',");
			}
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}

		return "(" +builder.toString() + ")";
	}

	public static final String array(int... arr) {
 		if (arr == null || arr.length == 0) {
 			return "";
	    }
		StringBuilder builder = new StringBuilder(200);
		for (int i : arr) {
			builder.append(i).append(',');
		}
		builder.setLength(builder.length() - 1);
		return "(" +builder.toString() + ")";
	}

	public static final String array(Integer... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (Integer i : arr) {
			if (i == null ) {
				continue;
			}
			builder.append(i).append(',');

		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}

		return "(" +builder.toString() + ")";
	}

	public static final String array(long... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (long i : arr) {
			builder.append(i).append(',');
		}
		builder.setLength(builder.length() - 1);
		return "(" +builder.toString() + ")";
	}

	public static final String array(Long... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (Long i : arr) {
			if (i == null) {
				continue;
			}
			builder.append(i).append(',');
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}
		return "(" +builder.toString() + ")";
	}

	public static final String array(short... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (short i : arr) {
			builder.append(i).append(',');
		}
		builder.setLength(builder.length() - 1);
		return "(" +builder.toString() + ")";
	}

	public static final String array(Short... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (Short i : arr) {
			if (i == null) {
				continue;
			}
			builder.append(i).append(',');
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}
		return "(" +builder.toString() + ")";
	}

	public static final String array(byte... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (byte i : arr) {
			builder.append(i).append(',');
		}
		builder.setLength(builder.length() - 1);
		return "(" +builder.toString() + ")";
	}

	public static final String array(Byte... arr) {
		if (arr == null || arr.length == 0) {
			return "";
		}
		StringBuilder builder = new StringBuilder(200);
		for (Byte i : arr) {
			if (i == null) {
				continue;
			}
			builder.append(i).append(',');
		}
		if (builder.length() > 0) {
			builder.setLength(builder.length() - 1);
		}
		return "(" +builder.toString() + ")";
	}


}