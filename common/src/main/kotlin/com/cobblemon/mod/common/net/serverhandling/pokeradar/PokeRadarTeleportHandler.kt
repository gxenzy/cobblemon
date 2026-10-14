package com.cobblemon.mod.common.net.serverhandling.pokeradar

import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.net.messages.server.pokeradar.PokeRadarTeleportPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

// TODO: Add Operator Check
object PokeRadarTeleportHandler : ServerNetworkPacketHandler<PokeRadarTeleportPacket> {
	override fun handle(packet: PokeRadarTeleportPacket, server: MinecraftServer, player: ServerPlayer) {
		player.teleportTo(
			packet.pos?.x?.plus(0.5) ?: 0.0,
			packet.pos?.y?.toDouble() ?: 0.0,
			packet.pos?.z?.plus(0.5) ?: 0.0
		)
	}
}