package sociocom.fuzzyannotation;

public class Annotation implements Comparable<Annotation> {

    private final int start;
    private final int end;
    private final String tag;

    private int startSpan = -1;
    private int endSpan = -1;

    public Annotation(int start, String tag) {
        this.start = start;
        this.end = start;
        this.tag = tag;
    }

    public Annotation(int start, int end, String tag) {
        this.start = start;
        this.end = end;
        this.tag = tag;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public String tag() {
        return tag;
    }

    public void setSpan(int start, int end) {
        startSpan = start;
        endSpan = end;
    }

    public int getStartSpan() {
        return startSpan;
    }

    public int getEndSpan() {
        return endSpan;
    }

    @Override
    public int compareTo(Annotation o) {
        return Integer.compare(start, o.start);
    }
}
