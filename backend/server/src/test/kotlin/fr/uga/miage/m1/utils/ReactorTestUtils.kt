package fr.uga.miage.m1.utils

import reactor.core.publisher.Hooks

object ReactorTestUtils {
    fun enableDebug() { Hooks.onOperatorDebug() }
}
