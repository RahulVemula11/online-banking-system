package com.rahul.banking.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Builds the database connection for production (Render) by reading Render's
 * DATABASE_URL directly and converting it to a proper JDBC URL in Java.
 *
 * Render provides: postgresql://user:password@host:port/dbname
 * JDBC needs:      jdbc:postgresql://host:port/dbname  (+ user/password set separately, + SSL)
 *
 * Doing this in Java (not a shell script) avoids Windows line-ending issues and
 * is robust regardless of how the platform formats the variable.
 */
@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            // Fall back to explicitly provided JDBC vars if present
            String jdbc = System.getenv("JDBC_DATABASE_URL");
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(jdbc);
            ds.setUsername(System.getenv("DB_USERNAME"));
            ds.setPassword(System.getenv("DB_PASSWORD"));
            ds.setDriverClassName("org.postgresql.Driver");
            return ds;
        }

        // Parse postgresql://user:password@host:port/dbname
        URI uri = URI.create(databaseUrl);
        String userInfo = uri.getUserInfo();              // "user:password"
        String username = userInfo.split(":")[0];
        String password = userInfo.contains(":") ? userInfo.split(":", 2)[1] : "";
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + "?sslmode=require";

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);   // free tier has limited connections
        return ds;
    }
}
