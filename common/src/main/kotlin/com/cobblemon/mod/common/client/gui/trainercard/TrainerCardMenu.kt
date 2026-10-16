package com.cobblemon.mod.common.client.gui.trainercard

import com.cobblemon.mod.common.api.pokedex.CaughtCount
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.server
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.stats.Stats
import net.minecraft.world.entity.LivingEntity
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.atan

class TrainerCardMenu() : Screen(
    Component.translatable("cobblemon.ui.trainercard.title")
), CobblemonRenderable {

    private var elapsedTicks = 0
    private var lastSyncedTimePlayed = 0

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        elapsedTicks = 0
        lastSyncedTimePlayed = getTimePlayed()
        // stats need to update when we open the menu, otherwise time played only updates when stats menu is opened
        Minecraft.getInstance().connection?.send(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS)
        )
    }

    override fun tick() {
        super.tick()
        // make our own live playtime update, otherwise we have to send a packet a second to retrieve the playtime stat
        val timePlayed = getTimePlayed()
        if (timePlayed != lastSyncedTimePlayed) {
            lastSyncedTimePlayed = timePlayed
            elapsedTicks = 0
        } else { elapsedTicks++ }
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val matrices = context.pose()

        val portraitX = 60
        val portaitY = 60
        val portraitSize = 30

        context.enableScissor(portraitX, portaitY, portraitX + (portraitSize * 2), portaitY + portraitSize)

        val player = Minecraft.getInstance().player ?: return // unsure of why this would ever not return a player but if you're here maybe it's because it did
        val entity = Minecraft.getInstance().level?.getPlayerByUUID(player.uuid) ?: return

        val f = atan((-40 / 40.0f).toDouble()).toFloat()
        val g = atan((-10 / 40.0f).toDouble()).toFloat()
        val quaternionf = Quaternionf().rotateZ(Math.PI.toFloat())
        val quaternionf2 = Quaternionf().rotateX(g * 20.0f * (Math.PI.toFloat() / 180))

        entity.yBodyRot = 180.0F + f * 20.0F
        entity.yRot = (180.0F + f * 40.0F) * 1
        entity.xRot = 0F
        entity.yHeadRot = entity.yRot
        entity.yHeadRotO = entity.yRot

        InventoryScreen.renderEntityInInventory(context,
            portraitX.toFloat() + 15, portaitY.toFloat() + 55, 28f, Vector3f(), quaternionf, quaternionf2,
            entity
        )

        context.disableScissor()

        val totalSeconds = (getTimePlayed() + elapsedTicks) / 20
        val timePlayed = "%d:%02d:%02d:%02d".format(
            totalSeconds / 86_400, // days
            totalSeconds % 86_400 / 3_600, // hours
            totalSeconds % 3_600 / 60, // minutes
            totalSeconds % 60 // seconds
        )

        drawScaledText(
            context = context,
            text = lang("ui.trainermenu.timeplayed", timePlayed),
            x = 20,
            y = 20,
        )

        drawScaledText(
            context = context,
            text = lang("ui.trainermenu.pokemon_registered",
                CobblemonClient.clientPokedexData.getDexCalculatedValue(cobblemonResource("national"), CaughtCount)),
            x = 20,
            y = 25,
        )
    }

    private fun getTimePlayed(): Int =
        Minecraft.getInstance().player?.stats?.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) ?: 0

}