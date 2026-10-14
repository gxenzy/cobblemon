package com.cobblemon.mod.common.pokeradar

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

// TODO: Temporary until the Data that the client needs is finalized

data class SpawnablePositionData(
	val position: BlockPos,
	val light: Int,
	val skyLight: Int,
	val canSeeSky: Boolean
) {
	fun encode(buffer: RegistryFriendlyByteBuf) {
		buffer.writeBlockPos(position)
		buffer.writeInt(light)
		buffer.writeInt(skyLight)
		buffer.writeBoolean(canSeeSky)
	}

	companion object {
		fun decode(buffer: RegistryFriendlyByteBuf) = SpawnablePositionData(
			position = buffer.readBlockPos(),
			light = buffer.readInt(),
			skyLight = buffer.readInt(),
			canSeeSky = buffer.readBoolean()
		)

		fun of(spawnable: SpawnablePosition) = SpawnablePositionData(
			position = spawnable.position,
			light = spawnable.light,
			skyLight = spawnable.skyLight,
			canSeeSky = spawnable.canSeeSky
		)
	}
}

data class SpawnDetailData(
	val type: String,
	val id: String,
	val displayName: String?,
	val bucket: String,
	val validBiomes: Set<ResourceLocation>
) {
	fun encode(buffer: RegistryFriendlyByteBuf) {
		buffer.writeUtf(type)
		buffer.writeUtf(id)
		buffer.writeNullable(displayName) { buf, s -> buf.writeUtf(s) }
		buffer.writeUtf(bucket)
		buffer.writeVarInt(validBiomes.size)
		validBiomes.forEach { buffer.writeResourceLocation(it) }
	}

	companion object {
		fun decode(buffer: RegistryFriendlyByteBuf): SpawnDetailData {
			val type = buffer.readUtf()
			val id = buffer.readUtf()
			val displayName = buffer.readNullable { it.readUtf() }
			val bucket = buffer.readUtf()
			val size = buffer.readVarInt()
			val validBiomes = mutableSetOf<ResourceLocation>()
			repeat(size) { validBiomes.add(buffer.readResourceLocation()) }
			return SpawnDetailData(type, id, displayName, bucket, validBiomes)
		}

		fun of(detail: SpawnDetail) = SpawnDetailData(
			type = detail.type,
			id = detail.id,
			displayName = detail.displayName,
			bucket = detail.bucket,
			validBiomes = detail.validBiomes
		)
	}
}

data class SpawnCheckEntryData(
	val spawnable: SpawnablePositionData,
	val detail: SpawnDetailData,
	val pokemon: PokemonProperties?,
	val levelRange: IntRange?,
	val worldPosition: Vec3,
	val bucket: Map<String, Float>,
	val weight: Float,
	val weightShareAtPosition: Float,
	val globalShare: Float,
	val distanceFromOrigin: Double
) : Encodable {
	override fun encode(buffer: RegistryFriendlyByteBuf) {
		spawnable.encode(buffer)
		detail.encode(buffer)
		buffer.writeNullable(pokemon) { buf, p -> buf.writeUtf(p.asString()) }
		buffer.writeNullable(levelRange) { buf, r ->
			buf.writeInt(r.first)
			buf.writeInt(r.last)
		}
		buffer.writeDouble(worldPosition.x)
		buffer.writeDouble(worldPosition.y)
		buffer.writeDouble(worldPosition.z)
		buffer.writeVarInt(bucket.size)
		bucket.forEach { (k, v) ->
			buffer.writeUtf(k)
			buffer.writeFloat(v)
		}
		buffer.writeFloat(weight)
		buffer.writeFloat(weightShareAtPosition)
		buffer.writeFloat(globalShare)
		buffer.writeDouble(distanceFromOrigin)
	}

	companion object {
		fun decode(buffer: RegistryFriendlyByteBuf): SpawnCheckEntryData {
			val spawnable = SpawnablePositionData.decode(buffer)
			val detail = SpawnDetailData.decode(buffer)
			val pokemon = buffer.readNullable { PokemonProperties.parse(it.readUtf()) }
			val levelRange = buffer.readNullable { IntRange(it.readInt(), it.readInt()) }
			val worldPosition = Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
			val bucketSize = buffer.readVarInt()
			val bucket = LinkedHashMap<String, Float>(bucketSize)
			repeat(bucketSize) { bucket[buffer.readUtf()] = buffer.readFloat() }
			val weight = buffer.readFloat()
			val weightShareAtPosition = buffer.readFloat()
			val globalShare = buffer.readFloat()
			val distanceFromOrigin = buffer.readDouble()
			return SpawnCheckEntryData(
				spawnable = spawnable,
				detail = detail,
				pokemon = pokemon,
				levelRange = levelRange,
				worldPosition = worldPosition,
				bucket = bucket,
				weight = weight,
				weightShareAtPosition = weightShareAtPosition,
				globalShare = globalShare,
				distanceFromOrigin = distanceFromOrigin
			)
		}
	}
}

data class SpawnCheckEntry(
	val position: SpawnablePosition,
	val detail: SpawnDetail,
	val pokemon: PokemonProperties?,
	val levelRange: IntRange?,
	val worldPosition: Vec3,
	val bucket: MutableMap<String, Float>,
	val weight: Float,
	val weightShareAtPosition: Float,
	val globalShare: Float,
	val distanceFromOrigin: Double,
) {
	companion object {
		fun toData(entries: List<SpawnCheckEntry>): List<SpawnCheckEntryData> =
			entries.map { it.toData() }
	}

	fun toData() = SpawnCheckEntryData(
		spawnable = SpawnablePositionData.of(position),
		detail = SpawnDetailData.of(detail),
		pokemon = pokemon,
		levelRange = levelRange,
		worldPosition = worldPosition,
		bucket = bucket,
		weight = weight,
		weightShareAtPosition = weightShareAtPosition,
		globalShare = globalShare,
		distanceFromOrigin = distanceFromOrigin
	)
}