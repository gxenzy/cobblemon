package com.cobblemon.mod.common.client.gui.trainercard

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.badges.Badges
import com.cobblemon.mod.common.api.pokedex.CaughtCount
import com.cobblemon.mod.common.api.stats.CobblemonStats.getStat
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.CobblemonRenderable
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.stats.Stats
import kotlin.math.ceil

class TrainerCardMenu : Screen(
    Component.translatable("cobblemon.ui.trainercard.title")
), CobblemonRenderable {

    private var elapsedTicks = 0
    private var lastSyncedTimePlayed = 0
    private var panelLeft = 0
    private var panelTop = 0
    private lateinit var badgeScrollList: BadgesScrollingWidget

    override fun isPauseScreen(): Boolean = false

    override fun init() {
        super.init()
        elapsedTicks = 0
        lastSyncedTimePlayed = getTimePlayed()
        // stats need to update when we open the menu, otherwise time played only updates when stats menu is opened
        Minecraft.getInstance().connection?.send(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS)
        )

        val badges = Badges.all().sortedBy { it.order }
        val panelWidth = minOf(220, (width - 16).coerceAtLeast(1))
        panelLeft = (width - panelWidth) / 2
        val leftColumnWidth = panelWidth / 2
        val badgeAreaWidth = panelWidth - leftColumnWidth - 8
        val columns = ((badgeAreaWidth + BadgesScrollingWidget.SLOT_SPACING) /
            (BadgesScrollingWidget.SLOT_SIZE + BadgesScrollingWidget.SLOT_SPACING)).coerceIn(1, 4)
        val badgeWidth = columns * BadgesScrollingWidget.SLOT_SIZE +
            (columns - 1) * BadgesScrollingWidget.SLOT_SPACING + 4
        val rowCount = ceil(badges.size.toDouble() / columns).toInt().coerceAtLeast(1)
        val rowHeight = BadgesScrollingWidget.SLOT_SIZE + BadgesScrollingWidget.SLOT_SPACING
        val badgeHeight = minOf(rowCount, BadgesScrollingWidget.MAX_VISIBLE_ROWS) * rowHeight
        val panelHeight = maxOf(92, badgeHeight + 20)
        panelTop = ((height - panelHeight) / 2).coerceAtLeast(8)

        badgeScrollList = BadgesScrollingWidget(
            x = panelLeft + panelWidth - badgeWidth,
            y = panelTop + 10,
            width = badgeWidth,
            height = badgeHeight,
            columns = columns
        )
        badgeScrollList.createEntries(badges)
        addRenderableWidget(badgeScrollList)
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
        val portraitX = panelLeft
        val portraitY = minOf(panelTop + 48, (height - 32).coerceAtLeast(0))
        val portraitSize = 30
        val statsTextWidth = (badgeScrollList.x - panelLeft - 8).coerceAtLeast(1)

        val player = Minecraft.getInstance().player ?: return // unsure of why this would ever not return a player but if you're here maybe it's because it did
        val entity = Minecraft.getInstance().level?.getPlayerByUUID(player.uuid) ?: return

        context.enableScissor(portraitX, portraitY, portraitX + portraitSize, portraitY + portraitSize)
        InventoryScreen.renderEntityInInventoryFollowsMouse(context,
            portraitX, portraitY, portraitX + portraitSize, portraitY + portraitSize, 28, 0.5f, mouseX.toFloat(), mouseY.toFloat(),
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
            x = panelLeft,
            y = panelTop + 4,
            maxCharacterWidth = statsTextWidth
        )

        drawScaledText(
            context = context,
            text = lang("ui.trainermenu.pokemon_registered",
                CobblemonClient.clientPokedexData.getDexCalculatedValue(cobblemonResource("national"), CaughtCount)),
            x = panelLeft,
            y = panelTop + 14,
            maxCharacterWidth = statsTextWidth
        )

        val playerBattlesWon = player.stats.getValue(Stats.CUSTOM.get(getStat(Cobblemon.statistics.PVP_BATTLES_WON)))
        val playerBattlesLost = player.stats.getValue(Stats.CUSTOM.get(getStat(Cobblemon.statistics.PVP_BATTLES_LOST)))

        if (playerBattlesWon > 0 || playerBattlesLost > 0) {
            drawScaledText(
                context = context,
                text = lang("ui.trainermenu.battle_stats",
                    playerBattlesWon,
                    playerBattlesLost
                ),
                x = panelLeft,
                y = panelTop + 24,
                maxCharacterWidth = statsTextWidth
            )
        }

        badgeScrollList.render(context, mouseX, mouseY, partialTick)
    }

    private fun getTimePlayed(): Int =
        Minecraft.getInstance().player?.stats?.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) ?: 0

}