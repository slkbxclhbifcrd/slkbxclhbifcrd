/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.drinkjava2.jbeanbox.EMPTY;

/**
 * INJECT used to inject BeanBox class for class, fields, method, parameter
 * 
 * @author Yong Zhu
 * @since 2.4.7
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER })
public @interface INJECT {

	public Class<?> value() default EMPTY.class; // In fact is BeanBox's target field

	public boolean pureValue() default false; // if true mean values is a pure value, otherwise value is a target

	public boolean required() default true; // if true when target not found will throw exception, if false keep silence

}
