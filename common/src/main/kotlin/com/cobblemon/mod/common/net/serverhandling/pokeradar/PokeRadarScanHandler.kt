package com.cobblemon.mod.common.net.serverhandling.pokeradar

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.client.pokeradar.PokeRadarScanResultPacket
import com.cobblemon.mod.common.net.messages.server.pokeradar.PokeRadarScanPacket
import com.cobblemon.mod.common.pokeradar.SpawnCheckEntry
import com.cobblemon.mod.common.pokeradar.SpawnExtensions
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

object PokeRadarScanHandler : ServerNetworkPacketHandler<PokeRadarScanPacket> {
	override fun handle(packet: PokeRadarScanPacket, server: MinecraftServer, player: ServerPlayer) {
		val radius = 16

		val entries: List<SpawnCheckEntry> = SpawnExtensions.scanSpawnsInRadius(
			player = player,
			bucket = Cobblemon.bestSpawner.config.worldBuckets,
			radius = radius,
			cube = false
		)

		PokeRadarScanResultPacket(SpawnCheckEntry.toData(entries)).sendToPlayer(player)
	}
}