package com.moodprint.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MoodprintBackendApplication

fun main(args: Array<String>) {
    runApplication<MoodprintBackendApplication>(*args)
}
