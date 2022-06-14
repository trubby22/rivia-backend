package me.rivia.api.handlers

import aws.smithy.kotlin.runtime.util.encodeToHex
import kotlin.random.Random

private const val UID_SIZE = 16

fun generateUid() = Random.Default.nextBytes(UID_SIZE).encodeToHex()
