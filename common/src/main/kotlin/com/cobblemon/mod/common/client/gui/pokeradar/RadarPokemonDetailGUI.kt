package com.cobblemon.mod.common.client.gui.pokeradar

import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import com.cobblemon.mod.common.pokeradar.SpawnCheckEntryData
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

class RadarPokemonDetailGUI(
	override val type: PokeRadarType,
	private val entry: SpawnCheckEntryData,
	private val returnPage: Int
) : PokeRadarGUI(type) {

	override fun init() {
		addRenderableWidget(
			Button.builder(Component.literal("< Back")) {
				Minecraft.getInstance().setScreen(RadarGUI(type, returnPage))
			}.bounds(10, 10, 60, 20).build()
		)
		super.init()
	}

	override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
		super.render(g, mouseX, mouseY, partialTick)

		val name = entry.pokemon?.species ?: entry.detail.displayName ?: entry.detail.id
		g.drawCenteredString(font, name, width / 2, 15, 0xFFFFFF)

		val lines = buildList {
			add("ID: ${entry.detail.id}")
			add("Type: ${entry.detail.type}")
			add("Bucket: ${entry.detail.bucket}")
			add("Level Range: ${entry.levelRange?.let { "${it.first}-${it.last}" } ?: "?"}")
			add("")
			add("Block Pos: ${entry.spawnable.position}")
			add(
				"World Pos: ${"%.1f".format(entry.worldPosition.x)}, ${"%.1f".format(entry.worldPosition.y)}, ${
					"%.1f".format(
						entry.worldPosition.z
					)
				}"
			)
			add("Distance: ${"%.2f".format(entry.distanceFromOrigin)}m")
			add("")
			add("Block Light: ${entry.spawnable.light}")
			add("Sky Light: ${entry.spawnable.skyLight}")
			add("Can See Sky: ${entry.spawnable.canSeeSky}")
			add("")
			add("Weight: ${"%.3f".format(entry.weight)}")
			add("Weight Share @ Pos: ${"%.2f".format(entry.weightShareAtPosition)}%")
			add("Global Share: ${"%.2f".format(entry.globalShare)}%")
			if (entry.detail.validBiomes.isNotEmpty()) {
				add("")
				add("Valid Biomes (${entry.detail.validBiomes.size}):")
				entry.detail.validBiomes.take(6).forEach { add("  - $it") }
				if (entry.detail.validBiomes.size > 6) {
					add("  ... +${entry.detail.validBiomes.size - 6} more")
				}
			}
		}

		var y = 40
		for (line in lines) {
			g.drawString(font, line, 20, y, 0xFFFFFF)
			y += 11
		}
	}
}