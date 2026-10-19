package com.cobblemon.mod.common.api.drop

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.Vec3

/**
 * A drop entry that points to a vanilla loot table.
 */
class LootTableDropEntry(
    override val percentage: Float,
    override val quantity: Int,
    override val maxSelectableTimes: Int,
    val id: ResourceLocation
) : DropEntry {
    override fun drop(
        entity: LivingEntity?,
        world: ServerLevel,
        pos: Vec3,
        player: ServerPlayer?
    ) {
        val key = ResourceKey.create(Registries.LOOT_TABLE, id)
        val playerEntity = player as? LivingEntity
        if (entity == null) return
        if (playerEntity != null) {
            val params = LootParams.Builder(world)
                .withParameter(LootContextParams.ATTACKING_ENTITY, playerEntity)
                .withParameter(LootContextParams.DAMAGE_SOURCE, playerEntity.damageSources().genericKill())
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .create(LootContextParamSets.ENTITY)
            val drops = world.server.reloadableRegistries().getLootTable(key)
                .getRandomItems(params)
            for (stack in drops) {
                if (stack.isEmpty) continue

                val itemEntity = ItemEntity(world, pos.x, pos.y, pos.z, stack)
                itemEntity.setDefaultPickUpDelay()
                world.addFreshEntity(itemEntity)
            }
        } else {
            val params = LootParams.Builder(world)
                .create(LootContextParamSets.EMPTY)
            val drops = world.server.reloadableRegistries().getLootTable(key)
                .getRandomItems(params)
            for (stack in drops) {
                if (stack.isEmpty) continue

                val itemEntity = ItemEntity(world, pos.x, pos.y, pos.z, stack)
                itemEntity.setDefaultPickUpDelay()
                world.addFreshEntity(itemEntity)
            }
        }

    }
}