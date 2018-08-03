package org.codetome.zircon.internal.component.impl

import org.codetome.zircon.api.behavior.DrawSurface
import org.codetome.zircon.api.builder.component.ComponentStyleSetBuilder
import org.codetome.zircon.api.builder.graphics.LayerBuilder
import org.codetome.zircon.api.builder.graphics.StyleSetBuilder
import org.codetome.zircon.api.component.ColorTheme
import org.codetome.zircon.api.component.Component
import org.codetome.zircon.api.component.ComponentStyleSet
import org.codetome.zircon.api.component.Container
import org.codetome.zircon.api.data.Position
import org.codetome.zircon.api.data.Size
import org.codetome.zircon.api.data.Tile
import org.codetome.zircon.api.graphics.Layer
import org.codetome.zircon.api.input.Input
import org.codetome.zircon.api.resource.TilesetResource
import org.codetome.zircon.api.util.Maybe
import org.codetome.zircon.internal.component.InternalComponent
import org.codetome.zircon.internal.component.WrappingStrategy
import org.codetome.zircon.internal.event.Event
import org.codetome.zircon.internal.event.EventBus
import org.codetome.zircon.internal.tileset.impl.FontSettings

@Suppress("UNCHECKED_CAST")
open class DefaultContainer(initialSize: Size,
                            position: Position,
                            initialTileset: TilesetResource<out Tile>,
                            componentStyleSet: ComponentStyleSet,
                            wrappers: Iterable<WrappingStrategy> = listOf())
    : DefaultComponent(initialSize = initialSize,
        position = position,
        componentStyleSet = componentStyleSet,
        wrappers = wrappers,
        initialTileset = initialTileset),
        Container {

    private val components = mutableListOf<InternalComponent>()

    override fun addComponent(component: Component) {
        require(component !== this) {
            "You can't add a component to itself!"
        }
        (component as? DefaultComponent)?.let { dc ->
            if (isAttached()) {
                dc.setPosition(dc.getPosition() + getEffectivePosition())
            }
            if (component.tileset() === FontSettings.NO_FONT) {
                component.useTileset(tileset())
            } else {
                require(tileset().size() == component.tileset().size()) {
                    "Trying to add component with incompatible tileset size '${component.tileset().size()}' to" +
                            "container with tileset size: '${tileset().size()}'!"
                }
            }
            require(components.none { it.intersects(component) }) {
                "You can't add a component to a container which intersects with other components!"
            }
            components.add(dc)
            EventBus.broadcast(Event.ComponentAddition)
        } ?: throw IllegalArgumentException("Using a base class other than DefaultComponent is not supported!")
    }

    override fun setPosition(position: Position) {
        super.setPosition(position)
        components.forEach {
            it.setPosition(it.getPosition() + getEffectivePosition())
            // TODO: if the component has the same size and position it adds it!!!
            require(containsBoundable(it)) {
                "You can't add a component to a container which is not within its bounds " +
                        "(target size: ${getEffectiveSize()}, component size: ${it.getBoundableSize()}" +
                        ", position: ${it.getPosition()})!"
            }
        }
    }

    override fun signalAttached() {
        super.signalAttached()
        components.forEach {
            it.signalAttached()
        }
    }

    override fun acceptsFocus() = false

    override fun giveFocus(input: Maybe<Input>) = false

    override fun takeFocus(input: Maybe<Input>) {}

    override fun removeComponent(component: Component): Boolean {
        var removalHappened = components.remove(component)
        if (removalHappened.not()) {
            val childResults = components
                    .filter { it is Container }
                    .map { (it as Container).removeComponent(component) }
            removalHappened = if (childResults.isEmpty()) {
                false
            } else {
                childResults.reduce(Boolean::or)
            }
        }
        if (removalHappened) {
            EventBus.broadcast(Event.ComponentRemoval)
        }
        return removalHappened
    }

    override fun transformToLayers(): List<Layer> {
        // TODO: persistent list here
        return mutableListOf(LayerBuilder.newBuilder()
                .textImage(getDrawSurface())
                .offset(getPosition())
                .build()).also {
            it.addAll(components.flatMap { (it as DefaultComponent).transformToLayers() })
        }
    }

    override fun drawOnto(surface: DrawSurface, offset: Position) {
        surface.draw(getDrawSurface(), getPosition())
        components.forEach {
            it.drawOnto(surface)
        }
    }

    override fun fetchComponentByPosition(position: Position) =
            if (this.containsPosition(position).not()) {
                Maybe.empty()
            } else {
                components.map {
                    it.fetchComponentByPosition(position)
                }.filter {
                    it.isPresent
                }.let { hits ->
                    if (hits.isEmpty()) {
                        Maybe.of(this)
                    } else {
                        hits.first()
                    }
                }
            }


    override fun toString(): String {
        return "${this::class.simpleName}(id=${id.toString().substring(0, 4)})"
    }

    override fun applyColorTheme(colorTheme: ColorTheme) {
        setComponentStyles(ComponentStyleSetBuilder.newBuilder()
                .defaultStyle(StyleSetBuilder.newBuilder()
                        .foregroundColor(colorTheme.getDarkForegroundColor())
                        .backgroundColor(colorTheme.getDarkBackgroundColor())
                        .build())
                .build())
        components.forEach {
            it.applyColorTheme(colorTheme)
        }
    }

    fun getComponents() = components

    fun fetchFlattenedComponentTree(): List<InternalComponent> {
        val result = mutableListOf<InternalComponent>()
        components.forEach {
            result.add(it)
            if (it is DefaultContainer) {
                result.addAll(it.fetchFlattenedComponentTree())
            }
        }
        return result
    }
}
