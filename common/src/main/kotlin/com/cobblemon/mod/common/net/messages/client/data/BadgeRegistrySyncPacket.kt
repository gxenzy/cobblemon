/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.data

import com.cobblemon.mod.common.api.badges.Badge
import com.cobblemon.mod.common.api.badges.Badges
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf

class BadgeRegistrySyncPacket(badges: List<Badge>) : DataRegistrySyncPacket<Badge, BadgeRegistrySyncPacket>(badges) {
    companion object {
        val ID = cobblemonResource("badges")
        fun decode(buffer: RegistryFriendlyByteBuf) = BadgeRegistrySyncPacket(emptyList()).apply { decodeBuffer(buffer) }
    }


    override val id = ID
    override fun encodeEntry(buffer: RegistryFriendlyByteBuf, entry: Badge) {
        buffer.writeIdentifier(entry.identifier)
        buffer.writeString(entry.name)
        buffer.writeResourceLocation(entry.texture)
        buffer.writeInt(entry.order)
    }

    override fun decodeEntry(buffer: RegistryFriendlyByteBuf): Badge {
        return Badge(
            order = buffer.readInt(),
            texture = buffer.readResourceLocation(),
            name = buffer.readString(),
            identifier = buffer.readIdentifier()
        )
    }

    override fun synchronizeDecoded(entries: Collection<Badge>) {
        Badges.reload(entries.associateBy { it.identifier })
    }
}