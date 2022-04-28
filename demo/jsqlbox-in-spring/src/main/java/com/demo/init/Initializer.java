package com.demo.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.demo.model.Team;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

public class Initializer implements WebApplicationInitializer {

	public void onStartup(ServletContext servletContext) throws ServletException {
 
		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(WebAppConfig.class);
		servletContext.addListener(new ContextLoaderListener(ctx));

		ctx.setServletContext(servletContext);

		Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
		servlet.addMapping("/");
		servlet.setLoadOnStartup(1);
		
		ctx.refresh();// force refresh
		
		//SqlBoxContext.setGlobalAllowShowSql(true);
		SqlBoxContext sqlCtx = ctx.getBean(SqlBoxContext.class);
		SqlBoxContext.setGlobalSqlBoxContext(sqlCtx);

		String[] ddls = sqlCtx.toDropAndCreateDDL(Team.class);
		for (String ddl : ddls)
			sqlCtx.quiteExecute(ddl);
		for (int i = 0; i < 5; i++)
			new Team().put("name", "Team" + i, "rating", i * 10).insert();
		System.out.println("========== com.jsqlboxdemo.init.Initializer initialized=====");
	}

}
