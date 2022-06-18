package me.rivia.api.handlers

import kotlin.random.Random

private const val UID_SIZE = 16

fun generateUid() = Random.Default.nextBytes(UID_SIZE).let {String.format("0x%02X", it)}
