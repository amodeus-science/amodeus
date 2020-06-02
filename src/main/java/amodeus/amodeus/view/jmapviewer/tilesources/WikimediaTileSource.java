/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.jmapviewer.tilesources;

import amodeus.amodeus.view.jmapviewer.interfaces.TileSource;

/** Wikimedia experimental */
public class WikimediaTileSource extends AbstractOsmTileSource {
    private static final String PATTERN = "https://maps.wikimedia.org/osm-intl";
    // ---
    /** singleton instance */
    public static final TileSource INSTANCE = new WikimediaTileSource();

    private WikimediaTileSource() {
        super("Wikimedia", PATTERN, "wikimedia");
    }

    @Override
    public String getBaseUrl() {
        return PATTERN;
    }

    @Override
    public int getMaxZoom() {
        return 18;
    }
}