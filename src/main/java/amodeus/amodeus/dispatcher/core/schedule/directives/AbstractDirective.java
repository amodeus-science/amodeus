package amodeus.amodeus.dispatcher.core.schedule.directives;

public class AbstractDirective implements Directive {
    private final boolean isModifiable;

    protected AbstractDirective(boolean isModifiable) {
        this.isModifiable = isModifiable;
    }

    @Override
    public boolean isModifiable() {
        return isModifiable;
    }
}
