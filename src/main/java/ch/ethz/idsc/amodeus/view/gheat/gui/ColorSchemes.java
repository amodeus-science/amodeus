/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.view.gheat.gui;

import ch.ethz.idsc.tensor.img.ColorDataGradients;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum ColorSchemes {
    Jet(StaticHelper.reverse(ColorDataGradients.JET)), //
    Classic(StaticHelper.reverse(ColorDataGradients.CLASSIC)), //
    Fire(GheatPalettes.FIRE.colorDataIndexed), //
    Sunset(StaticHelper.reverse(ColorDataGradients.SUNSET)), //
    Solar(StaticHelper.reverse(ColorDataGradients.SOLAR)), //
    Pbj(GheatPalettes.PBJ.colorDataIndexed), //
    Parula(StaticHelper.reverse(ColorDataGradients.PARULA)), //
    ParulaDark(StaticHelper.forward(ColorDataGradients.PARULA)), //
    Density(StaticHelper.reverse(ColorDataGradients.DENSITY)), //
    Pgaitch(GheatPalettes.PGAITCH.colorDataIndexed), //
    Omg(GheatPalettes.OMG.colorDataIndexed), //
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

    public final ColorDataIndexed colorDataIndexed;

    private ColorSchemes(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
    }

}
