package ch.ethz.idsc.subare.plot;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.*;

public class VisualSet {
    private List<VisualRow> visualRows;
    private String plotLabel = "";
    private String domainAxisLabel = "";
    private String rangeAxisLabel = "";
    private ColorDataIndexed colorDataIndexed = ColorDataLists._001.cyclic();

    public VisualSet(VisualRow... visualRows) {
        this.visualRows = new ArrayList<>(Arrays.asList(visualRows));
        addLabels();
    }

    public List<VisualRow> visualRows() {
        return visualRows;
    }

    public VisualRow get(int index) {
        return visualRows.get(index);
    }

    public Optional<VisualRow> get(String label) {
        return visualRows.stream().filter(visualRow -> visualRow.getLabel().toString().equals(label)).findAny();
    }

    public String getPlotLabel() {
        return plotLabel;
    }

    public String getDomainAxisLabel() {
        return domainAxisLabel;
    }

    public String getRangeAxisLabel() {
        return rangeAxisLabel;
    }

    public VisualSet add(VisualRow visualRow) {
        visualRows.add(visualRow);
        addLabels();
        return this;
    }

    public void setRowLabel(int index, String string) {
        get(index).getLabel().setString(string);
    }

    public void setPlotLabel(String string) {
        plotLabel = string;
    }

    public void setDomainAxisLabel(String string) {
        domainAxisLabel = string;
    }

    public void setRangeAxisLabel(String string) {
        rangeAxisLabel = string;
    }

    public void setColors(ColorDataIndexed colorDataIndexed) {
        this.colorDataIndexed = colorDataIndexed;
    }

    private void addLabels() {
        ListIterator<VisualRow> it = this.visualRows.listIterator();
        while (it.hasNext()) {
            VisualRow visualRow = it.next();
            if (!visualRow.hasLabel())
                visualRow.setLabel(new ComparableLabel(it.previousIndex()));
            visualRow.setColor(colorDataIndexed.getColor(it.previousIndex()));
        }
    }

    public CategoryDataset categorical() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (VisualRow visualRow : visualRows)
            for (int i = 0; i < visualRow.getDomain().length(); i++)
                dataset.addValue(visualRow.getValues().Get(i).number().doubleValue(), //
                        visualRow.getLabelString(), //
                        String.valueOf(i));
        return dataset;
    }
}
