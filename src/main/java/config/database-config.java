package config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Database Configuration
 * Configures JPA, Hibernate, and transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = {"com.example.repository"})
@EntityScan(basePackages = {"com.example.entity"})
@EnableTransactionManagement
@EnableJpaAuditing
public class DatabaseConfig {
    
    // Database configuration is managed via application.yml or application.properties
    // Commonly configured properties:
    
    /*
     * spring.datasource.url=jdbc:mysql://localhost:3306/database_name
     * spring.datasource.username=root
     * spring.datasource.password=password
     * spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
     * 
     * spring.jpa.hibernate.ddl-auto=update
     * spring.jpa.show-sql=true
     * spring.jpa.properties.hibernate.format_sql=true
     * spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
     * 
     * Hibernate Configuration:
     * - ddl-auto: validate, update, create, create-drop
     * - show-sql: Print SQL queries to console
     * - format_sql: Format SQL output
     */
}
