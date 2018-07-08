/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.img.ColorDataGradients;

public enum ColorSchemes {
    Jet(StaticHelper.reverse(ColorDataGradients.JET)), //
    Classic(StaticHelper.reverse(ColorDataGradients.CLASSIC)), //
    Fire(GheatPalettes.FIRE.colorScheme), //
    Sunset(StaticHelper.reverse(ColorDataGradients.SUNSET)), //
    Solar(StaticHelper.reverse(ColorDataGradients.SOLAR)), //
    Pbj(GheatPalettes.PBJ.colorScheme), //
    Parula(StaticHelper.reverse(ColorDataGradients.PARULA)), //
    ParulaDark(StaticHelper.forward(ColorDataGradients.PARULA)), //
    Density(StaticHelper.reverse(ColorDataGradients.DENSITY)), //
    Pgaitch(GheatPalettes.PGAITCH.colorScheme), //
    Omg(GheatPalettes.OMG.colorScheme), //
    Orange(CustomPalettes.createOrange()), //
    OrangeContour(CustomPalettes.createOrangeContour()), //
    Green(CustomPalettes.createGreen()), //
    GreenContour(CustomPalettes.createGreenContour()), //
    Black(CustomPalettes.createBlack()), //
    Bone(StaticHelper.forward(ColorDataGradients.BONE)), //
    Cool(InternetPalettes.createCool()), //
    Copper(StaticHelper.reverse(ColorDataGradients.COPPER)), //
    Starrynight(StaticHelper.reverse(ColorDataGradients.STARRYNIGHT)), //
    CopperDark(StaticHelper.forward(ColorDataGradients.COPPER)), //
    StarrynightDark(StaticHelper.forward(ColorDataGradients.STARRYNIGHT)), //
    ;

    public final ColorScheme colorScheme;

    private ColorSchemes(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

}
