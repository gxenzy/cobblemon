package com.cobblemon.mod.common.api.badges

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.net.messages.client.data.BadgeRegistrySyncPacket
import com.cobblemon.mod.common.util.adapters.IdentifierAdapter
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType

object Badges : JsonDataRegistry<Badge> {

    override val id: ResourceLocation = cobblemonResource("badges")
    override val type: PackType = PackType.SERVER_DATA
    override val observable = SimpleObservable<Badges>()

    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .create()

    override val typeToken: TypeToken<Badge> = TypeToken.get(Badge::class.java)
    override val resourcePath: String = "badges"

    private val badges = hashMapOf<ResourceLocation, Badge>()

    override fun reload(data: Map<ResourceLocation, Badge>) {
        this.badges.clear()
        data.forEach { (id, badge) ->
            try {
                badge.identifier = id
                this.badges[id] = badge
            } catch (e: Exception) {
                Cobblemon.LOGGER.error("Skipped loading the {} badge", id, e)
            }
        }
        Cobblemon.LOGGER.info("Loaded {} badges", this.badges.size)
        this.observable.emit(this)
    }

    override fun sync(player: ServerPlayer) {
        BadgeRegistrySyncPacket(this.all()).sendToPlayer(player)
    }

    /**
     * Get all loaded [Badge]s.
     */
    @JvmStatic
    fun all() = this.badges.values.toList()

    /**
     * Gets a [Badge] by its [ResourceLocation].
     * @param identifier The identifier of the badge.
     * @return The [Badge], if loaded, otherwise null.
     */
    @JvmStatic
    fun getByIdentifier(identifier: ResourceLocation): Badge? = this.badges[identifier]
}