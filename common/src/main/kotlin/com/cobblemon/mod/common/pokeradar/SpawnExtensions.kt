package com.cobblemon.mod.common.pokeradar

import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object SpawnExtensions {

	private const val BLOCKS_PER_CHUNK = 16
	
	fun scanPokemonInRadius(
		player: ServerPlayer,
		radius: Int,
		cube: Boolean = false,
		maxDistance: Double = (radius * BLOCKS_PER_CHUNK).toDouble()
	): List<SpawnCheckEntry> {
		val world: ServerLevel = player.serverLevel()
		val radiusInBlocks = (radius * BLOCKS_PER_CHUNK).toDouble()
		val verticalRadius = if (cube) radiusInBlocks else config.spawningZoneHeight / 2.0
		val box = player.boundingBox.inflate(radiusInBlocks, verticalRadius, radiusInBlocks)
		val origin = player.position()

		return world.getEntitiesOfClass(PokemonEntity::class.java, box) { entity ->
			entity.isAlive &&
					entity.pokemon.isWild() &&
					(maxDistance <= 0 || entity.position().distanceTo(origin) <= maxDistance)
		}.map { entity ->
			SpawnCheckEntry(
				pokemon = PokemonProperties.parse(entity.pokemon.species.resourceIdentifier.toString()),
				entityId = entity.id
			)
		}
	}
}