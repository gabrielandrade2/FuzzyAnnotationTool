package sociocom.fuzzyannotation;

import sociocom.fuzzyannotation.ui.annotation.BaseAnnotationUI;
import sociocom.fuzzyannotation.ui.annotation.HighlightAnnotationUI;
import sociocom.fuzzyannotation.ui.annotation.PointWiseAnnotationUI;

public enum WindowType {

    PointWiseAnnotationUI("Point-Wise Annotation", PointWiseAnnotationUI.class),
    HighlightAnnotationUI("Highlight Annotation", HighlightAnnotationUI.class);

    public final String name;
    public final Class<? extends BaseAnnotationUI> clazz;

    WindowType(String name, Class<? extends BaseAnnotationUI> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<? extends BaseAnnotationUI> getClazz() {
        return clazz;
    }
}
