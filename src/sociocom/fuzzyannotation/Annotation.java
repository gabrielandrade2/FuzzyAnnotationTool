package sociocom.fuzzyannotation;

public record Annotation(int start, String tag) implements Comparable<Annotation> {

    @Override
    public int compareTo(Annotation o) {
        return Integer.compare(start, o.start);
    }
}
