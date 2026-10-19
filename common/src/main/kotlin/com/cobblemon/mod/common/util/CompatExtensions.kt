/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

fun Player.displayClientMessage(message: Component, overlay: Boolean) {
    if (overlay) {
        sendOverlayMessage(message)
    } else {
        sendSystemMessage(message)
    }
}
