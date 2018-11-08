package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum Manuellbehandlingårsak {
    STØNADSKONTO_TOM(5001, "Stønadskonto tom for stønadsdager"),
    UGYLDIG_STØNADSKONTO(5002, "Ugyldig stønadskonto"),
    BEGRUNNELSE_IKKE_GYLDIG(5003, "Begrunnelse ikke gyldig"),
    AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT(5004, "Aktivitetskravet må sjekkes manuelt"),
    MANGLENDE_SØKT_PERIODE(5005, "Manglende søkt periode"),
    AVKLAR_ARBEID(5006, "Avklar arbeid"),
    ADOPSJON_IKKE_IMPLEMENTERT(5007, "Adopsjon ikke implementert"), //TODO midlertidig inntil adopsjon er implemmentert
    FORELDREPENGER_IKKE_IMPLEMENTERT(5008, "Foreldrepenger ikke implementert"), //TODO midlertidig inntil foreldreopenger/aleneomsorg er implemmentert
    SØKER_HAR_IKKE_OMSORG(5009, "Søker har ikke omsorg for barnet"),
    SØKNADSFRIST(5010, "Uttak ikke gyldig pga søknadsfrist"),
    IKKE_GYLDIG_GRUNN_FOR_UTSETTELSE(5011, "Ikke gyldig grunn for utsettelse"),
    PERIODE_UAVKLART(5012, "Periode uavklart av saksbehandler"),
    IKKE_SAMTYKKE(5013, "Ikke samtykke mellom foreldrene"),
    VURDER_SAMTIDIG_UTTAK(5014, "Vurder samtidig uttak"),
    VURDER_OVERFØRING(5016, "Vurder søknad om overføring av kvote");

    private int id;
    private String beskrivelse;

    Manuellbehandlingårsak(int id, String beskrivelse) {
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
