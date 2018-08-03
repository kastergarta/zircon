package org.codetome.zircon.api.data

import org.codetome.zircon.api.graphics.StyleSet
import org.codetome.zircon.api.behavior.DrawSurface
import org.codetome.zircon.api.behavior.Drawable
import org.codetome.zircon.api.color.TextColor
import org.codetome.zircon.api.modifier.Modifier
import kotlin.reflect.KClass

data class CharacterTile(
        val character: Char,
        private val style: StyleSet = StyleSet.defaultStyle())
    : Drawable, Tile {

    override fun tileType() = CharacterTile::class

    override fun generateCacheKey(): String {
        return "c:$character,s:{${style.generateCacheKey()}}"
    }

    override fun toStyleSet() = style

    override fun withForegroundColor(foregroundColor: TextColor): Tile {
        return Tile.create(character, style.withForegroundColor(foregroundColor))
    }

    override fun withBackgroundColor(backgroundColor: TextColor): Tile {
        return Tile.create(character, style.withBackgroundColor(backgroundColor))
    }

    override fun withStyle(styleSet: StyleSet): Tile {
        return Tile.create(character, styleSet)
    }

    override fun withModifiers(vararg modifiers: Modifier): Tile {
        return withModifiers(modifiers.toSet())
    }

    override fun withModifiers(modifiers: Set<Modifier>): Tile {
        return Tile.create(character, style.withModifiers(modifiers))
    }

    override fun withoutModifiers(vararg modifiers: Modifier): Tile {
        return withoutModifiers(modifiers.toSet())
    }

    override fun withoutModifiers(modifiers: Set<Modifier>): Tile {
        return Tile.create(character, style.withRemovedModifiers(modifiers))
    }

    fun withCharacter(character: Char): Tile {
        return Tile.create(character, style)
    }
}
