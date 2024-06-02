package br.com.farabolini.challenge.integration;

import org.assertj.core.util.Arrays;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("integration-test")
public class TestContainersTest {

    private static final String DATABASE = "challenge-itau";
    private static final int REDIS_PORT = 6379;

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:alpine")
            .withDatabaseName(DATABASE)
            .withUsername(DATABASE)
            .withPassword(DATABASE);

    protected static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.2.5"))
            .withExposedPorts(REDIS_PORT);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void addProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("redis.address", () -> Arrays.array(String.format("%s:%S", REDIS.getHost(), REDIS.getFirstMappedPort())));
    }

}
