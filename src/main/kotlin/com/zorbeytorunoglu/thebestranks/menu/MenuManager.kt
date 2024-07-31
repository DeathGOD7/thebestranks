package com.zorbeytorunoglu.thebestranks.menu

import com.zorbeytorunoglu.kLib.configuration.createYamlResource
import com.zorbeytorunoglu.kLib.extensions.colorHex
import com.zorbeytorunoglu.kLib.extensions.numbers
import com.zorbeytorunoglu.thebestranks.TBR
import com.zorbeytorunoglu.thebestranks.rank.Rank
import me.clip.placeholderapi.PlaceholderAPI
import org.apache.commons.lang.StringUtils
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MenuManager(private val plugin: TBR) {

    private val file = plugin.createYamlResource("menu.yml").load()

    val config = MenuConfig(plugin, file)
    val pages = HashMap<String, Page>()

    private val customDesign = CustomDesign(plugin, file)

    fun createCustomInventory(player: Player): Page {

        val playerRank = plugin.rankManager.getRank(player)

        val inventories = mutableListOf<Inventory>()

        var page = 1

        var inventory = plugin.server.createInventory(null, customDesign.invSize,
            config.name.replace("%page%", page.toString()))

        var slot = 0

        var rankIndex = 0

        var keepCreating = true

        while (keepCreating) {

            for (cd in customDesign.slots) {

                if (cd == null) {

                    inventory.setItem(slot, null)

                } else {

                    when (cd.functionType) {

                        FunctionType.ITEM -> inventory.setItem(slot, cd.item)

                        FunctionType.RANK -> {
                            plugin.rankManager.getRank(rankIndex)?.let {
                                inventory.setItem(slot, getRankItem(player, playerRank, it))
                                rankIndex++
                            } ?: run { inventory.setItem(slot, null) }
                        }

                        FunctionType.NEXT_PAGE -> inventory.setItem(slot, config.nextPageItem)

                        FunctionType.PREVIOUS_PAGE -> inventory.setItem(slot, config.previousPageItem)

                    }

                }

                slot++

                if (slot >= customDesign.invSize) {
                    inventories.add(inventory)
                    page++
                    inventory = plugin.server.createInventory(null, customDesign.invSize, config.name.replace("%page%", page.toString()))
                    slot = 0

                    if (rankIndex >= plugin.rankManager.ranks.size-1) {
                        keepCreating = false
                    }

                }

            }

        }

        val pageObj = Page(player.uniqueId.toString(), 0, inventories.toList())

        pages[player.uniqueId.toString()] = pageObj

        return pageObj

    }

    fun createInventory(player: Player): Page {

        val items = getMenuRankItems(player)

        val inventories: MutableList<Inventory> = mutableListOf()

        var currentPage = 1

        var inv = plugin.server.createInventory(null, config.size,
            config.name.replace("%page%", currentPage.toString()))

        var index = 0

        for ((innerIndex, item) in items.withIndex()) {

            inv.setItem(index, item)

            if (items.lastIndex == innerIndex) {

                inv.setItem((config.size-1)-3, config.nextPageItem)
                inv.setItem((config.size-1)-5, config.previousPageItem)

                inventories.add(inv)
                break
            }

            if (index == config.size-10) {
                inv.setItem((config.size-1)-3, config.nextPageItem)
                inv.setItem((config.size-1)-5, config.previousPageItem)

                inventories.add(inv)
                currentPage++
                inv = plugin.server.createInventory(null, config.size, config.name.replace("%page%", currentPage.toString()))
                index = 0
                continue
            }

            index++

        }

        val page = Page(player.uniqueId.toString(), 0, inventories.toList())

        pages[player.uniqueId.toString()] = page

        return page

    }

    private fun getMenuRankItems(player: Player): List<ItemStack> {

        if (plugin.rankManager.ranks.isEmpty()) return emptyList()

        val playerRank = plugin.rankManager.getRank(player)

        val itemList: MutableList<ItemStack> = mutableListOf()

        plugin.rankManager.ranks.forEach { rank ->

            itemList.add(getRankItem(player, playerRank, rank))

        }

        return itemList

    }

    private fun getRankItem(player: Player, playerRank: Rank?, rank: Rank): ItemStack {

        if (playerRank == null) {

            return if (rank == plugin.rankManager.getFirstRank()!!) {
                getItemStackWPlaceholders(player, config.inProgressItem, rank, true)
            } else {
                getItemStackWPlaceholders(player, config.lockedItem, rank, false)
            }

        }

        return if (plugin.rankManager.isCurrentRank(playerRank, rank)) {

            getItemStackWPlaceholders(player, config.currentItem, rank, false)

        } else if (plugin.rankManager.rankPassed(playerRank, rank)) {

            getItemStackWPlaceholders(player, config.passedItem, rank, false)

        } else if (plugin.rankManager.isInProgress(playerRank,rank)) {

            getItemStackWPlaceholders(player,config.inProgressItem, rank, true)

        } else {

            getItemStackWPlaceholders(player, config.lockedItem, rank, false)

        }

    }

    private fun getItemStackWPlaceholders(player: Player, itemStack: ItemStack, rank: Rank, isInProgressItem: Boolean): ItemStack {
		val item = itemStack.clone()
		val meta = item.itemMeta

		meta.displayName = PlaceholderAPI.setPlaceholders(player,
			meta.displayName.replace("%rank%", "§f" + rank.displayName).colorHex
		)

		if (isInProgressItem) {

			val lore: List<String>

			if (rank.requirements.isEmpty()) {
				lore = plugin.messages.noRequirementsLore
			} else {
				lore = rank.lore.map {

					if (it.contains("%requirement_")) {
						val no = StringUtils.substringBetween(it, "%", "%").numbers
						val req = rank.requirements[no]

						it.replace("%requirement_$no%",
							req.guiMessage.replace("%your%",
								PlaceholderAPI.setPlaceholders(player, req.placeholder)))
							.replace("%status%", if (plugin.rankManager.requirementMet(player, req))
								config.statusDone else config.statusNotDone).colorHex
					} else {
						it.replace("%rank%", rank.displayName).colorHex
					}

				}
			}

			meta.lore = lore

		} else {
			if (meta.lore != null)
				meta.lore = meta.lore.map { it.replace("%rank%", rank.displayName).colorHex }
		}

		item.itemMeta = meta

		return item

    }

}