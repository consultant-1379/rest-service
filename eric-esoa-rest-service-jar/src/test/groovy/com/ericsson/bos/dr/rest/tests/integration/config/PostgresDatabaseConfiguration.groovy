/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.bos.dr.rest.tests.integration.config

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

import javax.sql.DataSource

@TestConfiguration
@Profile("test-pg")
class PostgresDatabaseConfiguration {

    @Bean(destroyMethod = "close")
    EmbeddedPostgres embeddedPostgresDS(@Value("\${db.port}") int port) {
        return EmbeddedPostgres.builder().setPort(port).start()
    }

    @Bean
    @Primary
    DataSource dataSource(EmbeddedPostgres embeddedPostgres, @Value("\${spring.datasource.username}") String user,
                          @Value("\${spring.datasource.password}") String password) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.postgresql.Driver")
        dataSourceBuilder.url(embeddedPostgres.getJdbcUrl(user, password))
        return dataSourceBuilder.build();
    }
}
