package fr.uga.miage.m1.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class RemoteAirportClientConfig {

    @Bean
    fun remoteAirportWebClient(
        @Value("\${remote.airport.base-url}") baseUrl: String
    ): WebClient {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}
