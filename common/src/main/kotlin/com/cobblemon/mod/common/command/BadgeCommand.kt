package com.cobblemon.mod.common.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.command.argument.BadgeArgumentType
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object BadgeCommand {

    private const val NAME = "badge"
    private const val GIVE = "give"
    private const val REMOVE = "remove"
    private const val PLAYER = "player"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val command =
            Commands.literal(NAME)
                .permission(CobblemonPermissions.BADGE)
                .then(Commands.literal(GIVE)
                    .then(Commands.argument(PLAYER, EntityArgument.player())
                        .then(Commands.argument(BADGE, BadgeArgumentType.badge())
                            .executes { executeGive(it, EntityArgument.getPlayer(it, PLAYER)) }
                        )
                    )
                )
                .then(Commands.literal(REMOVE)
                    .then(Commands.argument(PLAYER, EntityArgument.player())
                        .then(Commands.argument(BADGE, BadgeArgumentType.badge())
                            .executes { executeRemove(it, EntityArgument.getPlayer(it, PLAYER)) }
                        )
                    )
                )
        dispatcher.register(command)
    }

    private fun executeGive(context: CommandContext<CommandSourceStack>, player: ServerPlayer): Int {
        val badge = BadgeArgumentType.getBadge(context, BADGE)
        val changed = Cobblemon.playerDataManager.getBadgeData(player).give(badge)
        val message = if (changed) {
            "Gave ${badge.name} to ${player.name.string}."
        } else {
            "${player.name.string} already has ${badge.name}."
        }
        context.source.sendSuccess({ Component.literal(message) }, true)
        return Command.SINGLE_SUCCESS
    }

    private fun executeRemove(context: CommandContext<CommandSourceStack>, player: ServerPlayer): Int {
        val badge = BadgeArgumentType.getBadge(context, BADGE)
        val changed = Cobblemon.playerDataManager.getBadgeData(player).remove(badge)
        val message = if (changed) {
            "Removed ${badge} from ${player.name.string}." // TODO move this to lang
        } else {
            "${player.name.string} does not have ${badge.name}."
        }
        context.source.sendSuccess({ Component.literal(message) }, true)
        return Command.SINGLE_SUCCESS
    }

    private const val BADGE = "badge"
}