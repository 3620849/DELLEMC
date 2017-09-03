import org.neo4j.graphdb.Label;

import java.util.concurrent.ThreadLocalRandom;

public enum Lbl implements Label {
    A, B, C, D, E;
    int count = 0;
    private static Lbl[] a = {B, C, D, null};
    private static Lbl[] b = {E, D, null};
    private static Lbl[] d = {A, E, null};
    private static Lbl[] e = {C, null};

    public static Lbl getRandomFor(Label l) {
        if (l.name().equals(Lbl.A.name())) {
            return a[getRandom(a.length)];
        } else if (l.name().equals(Lbl.B.name())) {
            return b[getRandom(b.length)];
        } else if (l.name().equals(Lbl.D.name())) {
            return d[getRandom(d.length)];
        } else if (l.name().equals(Lbl.E.name())) {
            return e[getRandom(e.length)];
        }
        return null;
    }

    private static int getRandom(int a) {
        if (a <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(0, a);
    }

    public static int getMaxChilds(Label nLabel) {
        if (nLabel.name().equals(Lbl.A.name())) {
            return getPosibelNumber(2 * (a.length - 1), a);
        } else if (nLabel.name().equals(Lbl.B.name())) {
            return getPosibelNumber(2 * (b.length - 1), b);
        } else if (nLabel.name().equals(Lbl.D.name())) {
            return getPosibelNumber(2 * (d.length - 1), d);
        } else if (nLabel.name().equals(Lbl.E.name())) {
            return getPosibelNumber(2 * (e.length - 1), e);
        }
        return 0;
    }

    private static int getPosibelNumber(int max, Lbl[] eges) {

        for (int i = 0; i < eges.length - 1; ++i) {
            max = max - eges[i].count;
        }
        return max;
    }
}

