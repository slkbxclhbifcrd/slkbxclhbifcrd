/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.helloworld;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;

import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class HelloWorld extends ActiveRecord<HelloWorld> {
    @Id
    @Column(length = 20)
    private String name;

    public String getName() {
        return name;
    }

    public HelloWorld setName(String name) {
        this.name = name;
        return this;
    }

    public static void main(String[] args) {
        DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
        DbContext.setGlobalNextAllowShowSql(true);
        DbContext ctx = new DbContext(ds);
        DbContext.setGlobalDbContext(ctx);
        ctx.executeDDL(ctx.toCreateDDL(HelloWorld.class));
        new HelloWorld().setName("Hellow jSqlBox").insert();
        String sql = DB.trans("select qt(name) from qt(helloWorld) where qt(name) like ?");
        String param = "He%";
        System.out.println(DB.qryString(sql, DB.par(param)));
        ctx.executeDDL(ctx.toDropDDL(HelloWorld.class));
    }
}