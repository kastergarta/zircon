package org.hexworks.zircon.examples.playground

import org.hexworks.zircon.api.*
import org.hexworks.zircon.api.builder.screen.ScreenBuilder
import org.hexworks.zircon.api.resource.BuiltInCP437TilesetResource
import org.hexworks.zircon.api.resource.ColorThemeResource

object KotlinPlayground {

    private val SIZE = Sizes.create(50, 30)
    private val TILESET = BuiltInCP437TilesetResource.TAFFER_20X20

    @JvmStatic
    fun main(args: Array<String>) {

        val tileGrid = SwingApplications.startTileGrid(AppConfigs.newConfig()
                .withDefaultTileset(TILESET)
                .withSize(SIZE)
                .withDebugMode(true)
                .build())

        val screen = ScreenBuilder.createScreenFor(tileGrid)

        val panel = Components.panel()
                .wrapWithBox()
                .withSize(Sizes.create(48, 20))
                .build()

        panel.addComponent(Components.header()
                .withText("Plain button example"))

        panel.addComponent(Components.label()
                .withPosition(Positions.create(0, 1))
                .withText("Some label with text"))

        val labelLeft = Components.label()
                .withPosition(Positions.create(0, 2))
                .withText("Label left ")
                .build()
        val btn = Components.button()
                .withPosition(Positions.topRightOf(labelLeft))
                .withDecorationRenderers()
                .withText("Button")
                .build()

        val labelRight = Components.label()
                .withPosition(Positions.topRightOf(btn))
                .withText(" Label right")


        panel.addComponent(labelLeft)
        panel.addComponent(btn)
        panel.addComponent(labelRight)


        panel.addComponent(Components.label()
                .withPosition(Positions.create(0, 3))
                .withText("Another label with text"))

        screen.addComponent(panel)

        screen.display()
        screen.applyColorTheme(ColorThemeResource.CYBERPUNK.getTheme())

    }

}