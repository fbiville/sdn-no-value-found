package com.example.demo;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataNeo4jTest
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
    void creates_node(@Autowired TestNodeRepository repository,
                      @Autowired Neo4jClient neo4jClient) {

        TestNode testNode = repository.createTestNode("some-name");

        Optional<String> result = neo4jClient.query("MATCH (t:TestNode) RETURN t.name AS name")
                .fetchAs(String.class)
                .one();
        assertThat(testNode.getId()).isGreaterThanOrEqualTo(0);
        assertThat(testNode.getName()).isEqualTo("some-name");
        assertThat(result).hasValue("some-name");
    }

    private org.neo4j.driver.Record singleRecordOf(Session session, String query) {
        return session.run(query).single();
    }
}