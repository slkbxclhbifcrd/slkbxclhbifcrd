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
package com.github.drinkjava2.jdbpro;

/**
 * The interface for DbContext.tx method use
 * 
 * <pre>
 * Usage:
 * DbContext db=new DbContext(ds);
 * db.tx(new TxBody(){ 
 * 		public void run(){ 
 * 			new User().setName("Tom").insert();
 * 			db.iExecute("delete from order where userId=?",param("Sam"));
 * 	   }}); 
 * 
 * or 
 * 
 * boolean result=db.tryTx(()->{new User().setName("Tom").insert();
 * 			  db.iExecute("delete from order where userId=?",param("Sam"));  
 *     });
 * 
 * </pre>
 */
public interface TxBody {
	public void run();
}