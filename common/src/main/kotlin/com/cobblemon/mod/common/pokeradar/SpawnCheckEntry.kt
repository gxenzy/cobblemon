package com.cobblemon.mod.common.pokeradar

import com.cobblemon.mod.common.api.net.Encodable
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf

data class SpawnCheckEntryData(
	val pokemon: PokemonProperties?,
	val entityId: Int? = null
) : Encodable {

	override fun encode(buffer: RegistryFriendlyByteBuf) {
		buffer.writeNullable(pokemon) { buf, p -> buf.writeUtf(p.asString()) }
		buffer.writeNullable(entityId) { buf, id -> buf.writeVarInt(id) }
	}

	companion object {
		fun decode(buffer: RegistryFriendlyByteBuf): SpawnCheckEntryData {
			val pokemon = buffer.readNullable { PokemonProperties.parse(it.readUtf()) }
			val entityId = buffer.readNullable { it.readVarInt() }
			return SpawnCheckEntryData(pokemon, entityId)
		}
	}
}

data class SpawnCheckEntry(
	val pokemon: PokemonProperties?,
	val entityId: Int? = null
) {
	companion object {
		fun toData(entries: List<SpawnCheckEntry>): List<SpawnCheckEntryData> =
			entries.map { it.toData() }
	}

	fun toData() = SpawnCheckEntryData(
		pokemon = pokemon,
		entityId = entityId
	)
}