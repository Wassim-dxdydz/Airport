package fr.uga.miage.m1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["fr.uga.miage.m1"])
class PcProjectApplication

fun main(args: Array<String>) {
	runApplication<PcProjectApplication>(*args)
}
