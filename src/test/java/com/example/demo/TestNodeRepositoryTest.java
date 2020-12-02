package com.example.demo;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.AccessMode;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataNeo4jTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TestNodeRepositoryTest {

    @Container
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:4.2")
            .withAdminPassword("foobar");

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @Test
    void creates_node(@Autowired TestNodeRepository repository, @Autowired Driver driver) {
        TestNode testNode = repository.createTestNode("some-name");

        assertThat(testNode.getId()).isGreaterThanOrEqualTo(0);
        assertThat(testNode.getName()).isEqualTo("some-name");
        try (Session session = driver.session(SessionConfig.builder().withDefaultAccessMode(AccessMode.READ).build())) {
            String name = singleRecordOf(session, "MATCH (t:TestNode) RETURN t.name AS name").get("name").asString();
            assertThat(name).isEqualTo("some-name");
        }
    }

    private org.neo4j.driver.Record singleRecordOf(Session session, String query) {
        return session.run(query).single();
    }
}