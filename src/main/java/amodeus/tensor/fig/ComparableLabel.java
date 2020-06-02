/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.tensor.fig;

import java.util.Objects;

import javax.annotation.Nonnull;

/* package */ class ComparableLabel implements Comparable<ComparableLabel> {
    private final int index;
    /** may not be null */
    private String string;

    public ComparableLabel(int index) {
        this.index = index;
        string = "";
    }

    @Override
    public int compareTo(@Nonnull ComparableLabel comparableLabel) {
        return Integer.compare(index, comparableLabel.index);
    }

    public void setString(String string) {
        this.string = Objects.requireNonNull(string);
    }

    @Override
    public String toString() {
        return string;
    }
}
