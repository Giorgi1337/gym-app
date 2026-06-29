package com.gym;

import com.gym.config.AppConfig;
import com.gym.config.WebConfig;
import com.gym.filter.RequestResponseLoggingFilter;
import com.gym.filter.TransactionIdFilter;
import jakarta.servlet.Filter;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class GymApplication {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));

        // Order matters: transactionId must be established before request/response
        // logging runs, so every REQUEST/RESPONSE log line carries it via MDC.
        registerFilter(context, "transactionIdFilter", new TransactionIdFilter());
        registerFilter(context, "requestResponseLoggingFilter", new RequestResponseLoggingFilter());

        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.register(AppConfig.class, WebConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
        tomcat.addServlet("", "dispatcher", dispatcherServlet);
        context.addServletMappingDecoded("/", "dispatcher");

        System.out.println("Starting Gym CRM Application on port 8080...");
        tomcat.start();
        tomcat.getServer().await();
    }

    private static void registerFilter(Context context, String name, Filter filter) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(name);
        filterDef.setFilter(filter);
        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(name);
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);
    }
}