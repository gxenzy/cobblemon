package com.cobblemon.mod.common.api.badges

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendPacket
import com.cobblemon.mod.common.api.storage.player.InstancedPlayerData
import com.cobblemon.mod.common.api.storage.player.PlayerInstancedDataStoreTypes
import com.cobblemon.mod.common.api.storage.player.client.ClientBadgeManager
import com.cobblemon.mod.common.net.messages.client.SetClientPlayerDataPacket
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import java.util.UUID

class BadgeManager(
    override val uuid: UUID,
    val obtainedBadges: MutableSet<ResourceLocation> = mutableSetOf()
) : InstancedPlayerData {

    val storeType = PlayerInstancedDataStoreTypes.BADGES

    override fun toClientData() = ClientBadgeManager(obtainedBadges.toMutableSet())

    fun give(badge: Badge): Boolean {
        if (!obtainedBadges.add(badge.identifier)) return false
        syncAndSave()
        return true
    }

    fun remove(badge: Badge): Boolean {
        if (!obtainedBadges.remove(badge.identifier)) return false
        syncAndSave()
        return true
    }

    private fun syncAndSave() {
        uuid.getPlayer()?.sendPacket(SetClientPlayerDataPacket(storeType, toClientData()))
        Cobblemon.playerDataManager.saveSingle(this, storeType)
    }

    companion object {
        val CODEC: Codec<BadgeManager> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.xmap(UUID::fromString, UUID::toString)
                    .fieldOf("uuid")
                    .forGetter { it.uuid },
                Codec.list(ResourceLocation.CODEC)
                    .xmap({ it.toMutableSet() }, { it.toList() })
                    .fieldOf("obtainedBadges")
                    .forGetter { it.obtainedBadges }
            ).apply(instance, ::BadgeManager)
        }
    }
}