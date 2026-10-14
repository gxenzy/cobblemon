package com.cobblemon.mod.common.net.messages.server.pokeradar

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf

class PokeRadarTeleportPacket(val pos: BlockPos? = BlockPos.ZERO) : NetworkPacket<PokeRadarTeleportPacket> {
	override val id = ID
	override fun encode(buffer: RegistryFriendlyByteBuf) {
		buffer.writeBlockPos(pos ?: BlockPos.ZERO)
	}

	companion object {
		val ID = cobblemonResource("poke_radar_teleport")
		fun decode(buffer: RegistryFriendlyByteBuf) = PokeRadarTeleportPacket(buffer.readBlockPos())
	}
}