package com.cobblemon.mod.common.net.messages.server.pokeradar

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class PokeRadarScanPacket : NetworkPacket<PokeRadarScanPacket> {
	override val id = ID
	override fun encode(buffer: RegistryFriendlyByteBuf) {
	}

	companion object {
		val ID = cobblemonResource("poke_radar_scan")
		fun decode(buffer: RegistryFriendlyByteBuf) = PokeRadarScanPacket()
	}
}