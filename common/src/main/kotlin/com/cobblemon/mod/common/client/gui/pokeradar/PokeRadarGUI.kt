package com.cobblemon.mod.common.client.gui.pokeradar

import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

open class PokeRadarGUI(
	open val type: PokeRadarType,
) : Screen(Component.translatable("cobblemon.ui.poke_radar.title")), CobblemonRenderable {

	companion object {

		// TODO: For now always opens Radar Page, later need persistant storage and open the last opened page
		fun open(type: PokeRadarType) {
			val mc = Minecraft.getInstance()
			val screen = RadarGUI(type)
			mc.setScreen(screen)
		}
	}
}