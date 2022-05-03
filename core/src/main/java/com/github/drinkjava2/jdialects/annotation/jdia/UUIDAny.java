/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;

/**
 * Defines a primary key generator that may be referenced by name when
 * a generator element is specified for the {@link GeneratedValue}
 * annotation. A UUIDAny generator may be specified on the entity
 * class or on the primary key field or property. The scope of the
 * generator name is global to the persistence unit (across all
 * generator types).
 *
 * <pre>
 *   Example:
 *
 *   &#064;UUIDAny(name="uuid_100", length=100)
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE, FIELD}) 
@Retention(RUNTIME)
public @interface UUIDAny {
	/**
	 * (Required) A unique generator name that can be referenced by one or more
	 * classes to be the generator for primary key values.
	 */
	String name();

	/**
	 * The length of the UUID
	 */
	int length() default 20;
}
