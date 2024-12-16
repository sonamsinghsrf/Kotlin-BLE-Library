/*
 * Copyright (c) 2024, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.ble.core.util

import java.util.Locale
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Bluetooth UUIDs are 128-bit values used to uniquely identify services, characteristics, and
 * descriptors. They are defined by the Bluetooth Special Interest Group (SIG).
 *
 * The Bluetooth SIG defines a number of base UUIDs that are used to create other UUIDs.
 * The most common base UUID is the Bluetooth Base UUID, which is used to create UUIDs for
 * standard Bluetooth services and profiles.
 *
 * The Bluetooth Base UUID is defined as `00000000-0000-1000-8000-00805F9B34FB`.
 *
 * The 128-bit UUIDs are divided into four categories:
 * - 16-bit UUIDs
 * - 32-bit UUIDs
 * - 128-bit UUIDs
 *
 * The 16-bit and 32-bit UUIDs are derived from the 128-bit UUIDs using the Bluetooth Base UUID.
 * The 128-bit UUIDs are used for custom services and characteristics.
 *
 * The 16-bit UUIDs are used for standard Bluetooth services and profiles, such as the
 * Heart Rate Service, Battery Service, etc. The 16-bit UUIDs are defined by the Bluetooth SIG.
 *
 * The 32-bit UUIDs are used for custom services and profiles. The 32-bit UUIDs are generated
 * by the developer and are not defined by the Bluetooth SIG.
 *
 * The 128-bit UUIDs are used for custom services and characteristics. The 128-bit UUIDs are
 * generated by the developer and are not defined by the Bluetooth SIG.
 */
@OptIn(ExperimentalUuidApi::class)
val Uuid.Companion.baseUuid: Uuid
    /*
     * Note:
     *
     * What is "-0x7FFFFF7FA064CB05", you ask?
     *
     * Due to the fact, that the least significant part of the Base Bluetooth UUID
     * (0x800000805f9b34fb) is outside of the range of Long, the value cannot be
     * simply written as UUID(0x0000000000001000, 0x800000805f9B34FB).
     * Instead, we take a 2's complement of the least significant part and invert the sign.
     */
    get() = fromLongs(0x0000000000001000, -0x7FFFFF7FA064CB05)

/**
 * Check whether the given UUID is a 16-bit or 32-bit UUID.
 */
@OptIn(ExperimentalUuidApi::class)
val Uuid.isShortUuid: Boolean
    get() = is16BitUuid || is32BitUuid

/**
 * Check whether the given UUID is a 16-bit UUID.
 */
@OptIn(ExperimentalUuidApi::class)
val Uuid.is16BitUuid: Boolean
    get() = toLongs { msb, lsb ->
        if (lsb != -0x7FFFFF7FA064CB05) {
            return false
        }
        return msb and -0xffff00000001L == 0x1000L
    }

/**
 * Check whether the given UUID is a 32-bit UUID.
 */
@OptIn(ExperimentalUuidApi::class)
val Uuid.is32BitUuid: Boolean
    get() = toLongs { msb, lsb ->
        if (lsb != -0x7FFFFF7FA064CB05) {
            return false
        }
        return !is16BitUuid && msb and 0xFFFFFFFFL == 0x1000L
    }

/**
 * Extract the 16-bit or 32-bit Service Identifier of the 128-bit UUID.
 *
 * For example, if `0000110B-0000-1000-8000-00805F9B34FB` is the parcel UUID, this
 * function will return `0x110B` as [Int].
 */
@OptIn(ExperimentalUuidApi::class)
val Uuid.shortUuid: Int?
    get() = toLongs { msb, _ ->
        if (!is32BitUuid && !is16BitUuid) return@toLongs null
        (msb and -0x100000000L ushr 32).toInt()
    }

