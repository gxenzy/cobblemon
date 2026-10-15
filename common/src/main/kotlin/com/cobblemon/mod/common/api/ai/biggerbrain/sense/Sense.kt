/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai.biggerbrain.sense

import com.cobblemon.mod.common.api.ai.biggerbrain.BiggerBrain
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.memory.MemoryModuleType

/**
 * A sense that an entity can use to gather information and store it in its brain's memory. This is an analog to a
 * Minecraft SensorType. Unlike SensorTypes, Senses are instantiated per entity and can handle cooldowns in a more
 * bespoke way. The remaining cooldown time is something that is set by the sense itself rather than it being forced,
 * so the cooldown can differ.
 *
 * @author Hiroku
 * @since December 29th, 2025
 */
interface Sense {
    /** The ticks until it will next be ticked. This is also displayed in the debug overlay. */
    var ticksUntilNextExecution: Int

    /** The set of memories that this sense may write to or read from. This ensures they are registered in the brain. */
    val memories: Set<MemoryModuleType<*>>

    /** Performs the sensing logic. Alter [ticksUntilNextExecution] to apply a cooldown if necessary. */
    fun tick(entity: LivingEntity, brain: BiggerBrain)

    /** Attempts to merge the [other] Sense into this one. Returns true if it was merged and therefore should not be added to the brain separately. */
    fun merge(other: Sense): Boolean
}
