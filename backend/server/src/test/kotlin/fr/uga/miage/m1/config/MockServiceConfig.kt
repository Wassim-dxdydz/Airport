package fr.uga.miage.m1.config

import fr.uga.miage.m1.domain.service.*
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MockServiceConfig {

    @Bean
    fun avionService(): AvionService = mockk(relaxed = true)

    @Bean
    fun hangarService(): HangarService = mockk(relaxed = true)

    @Bean
    fun pisteService(): PisteService = mockk(relaxed = true)

    @Bean
    fun volService(): VolService = mockk(relaxed = true)
}
