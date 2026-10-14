package com.cobblemon.mod.common.client.gui.pokeradar

import com.cobblemon.mod.common.client.pokeradar.PokeRadarScanResults
import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import com.cobblemon.mod.common.net.messages.server.pokeradar.PokeRadarScanPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

class RadarGUI(
	override val type: PokeRadarType,
	private val initialPage: Int = 0
) : PokeRadarGUI(type) {

	private val rowsPerPage = 10
	private val rowHeight = 18
	private val listWidth = 300
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
	}

	private fun changePage(delta: Int) {
		val totalPages = maxOf(1, (PokeRadarScanResults.entries.size + rowsPerPage - 1) / rowsPerPage)
		page = Mth.clamp(page + delta, 0, totalPages - 1)
	}

	override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
		super.render(g, mouseX, mouseY, partialTick)

		val entries = PokeRadarScanResults.entries
		val listLeft = width / 2 - listWidth / 2
		val listTop = 30

		g.drawCenteredString(font, "PokeRadar Scan (${entries.size} results)", width / 2, 12, 0xFFFFFF)

		if (entries.isEmpty()) {
			g.drawCenteredString(font, "No results", width / 2, height / 2, 0xAAAAAA)
			return
		}

		val totalPages = maxOf(1, (entries.size + rowsPerPage - 1) / rowsPerPage)
		page = Mth.clamp(page, 0, totalPages - 1)

		val start = page * rowsPerPage
		val end = Mth.clamp(start + rowsPerPage, 0, entries.size)

		for (i in start until end) {
			val entry = entries[i]
			val rowY = listTop + (i - start) * rowHeight
			val hovered = mouseX in listLeft..(listLeft + listWidth) && mouseY in rowY..(rowY + rowHeight - 2)

			g.fill(
				listLeft, rowY, listLeft + listWidth, rowY + rowHeight - 2,
				if (hovered) 0x80FFFFFF.toInt() else 0x40000000
			)

			val name = entry.pokemon?.species ?: entry.detail.displayName ?: entry.detail.id
			val levelText = entry.levelRange?.let { "L${it.first}-${it.last}" } ?: "L?"
			val text = "$name  $levelText  ${"%.2f".format(entry.globalShare)}%  " +
					"${"%.1f".format(entry.distanceFromOrigin)}m  [${entry.detail.bucket}]"

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

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseClicked(mouseX, mouseY, button)) return true
		if (button != 0) return false

		val entries = PokeRadarScanResults.entries
		if (entries.isEmpty()) return false

		val listLeft = width / 2 - listWidth / 2
		val listTop = 30
		val start = page * rowsPerPage
		val end = Mth.clamp(start + rowsPerPage, 0, entries.size)

		for (i in start until end) {
			val rowY = listTop + (i - start) * rowHeight
			if (mouseX in listLeft.toDouble()..(listLeft + listWidth).toDouble() &&
				mouseY in rowY.toDouble()..(rowY + rowHeight - 2).toDouble()
			) {
				Minecraft.getInstance().setScreen(RadarPokemonDetailGUI(type, entries[i], page))
				return true
			}
		}
		return false
	}
}