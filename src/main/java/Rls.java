import org.neo4j.graphdb.RelationshipType;

public enum Rls implements RelationshipType {
    HAS, CONTAINS, RELATE;

    public static Rls getType(String a, String b) {
        if (a.equals(Lbl.A.name()) && b.equals(Lbl.B.name()))
            return HAS;
        if (a.equals(Lbl.A.name()) && b.equals(Lbl.C.name()))
            return CONTAINS;
        if (a.equals(Lbl.A.name()) && b.equals(Lbl.D.name()))
            return HAS;
        if (a.equals(Lbl.B.name()) && b.equals(Lbl.E.name()))
            return RELATE;
        if (a.equals(Lbl.B.name()) && b.equals(Lbl.D.name()))
            return HAS;
        if (a.equals(Lbl.D.name()) && b.equals(Lbl.A.name()))
            return RELATE;
        if (a.equals(Lbl.D.name()) && b.equals(Lbl.E.name()))
            return RELATE;
        if (a.equals(Lbl.E.name()) && b.equals(Lbl.C.name()))
            return CONTAINS;
        return null;
    }
}
