/*
 * Copyright 2003 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.cglib3_2_0.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import com.github.drinkjava2.asm5_0_3.ClassVisitor;
import com.github.drinkjava2.asm5_0_3.Type;
import com.github.drinkjava2.cglib3_2_0.core.AbstractClassGenerator;
import com.github.drinkjava2.cglib3_2_0.core.ClassEmitter;
import com.github.drinkjava2.cglib3_2_0.core.CodeEmitter;
import com.github.drinkjava2.cglib3_2_0.core.Constants;
import com.github.drinkjava2.cglib3_2_0.core.EmitUtils;
import com.github.drinkjava2.cglib3_2_0.core.KeyFactory;
import com.github.drinkjava2.cglib3_2_0.core.ReflectUtils;
import com.github.drinkjava2.cglib3_2_0.core.TypeUtils;

/**
 * @author Chris Nokleberg
 * @version $Id: ConstructorDelegate.java,v 1.20 2006/03/05 02:43:19 herbyderby Exp $
 */
@SuppressWarnings({"rawtypes","unchecked"})  
abstract public class ConstructorDelegate {
    private static final ConstructorKey KEY_FACTORY =
      (ConstructorKey)KeyFactory.create(ConstructorKey.class, KeyFactory.CLASS_BY_NAME);
    
    interface ConstructorKey {
        public Object newInstance(String declaring, String iface);
    }

    protected ConstructorDelegate() {
    }

    public static ConstructorDelegate create(Class targetClass, Class iface) {
        Generator gen = new Generator();
        gen.setTargetClass(targetClass);
        gen.setInterface(iface);
        return gen.create();
    }

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(ConstructorDelegate.class.getName());
        private static final Type CONSTRUCTOR_DELEGATE =
          TypeUtils.parseType("com.github.drinkjava2.cglib3_2_0.reflect.ConstructorDelegate");

        private Class iface;
        private Class targetClass;

        public Generator() {
            super(SOURCE);
        }

        public void setInterface(Class iface) {
            this.iface = iface;
        }

        public void setTargetClass(Class targetClass) {
            this.targetClass = targetClass;
        }

        public ConstructorDelegate create() {
            setNamePrefix(targetClass.getName());
            Object key = KEY_FACTORY.newInstance(iface.getName(), targetClass.getName());
            return (ConstructorDelegate)super.create(key);
        }

        protected ClassLoader getDefaultClassLoader() {
            return targetClass.getClassLoader();
        }

        protected ProtectionDomain getProtectionDomain() {
        	return ReflectUtils.getProtectionDomain(targetClass);
        }

        public void generateClass(ClassVisitor v) {
            setNamePrefix(targetClass.getName());

            final Method newInstance = ReflectUtils.findNewInstance(iface);
            if (!newInstance.getReturnType().isAssignableFrom(targetClass)) {
                throw new IllegalArgumentException("incompatible return type");
            }
            final Constructor constructor;
            try {
                constructor = targetClass.getDeclaredConstructor(newInstance.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("interface does not match any known constructor");
            }

            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.V1_2,
                           Constants.ACC_PUBLIC,
                           getClassName(),
                           CONSTRUCTOR_DELEGATE,
                           new Type[]{ Type.getType(iface) },
                           Constants.SOURCE_FILE);
            Type declaring = Type.getType(constructor.getDeclaringClass());
            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC,
                                            ReflectUtils.getSignature(newInstance),
                                            ReflectUtils.getExceptionTypes(newInstance));
            e.new_instance(declaring);
            e.dup();
            e.load_args();
            e.invoke_constructor(declaring, ReflectUtils.getSignature(constructor));
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        protected Object nextInstance(Object instance) {
            return instance;
        }
    }
}
