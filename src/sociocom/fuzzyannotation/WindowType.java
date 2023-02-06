package sociocom.fuzzyannotation;

public enum WindowType {

    PointWiseAnnotationUI("Point-Wise Annotation"),
    HighlightAnnotationUI("Highlight Annotation");

    public final String name;

    WindowType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
