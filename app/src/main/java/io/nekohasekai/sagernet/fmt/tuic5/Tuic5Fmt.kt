/******************************************************************************
 * Copyright (C) 2022 by nekohasekai <contact-git@sekai.icu>                  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.fmt.tuic5

import cn.hutool.json.JSONArray
import cn.hutool.json.JSONObject
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.fmt.LOCALHOST
import io.nekohasekai.sagernet.ktx.isIpAddress
import java.io.File

fun Tuic5Bean.buildTuic5Config(port: Int, cacheFile: (() -> File)?): String {
    return JSONObject().also {
        it["relay"] = JSONObject().also {
            if (sni.isNotBlank()) {
                it["server"] = sni
                it["ip"] = finalAddress
            } else if (serverAddress.isIpAddress()) {
                it["server"] = finalAddress
            } else {
                it["server"] = serverAddress
                it["ip"] = finalAddress
            }
            it["port"] = finalPort
            it["uuid"] = uuid
            it["password"] = password

            if (caText.isNotBlank() && cacheFile != null) {
                val caFile = cacheFile()
                caFile.writeText(caText)
                it["certificates"] = JSONArray().apply {
                    put(caFile.absolutePath)
                }
            }

            it["udp_relay_mode"] = udpRelayMode
            if (alpn.isNotBlank()) {
                it["alpn"] = JSONArray(alpn.split("\n"))
            }
            it["congestion_control"] = congestionControl
            it["disable_sni"] = disableSNI
            it["zero_rtt_handshake"] = zeroRTTHandshake
        }
        it["local"] = JSONObject().also {
            it["server"] = LOCALHOST + ":" + port
            it["max_packet_size"] = mtu
        }
        it["log_level"] = if (DataStore.enableLog) "debug" else "info"
    }.toStringPretty()
}