package com.cobblemon.mod.common.client.net.pokeradar

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.client.gui.pokeradar.RadarGUI
import com.cobblemon.mod.common.client.pokeradar.PokeRadarScanResults
import com.cobblemon.mod.common.net.messages.client.pokeradar.PokeRadarScanResultPacket
import net.minecraft.client.Minecraft

object PokeRadarScanResultHandler : ClientNetworkPacketHandler<PokeRadarScanResultPacket> {
	override fun handle(
		packet: PokeRadarScanResultPacket,
		client: Minecraft
	) {
		PokeRadarScanResults.entries = packet.entries
		(client.screen as? RadarGUI)?.onResultsReceived()
	}
}