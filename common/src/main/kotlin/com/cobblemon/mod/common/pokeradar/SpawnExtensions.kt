package com.cobblemon.mod.common.pokeradar

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.config
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.AreaSpawnablePosition
import com.cobblemon.mod.common.api.spawning.position.calculators.SpawnablePositionCalculator.Companion.prioritizedAreaCalculators
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.api.spawning.spawner.SpawningZoneInput
import com.cobblemon.mod.common.duck.ServerLevelDuck
import com.cobblemon.mod.common.util.spawner
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import kotlin.math.max

object SpawnExtensions {
	fun scanSpawnsInRadius(
		player: ServerPlayer,
		bucket: MutableMap<String, Float>,
		radius: Int,
		cube: Boolean = false,
		maxDistance: Double = radius.toDouble()
	): List<SpawnCheckEntry> {
		if (!config.enableSpawning) return emptyList()
		val levelKey = (player.serverLevel() as ServerLevelDuck).`cobblemon$getLevelResourceLocation`()
		if (levelKey in config.worldSpawningBlocklist) return emptyList()

		val spawner: Spawner = player.spawner
		val world: ServerLevel = player.serverLevel()
		val cause = SpawnCause(spawner, player)

		val diameter = radius * 2 + 1
		val height = if (cube) diameter else config.spawningZoneHeight

		val slice = Cobblemon.spawningZoneGenerator.generate(
			spawner = spawner,
			input = SpawningZoneInput(
				cause = cause,
				world = world,
				baseX = Mth.ceil(player.x - diameter / 2.0),
				baseY = Mth.ceil(player.y - height / 2.0),
				baseZ = Mth.ceil(player.z - diameter / 2.0),
				length = diameter,
				height = height,
				width = diameter
			)
		)

		val positions: List<AreaSpawnablePosition> = Cobblemon.areaSpawnablePositionResolver
			.resolve(spawner, prioritizedAreaCalculators, slice)

		val origin = player.position()
		val entries = ArrayList<SpawnCheckEntry>(positions.size)
		val globalWeightByDetail = HashMap<SpawnDetail, Float>()

		for (pos in positions) {
			val wp = Vec3(pos.position.x + 0.5, pos.position.y.toDouble(), pos.position.z + 0.5)
			if (maxDistance > 0 && wp.distanceTo(origin) > maxDistance) continue

			val matching = LinkedHashMap<SpawnDetail, Float>()
			for (bucketName in bucket.keys) {
				for (detail in spawner.getMatchingSpawns(bucketName, pos)) {
					if (!pos.preFilter(detail) || !pos.postFilter(detail)) continue
					val w = max(0F, pos.getWeight(detail))
					if (w <= 0F) continue
					matching.merge(detail, w, Float::plus)
				}
			}

			if (matching.isEmpty()) continue

			val positionTotal = matching.values.sum().takeIf { it > 0F } ?: 1F

			for ((detail, weight) in matching) {
				globalWeightByDetail.merge(detail, weight, Float::plus)

				entries.add(
					SpawnCheckEntry(
						position = pos,
						detail = detail,
						pokemon = (detail as? PokemonSpawnDetail)?.pokemon,
						levelRange = (detail as? PokemonSpawnDetail)?.levelRange,
						worldPosition = wp,
						bucket = bucket,
						weight = weight,
						weightShareAtPosition = weight / positionTotal * 100F,
						globalShare = 0F,
						distanceFromOrigin = origin.distanceTo(wp),
					)
				)
			}
		}

		val globalTotal = globalWeightByDetail.values.sum().takeIf { it > 0F } ?: 1F
		return entries.map { entry ->
			val g = (globalWeightByDetail[entry.detail] ?: 0F) / globalTotal * 100F
			entry.copy(globalShare = g)
		}
	}
}

// TODO: Debug
fun ServerPlayer.sendSpawnScan(entries: List<SpawnCheckEntry>, bucket: MutableMap<String, Float>) {
	if (entries.isEmpty()) {
		sendSystemMessage(Component.literal("No spawns for $bucket."))
		return
	}

	val grouped = entries
		.groupBy { it.pokemon?.species ?: it.detail.displayName ?: "?" }

	sendSystemMessage(
		Component.literal(
			"${entries.size} entries across ${grouped.size} species in bucket $bucket:"
		)
	)

	grouped.entries
		.sortedByDescending { (_, list) -> list.maxOf { it.globalShare } }
		.forEach { (name, list) ->
			val nearest = list.minBy { it.distanceFromOrigin }
			sendSystemMessage(
				Component.literal(
					"  $name — ${"%.2f".format(nearest.globalShare)}%  " +
							"(${list.size} pos, nearest @ ${nearest.position.position} " +
							"${"%.1f".format(nearest.distanceFromOrigin)}m, " +
							"light=${nearest.position.light}/sky=${nearest.position.skyLight}, " +
							"biome=${nearest.position.biomeName})"
				)
			)
		}
}