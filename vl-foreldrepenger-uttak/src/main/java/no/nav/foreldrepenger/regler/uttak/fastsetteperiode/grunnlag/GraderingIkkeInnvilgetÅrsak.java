package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum GraderingIkkeInnvilgetÅrsak {

    AVSLAG_PGA_FOR_TIDLIG_GRADERING(4504, "Gradering før uke 7");

    private int id;
    private String beskrivelse;

    GraderingIkkeInnvilgetÅrsak(int id, String beskrivelse) {
        this.id = id;
        this.beskrivelse = beskrivelse;
    }

    public int getId() {
        return id;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
}

