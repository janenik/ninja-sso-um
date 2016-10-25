package models.sso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Selection of objects.
 */
public final class Selection<PK extends Serializable> implements Serializable {

    private static final Selection<Serializable> ALL = new Selection<>(Collections.emptyList(), Mode.ALL);

    private final List<PK> selection;

    private final Mode mode;

    private Selection(List<PK> selection, Mode mode) {
        this.selection = Collections.unmodifiableList(selection);
        this.mode = mode;
    }

    public boolean isAll() {
        return Mode.ALL.equals(mode);
    }

    public boolean isAllExcluding() {
        return Mode.ALL_EXCLUDING.equals(mode);
    }

    public boolean isOnly() {
        return Mode.ONLY.equals(mode);
    }

    public List<PK> getSelection() {
        return selection;
    }

    @Override
    public String toString() {
        if (isAll()) {
            return "ALL";
        }
        StringBuilder sb = new StringBuilder(mode.name());
        sb.append(':');
        int i = 0;
        for (PK pk : selection) {
            sb.append(pk);
            if (++i != selection.size()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Selection<?> other = (Selection<?>) o;
        return mode == other.mode && selection.equals(other.selection);

    }

    @Override
    public int hashCode() {
        int result = selection.hashCode();
        result = 31 * result + mode.hashCode();
        return result;
    }

    public static <PK extends Serializable> Selection<PK> only(Collection<PK> keys) {
        return new Selection<>(new ArrayList<>(keys), Mode.ONLY);
    }

    public static <PK extends Serializable> Selection<PK> allExcluding(Collection<PK> keys) {
        return new Selection<>(new ArrayList<>(keys), Mode.ALL_EXCLUDING);
    }

    @SuppressWarnings("unchecked")
    public static <PK extends Serializable> Selection<PK> all() {
        return (Selection<PK>) ALL;
    }

    private enum Mode {
        ALL, ALL_EXCLUDING, ONLY;
    }

    public static void main(String... args) {
        Selection<Long> ids = Selection.only(Arrays.asList(1L, 2L, 3L));
        System.out.println(ids.toString());

        ids = Selection.all();
        System.out.println(ids.toString());

        ids = Selection.allExcluding(Arrays.asList(1L, 2L, 3L));
        System.out.println(ids.toString());
    }
}
