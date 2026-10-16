package com.cobblemon.mod.common.api.badges

import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable

class Badge(
    identifier: ResourceLocation,
    val name: String,
    val texture: ResourceLocation,
    val order: Int
): StringRepresentable {

    @Transient
    var identifier: ResourceLocation = identifier
        internal set

    override fun getSerializedName(): String {
        return identifier.toString()
    }
}