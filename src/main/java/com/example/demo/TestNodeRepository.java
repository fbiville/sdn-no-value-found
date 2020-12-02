package com.example.demo;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface TestNodeRepository extends Neo4jRepository<TestNode, Long> {

    @Query("CREATE (n:TestNode {name: $name}) RETURN n")
    TestNode createTestNode(String name);
}