/**
 * Creates a 128-bit UUID from a 16-bit or 32-bit UUID using [Bluetooth Base UUID][baseUuid].
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.fromShortUuid(shortUuid: Int): Uuid = baseUuid.toLongs { msb, lsb ->
    fromLongs(
        msb or (shortUuid.toLong() shl 32),
        lsb
    )
}

/**
 * Returns a string representation of the UUID.
 *
 * If the UUID is a 16-bit or 32-bit UUID, the string will be in the form of `ABCD` or `ABCDEFGH`,
 * otherwise it will be in the form of standard UUID with dashes.
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.toShortString(): String = shortUuid
    ?.toString(16)
    ?.padStart((if (is16BitUuid) 4 else 8), '0')?.uppercase(Locale.US)
    ?: toString().uppercase(Locale.US)

/**
 * Returns the UUID as Byte Array, in Little Endian.
 *
 * UUID matching the [baseUuid] are returned as 2 or 4 bytes, depending on the type.
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.toShortByteArray(): ByteArray {
    val bytes = toByteArray()
    return when {
        is16BitUuid -> bytes.sliceArray(2..3).reversedArray()
        is32BitUuid -> bytes.sliceArray(0..3).reversedArray()
        else -> bytes
    }
}

/**
 * Converts 2, 4 or 16 byte array to a UUID using Little Endian byte order.
 *
 * Example:
 * * `0x0918` -> `00001809-0000-1000-8000-00805F9B34FB`
 * * `0x78563412` -> `12345678-0000-1000-8000-00805F9B34FB`
 * * `0x58B29CC8812877B8E7490C1300783814` -> `14383800-30C1-49E7-B877-1281C89CB258`
 *
 * @param uuidBytes The UUID as a byte array. The byte array must have the exact size.
 * @return The UUID.
 * @throws IllegalArgumentException if the byte array is too short or too long.
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.fromBytes(uuidBytes: ByteArray): Uuid {
    return fromBytes(uuidBytes, 0, uuidBytes.size)
}

/**
 * Converts 2, 4 or 16 byte array to a UUID using Little Endian byte order.
 *
 * Example:
 * * `0x0918` -> `00001809-0000-1000-8000-00805F9B34FB`
 * * `0x78563412` -> `12345678-0000-1000-8000-00805F9B34FB`
 * * `0x58B29CC8812877B8E7490C1300783814` -> `14383800-30C1-49E7-B877-1281C89CB258`
 *
 * @param uuidBytes The UUID as a byte array. The byte array must have the exact size.
 * @param offset The offset in the byte array.
 * @param length The number of bytes to read.
 * @return The UUID.
 * @throws IllegalArgumentException if the byte array is too short or too long.
 */
@OptIn(ExperimentalUuidApi::class)
fun Uuid.Companion.fromBytes(uuidBytes: ByteArray, offset: Int, length: Int): Uuid {
    require(length == 2 || length == 4 || length == 16) { "Cannot convert $length bytes to a UUID" }
    require(uuidBytes.size >= offset + length) { "Byte array too short: ${uuidBytes.size}, offset: $offset, length: $length" }
    if (length == 2) {
        val uuidVal =
            (uuidBytes[offset].toInt() and 0xFF) or
            (uuidBytes[offset + 1].toInt() and 0xFF shl 8)
        return fromShortUuid(uuidVal)
    }
    if (length == 4) {
        val uuidVal =
            (uuidBytes[offset].toInt() and 0xFF) or
            (uuidBytes[offset + 1].toInt() and 0xFF shl 8) or
            (uuidBytes[offset + 2].toInt() and 0xFF shl 16) or
            (uuidBytes[offset + 3].toInt() and 0xFF shl 24)
        return fromShortUuid(uuidVal)
    }
    return fromLongs(uuidBytes.toLong(startIndex = offset), uuidBytes.toLong(startIndex = offset + 8))
}

private fun ByteArray.toLong(startIndex: Int): Long {
    return ((this[startIndex + 0].toLong() and 0xFF) shl 56) or
           ((this[startIndex + 1].toLong() and 0xFF) shl 48) or
           ((this[startIndex + 2].toLong() and 0xFF) shl 40) or
           ((this[startIndex + 3].toLong() and 0xFF) shl 32) or
           ((this[startIndex + 4].toLong() and 0xFF) shl 24) or
           ((this[startIndex + 5].toLong() and 0xFF) shl 16) or
           ((this[startIndex + 6].toLong() and 0xFF) shl 8) or
            (this[startIndex + 7].toLong() and 0xFF)
}