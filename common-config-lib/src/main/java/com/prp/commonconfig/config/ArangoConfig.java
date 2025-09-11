package com.prp.commonconfig.config;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArangoConfig implements ArangoConfiguration {

    @Value("${arangodb.spring.data.hosts}")
    private String host;

    @Value("${arangodb.spring.data.port}")
    private int port;

    @Value("${arangodb.spring.data.user}")
    private String user;

    @Value("${arangodb.spring.data.password}")
    private String password;

    @Value("${arangodb.spring.data.useSsl:false}")
    private boolean useSsl;

    @Value("${arangodb.spring.data.database}")
    private String database;

    @Override
    public String database() {
        return database;
    }

    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder()
                .host(host, port)
                .user(user)
                .password(password)
                .useSsl(useSsl);
    }
}
