package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.client.gui.pokeradar.PokeRadarGUI
import com.cobblemon.mod.common.client.pokeradar.PokeRadarType
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class PokeRadarItem(val type: PokeRadarType) : CobblemonItem(Properties().stacksTo(1)) {

	override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack?>? {
		if (level.isClientSide && player is LocalPlayer) {
			PokeRadarGUI.open(type)
		}
		return super.use(level, player, usedHand)
	}
}