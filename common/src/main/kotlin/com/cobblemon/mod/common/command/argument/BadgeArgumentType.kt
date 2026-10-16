/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.command.argument

import com.cobblemon.mod.common.api.badges.Badge
import com.cobblemon.mod.common.api.badges.Badges
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class BadgeArgumentType : ArgumentType<Badge> {

    override fun parse(reader: StringReader): Badge {
        val identifier = try {
            reader.asIdentifierDefaultingNamespace()
        } catch (exception: Exception) {
            throw SimpleCommandExceptionType(INVALID_BADGE).createWithContext(reader)
        }
        return Badges.getByIdentifier(identifier)
            ?: throw SimpleCommandExceptionType(INVALID_BADGE).createWithContext(reader)
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(Badges.all().map { it.identifier.toString() }, builder)
    }

    companion object {
        private val INVALID_BADGE = Component.translatable("cobblemon.command.badge.invalid")

        fun badge() = BadgeArgumentType()

        fun <S> getBadge(context: CommandContext<S>, name: String): Badge =
            context.getArgument(name, Badge::class.java)
    }
}
