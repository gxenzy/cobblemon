package com.cobblemon.mod.common.client.gui.pokeradar

import com.cobblemon.mod.common.client.pokeradar.PokeRadarScanResults
import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.net.messages.server.pokeradar.PokeRadarScanPacket
import com.cobblemon.mod.common.net.messages.server.pokeradar.PokeRadarTeleportPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

class RadarGUI(
	override val type: PokeRadarType,
	private val initialPage: Int = 0
) : PokeRadarGUI(type) {

	private val rowsPerPage = 10
	private val rowHeight = 18
	private val listWidth = 300
	private val tpButtonWidth = 28
	private var page = initialPage

	override fun init() {
		PokeRadarScanPacket().sendToServer()
		rebuild()
		super.init()
	}

	fun onResultsReceived() {
		page = 0
		rebuild()
	}

	private fun rebuild() {
		clearWidgets()

		addRenderableWidget(
			Button.builder(Component.literal("<")) { changePage(-1) }
				.bounds(width / 2 - 60, height - 28, 40, 20)
				.build()
		)
		addRenderableWidget(
			Button.builder(Component.literal(">")) { changePage(1) }
				.bounds(width / 2 + 20, height - 28, 40, 20)
				.build()
		)

		val entries = PokeRadarScanResults.entries
		if (entries.isEmpty()) return

		val totalPages = maxOf(1, (entries.size + rowsPerPage - 1) / rowsPerPage)
		page = Mth.clamp(page, 0, totalPages - 1)

		val listLeft = width / 2 - listWidth / 2
		val listTop = 30
		val start = page * rowsPerPage
		val end = Mth.clamp(start + rowsPerPage, 0, entries.size)

		for (i in start until end) {
			val entry = entries[i]
			val level = Minecraft.getInstance().level ?: return
			val entity = entry.entityId?.let { level.getEntity(it) } as? PokemonEntity

			val currentPos: BlockPos? = entity?.blockPosition()
			val rowY = listTop + (i - start) * rowHeight
			addRenderableWidget(
				Button.builder(Component.literal("TP")) {
					PokeRadarTeleportPacket(
						currentPos ?: Minecraft.getInstance().player?.blockPosition()
					).sendToServer()
					Minecraft.getInstance().setScreen(null)
				}
					.bounds(listLeft + listWidth - tpButtonWidth - 2, rowY, tpButtonWidth, rowHeight - 2)
					.build()
			)
		}
	}

	private fun changePage(delta: Int) {
		val totalPages = maxOf(1, (PokeRadarScanResults.entries.size + rowsPerPage - 1) / rowsPerPage)
		page = Mth.clamp(page + delta, 0, totalPages - 1)
		rebuild()
	}

	override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
		super.render(g, mouseX, mouseY, partialTick)

		val entries = PokeRadarScanResults.entries
		val listLeft = width / 2 - listWidth / 2
		val listTop = 30

		g.drawCenteredString(font, "PokeRadar Scan (${entries.size})", width / 2, 12, 0xFFFFFF)

		if (entries.isEmpty()) {
			g.drawCenteredString(font, "No results", width / 2, height / 2, 0xAAAAAA)
			return
		}

		val totalPages = maxOf(1, (entries.size + rowsPerPage - 1) / rowsPerPage)
		val start = page * rowsPerPage
		val end = Mth.clamp(start + rowsPerPage, 0, entries.size)
		val textRight = listLeft + listWidth - tpButtonWidth - 6

		for (i in start until end) {
			val entry = entries[i]
			val level = Minecraft.getInstance().level ?: return
			val entity = entry.entityId?.let { level.getEntity(it) } as? PokemonEntity

			val pos: BlockPos? = entity?.blockPosition()
			val rowY = listTop + (i - start) * rowHeight
			val hovered = mouseX in listLeft..textRight && mouseY in rowY..(rowY + rowHeight - 2)

			g.fill(
				listLeft, rowY, listLeft + listWidth, rowY + rowHeight - 2,
				if (hovered) 0x80FFFFFF.toInt() else 0x40000000
			)

			val name = entry.pokemon?.species ?: "?"
			val levelText = entry.pokemon?.level?.let { "$it" } ?: "?"
			val text = "$name  $levelText  (${pos?.x}, ${pos?.y}, ${pos?.z})"

			g.drawString(font, text, listLeft + 4, rowY + 4, 0xFFFFFF)
		}

		g.drawCenteredString(
			font,
			"Page ${page + 1} / $totalPages",
			width / 2,
			height - 40,
			0xFFFFFF
		)
	}
}