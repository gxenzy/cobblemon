package com.cobblemon.mod.common.client.gui.trainercard

import com.cobblemon.mod.common.api.badges.Badge
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.ScrollingWidget
import com.cobblemon.mod.common.client.gui.pokedex.renderTooltip
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.util.FastColor
import net.minecraft.util.Mth

class BadgesScrollingWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val columns: Int
) : ScrollingWidget<BadgesScrollingWidget.BadgeRow>(
    width = width,
    height = height,
    left = x,
    top = y - height,
    slotHeight = SLOT_SIZE + SLOT_SPACING
) {

    private var tooltip: MutableComponent? = null
    private var tooltipX = 0
    private var tooltipY = 0

    companion object {
        const val SLOT_SIZE = 16
        const val SLOT_SPACING = 3
        const val MAX_VISIBLE_ROWS = 2
    }

    fun createEntries(badges: List<Badge>) {

        val obtainedBadgeList = badges.filter { CobblemonClient.clientBadgeData.obtainedBadges.contains(it.identifier) }
            .sortedBy { it.order }
        val unobtainedBadgesList = badges.filter { !obtainedBadgeList.contains(it) }
            .sortedBy { it.order }
        val sortedBadgeList = obtainedBadgeList.plus(unobtainedBadgesList)

        sortedBadgeList.chunked(columns).forEach { listChunk ->
            addEntry(BadgeRow(this, listChunk))
        }
    }

    private val visibleRows: Int
        get() = (height / itemHeight).coerceAtLeast(1)

    private fun hasScrollbar(): Boolean = children().size > visibleRows

    override fun getRowLeft(): Int = if (hasScrollbar()) left + 1 else left
    override fun getScrollbarPosition(): Int = rowLeft + width - 3

    override fun renderWidget(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        tooltip = null
        super.renderWidget(context, mouseX, mouseY, delta)
        tooltip?.let { renderTooltip(context, it, tooltipX, tooltipY, delta) }
    }

    fun setTooltip(text: MutableComponent, mouseX: Int, mouseY: Int) {
        if (mouseX in left until left + width && mouseY in this.y until bottom) {
            tooltip = text
            tooltipX = mouseX + 20
            tooltipY = mouseY - 10
        }
    }

    override fun renderScrollbar(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (hasScrollbar()) {
            val xLeft = this.scrollbarPosition
            val xRight = xLeft + 3

            val barHeight = this.bottom - this.y

            var yBottom = ((barHeight * barHeight).toFloat() / this.maxPosition.toFloat()).toInt()
            yBottom = Mth.clamp(yBottom, minOf(32, barHeight), maxOf(minOf(32, barHeight), barHeight - 8))
            var yTop = scrollAmount.toInt() * (barHeight - yBottom) / this.maxScroll + this.y
            if (yTop < this.y) {
                yTop = this.y
            }

            context.fill(xLeft, this.y, xRight, this.bottom, FastColor.ARGB32.color(255, 75, 75, 75))
            context.fill(xLeft, yTop, xRight, yTop + yBottom, FastColor.ARGB32.color(255, 141, 141, 141))
        }
    }

    private fun renderEntry(context: GuiGraphics, mouseX: Int, mouseY: Int, index: Int, x: Int, y: Int) {
        getEntry(index).renderRow(context, y, x, mouseX, mouseY)
    }

    override fun renderListItems(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val rowX = rowLeft

        for (index in 0 until this.itemCount) {
            val rowY = this.getRowTop(index)
            val o = this.getRowBottom(index)
            if (o >= this.y && rowY <= this.bottom) {
                this.renderEntry(context, mouseX, mouseY, index, rowX, rowY)
            }
        }
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean =
        if (hasScrollbar()) super.mouseDragged(mouseX, mouseY, button, dragX, dragY) else true

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean =
        if (hasScrollbar()) super.mouseScrolled(mouseX, mouseY, scrollX, scrollY) else true

    class BadgeRow(
        val parent: BadgesScrollingWidget,
        val badgeList: List<Badge?>
    ) : Slot<BadgeRow>() {

        fun renderRow(context: GuiGraphics, y: Int, x: Int, mouseX: Int, mouseY: Int) {
            badgeList.forEachIndexed { index, badge ->
                val startPosX = x + ((SLOT_SPACING + SLOT_SIZE) * index)
                val startPosY = y + SLOT_SPACING

                if (badge != null) {
                    val obtained = CobblemonClient.clientBadgeData.obtainedBadges.contains(badge.identifier)
                    blitk(
                        matrixStack = context.pose(),
                        texture = badge.texture,
                        x = startPosX,
                        y = startPosY,
                        width = SLOT_SIZE,
                        height = SLOT_SIZE,
                        red = if (obtained) 1 else 0,
                        green = if (obtained) 1 else 0,
                        blue = if (obtained) 1 else 0
                    )
                    if (mouseX in startPosX until startPosX + SLOT_SIZE &&
                        mouseY in startPosY until startPosY + SLOT_SIZE
                    ) {
                        parent.setTooltip(
                            if (obtained) Component.translatable(badge.name) else lang("badge.unobtained"),
                            mouseX, mouseY
                        )
                    }
                }
            }
        }

        override fun getNarration(): Component? {
            return badgeList.firstOrNull()?.let { Component.translatable(it.name) }
        }

        override fun render(
            guiGraphics: GuiGraphics,
            index: Int,
            top: Int,
            left: Int,
            width: Int,
            height: Int,
            mouseX: Int,
            mouseY: Int,
            hovering: Boolean,
            partialTick: Float
        ) {}
    }
}