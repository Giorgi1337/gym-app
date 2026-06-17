package com.gym.config;

import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.gym")
@PropertySource("classpath:hibernate.properties")
@EnableAspectJAutoProxy
public class AppConfig {

    private final Environment env;

    public AppConfig(Environment env) {
        this.env = env;
    }

    @Bean
    @DependsOn("flyway")
    public LocalSessionFactoryBean sessionFactoryBean() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setPackagesToScan("com.gym.model");
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public Flyway flyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        env.getRequiredProperty("hibernate.connection.url"),
                        env.getRequiredProperty("hibernate.connection.username"),
                        env.getRequiredProperty("hibernate.connection.password")
                )
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.setProperty("hibernate.connection.driver_class", env.getRequiredProperty("hibernate.connection.driver_class"));
        props.setProperty("hibernate.connection.url", env.getRequiredProperty("hibernate.connection.url"));
        props.setProperty("hibernate.connection.username", env.getRequiredProperty("hibernate.connection.username"));
        props.setProperty("hibernate.connection.password", env.getRequiredProperty("hibernate.connection.password"));

        props.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        props.setProperty("hibernate.hikari.minimumIdle", env.getRequiredProperty("hikari.minimumIdle"));
        props.setProperty("hibernate.hikari.maximumPoolSize", env.getRequiredProperty("hikari.maximumPoolSize"));
        props.setProperty("hibernate.hikari.idleTimeout", env.getRequiredProperty("hikari.idleTimeout"));
        props.setProperty("hibernate.hikari.connectionTimeout", env.getRequiredProperty("hikari.connectionTimeout"));
        props.setProperty("hibernate.hikari.maxLifetime", env.getRequiredProperty("hikari.maxLifetime"));

        props.setProperty("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
//        props.setProperty("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));
//        props.setProperty("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));
        props.setProperty("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));

        return props;
    }
}