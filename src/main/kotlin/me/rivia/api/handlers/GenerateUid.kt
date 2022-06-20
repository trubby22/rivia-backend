package me.rivia.api.handlers

import kotlin.random.Random

private const val UID_SIZE = 16

fun generateUid() = Random.Default.nextBytes(UID_SIZE).joinToString("") {String.format("%02x", it)}
