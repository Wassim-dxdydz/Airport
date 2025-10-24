package fr.uga.miage.m1.repositories

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.sql.init.schema-locations=classpath:schema.sql"
])
abstract class RepoBase