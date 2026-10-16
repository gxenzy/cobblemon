/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.storage.player.adapter

import com.cobblemon.mod.common.api.badges.BadgeManager
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.util.adapters.CodecBackedAdapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mongodb.client.MongoClient
import java.util.UUID

class BadgeMongoBackend(
    mongoClient: MongoClient,
    databaseName: String,
    collectionName: String
) : MongoBackedPlayerDataStoreBackend<BadgeManager>(
    mongoClient,
    databaseName,
    collectionName,
    PlayerInstancedDataStoreTypes.BADGES
) {
    override val gson = GsonBuilder()
        .registerTypeAdapter(BadgeManager::class.java, CodecBackedAdapter(BadgeManager.CODEC))
        .create()
    override val classToken = TypeToken.get(BadgeManager::class.java)
    override val defaultData = defaultDataFunc

    companion object {
        val defaultDataFunc = { uuid: UUID -> BadgeManager(uuid) }
    }
}
