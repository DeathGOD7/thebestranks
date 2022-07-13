package com.zorbeytorunoglu.thebestranks.configuration.messages

import com.zorbeytorunoglu.thebestranks.utils.StringUtils

class MessageHandler(private val container: MessageContainer) {

    fun getNoPerm(): String {
        return StringUtils.hex(container.noPerm)
    }

    fun getYourRank(): String {
        return container.yourRank
    }

    fun getOnlyInGame(): String {
        return container.onlyInGame
    }

    fun getRankUp(): String {
        return container.rankUpMessage
    }

    fun getNoGreaterRank(): String {
        return container.noGreaterRank
    }

    fun getRankSetUsage(): String {
        return container.rankSetUsage
    }

    fun getPlayerNotFound(): String {
        return container.playerNotFound
    }

    fun getRankNotFound(): String {
        return container.rankDoesntExists
    }

    fun getRankSet(): String {
        return container.rankSet
    }

    fun getNoRankPrefix(): String {
        return container.noRankPrefix
    }

    fun getNoGreaterRankPrefix(): String {
        return container.noGreaterRankPrefix
    }

}