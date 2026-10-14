package com.cobblemon.mod.common.client.gui.pokeradar

import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class PokeRadarGUI private constructor(
	val type: PokeRadarType,
) : Screen(Component.translatable("cobblemon.ui.poke_radar.title")), CobblemonRenderable {

	companion object {
		/**
		 * Attempts to open this screen for a client.
		 */
		fun open(type: PokeRadarType) {
			val mc = Minecraft.getInstance()
			val screen = PokeRadarGUI(type)
			mc.setScreen(screen)
		}
	}
}