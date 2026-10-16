package com.cobblemon.mod.common.api.storage.player.client

import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeString
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation

class ClientBadgeManager(
    val obtainedBadges: MutableSet<ResourceLocation>
) : ClientInstancedPlayerData {

    override fun encode(buf: RegistryFriendlyByteBuf) {
        buf.writeCollection(obtainedBadges) { _, id, -> buf.writeString(id.toString()) }
    }

    companion object {
        fun decode(buf: RegistryFriendlyByteBuf): SetClientPlayerDataPacket {
            val badges = buf.readCollection(
                { mutableSetOf<ResourceLocation>() },
                { buf.readString().let(ResourceLocation::tryParse) ?: ResourceLocation("minecraft", "empty") }
            )
            return SetClientPlayerDataPacket(
                PlayerInstancedDataStoreTypes.BADGES,
                ClientBadgeManager(badges)
            )
        }

        fun runAction(data: ClientInstancedPlayerData) {
            if (data !is ClientBadgeManager) return
            CobblemonClient.clientBadgeData = data
        }

        fun runIncremental(data: ClientInstancedPlayerData) {
            if (data !is ClientBadgeManager) return
            CobblemonClient.clientBadgeData.obtainedBadges.addAll(data.obtainedBadges)
        }
    }

}