package com.cobblemon.mod.common.net.messages.client.pokeradar

import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.pokeradar.SpawnCheckEntry
import com.cobblemon.mod.common.pokeradar.SpawnCheckEntryData
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.network.RegistryFriendlyByteBuf

class PokeRadarScanResultPacket(
	val entries: List<SpawnCheckEntryData>
) : NetworkPacket<PokeRadarScanResultPacket> {
	override val id = ID

	override fun encode(buffer: RegistryFriendlyByteBuf) {
		buffer.writeVarInt(entries.size)
		entries.forEach { it.encode(buffer) }
	}

	companion object {
		val ID = cobblemonResource("poke_radar_scan_result")
		fun decode(buffer: RegistryFriendlyByteBuf): PokeRadarScanResultPacket {
			val size = buffer.readVarInt()
			val entries = ArrayList<SpawnCheckEntryData>(size)
			repeat(size) { entries.add(SpawnCheckEntryData.decode(buffer)) }
			return PokeRadarScanResultPacket(entries)
		}
	}
}