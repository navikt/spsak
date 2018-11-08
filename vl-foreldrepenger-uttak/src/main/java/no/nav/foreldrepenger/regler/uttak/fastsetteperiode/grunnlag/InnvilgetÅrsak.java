package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum InnvilgetÅrsak implements Årsak {

    // Uttak årsaker
    FELLESPERIODE_ELLER_FORELDREPENGER(2002, "Fellesperiode/foreldrepenger"),
    KVOTE_ELLER_OVERFØRT_KVOTE(2003, "Kvote/overført kvote"),
    FORELDREPENGER_KUN_FAR_HAR_RETT(2004, "Foreldrepenger, kun far har rett"),
    FORELDREPENGER_ALENEOMSORG(2005, "Foreldrepenger ved aleneomsorg"),
    FORELDREPENGER_FØR_FØDSEL(2006, "Foreldrepenger før fødsel"),
    FORELDREPENGER_KUN_MOR_HAR_RETT(2007, "Foreldrepenger, kun mor har rett"),
    GRADERING_FELLESPERIODE_ELLER_FORELDREPENGER(2030, "Gradering av fellesperiode/foreldrepenger"),
    GRADERING_KVOTE_ELLER_OVERFØRT_KVOTE(2031, "Gradering av kvote/overført kvote"),
    GRADERING_ALENEOMSORG(2032, "Gradering ved aleneomsorg"),
    GRADERING_FORELDREPENGER_KUN_FAR_HAR_RETT(2033, "Gradering foreldrepenger, kun far har rett"),
    GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT(2034, "Gradering foreldrepenger, kun mor har rett"),

    // Overføring årsaker
    OVERFØRING_ANNEN_PART_SYKDOM_SKADE(2021, "Overføring - annen part sykdom/skade"),
    OVERFØRING_ANNEN_PART_INNLAGT(2022, "Overføring - annen part innlagt"),

    //Utsettelse årsaker
    UTSETTELSE_GYLDING_PGA_FERIE(2010, "Utsettelse pga ferie"),
    UTSETTELSE_GYLDING_PGA_100_PROSENT_ARBEID(2011, "Utsettelse pga 100% arbeid"),
    UTSETTELSE_GYLDING_PGA_INNLEGGELSE(2012, "Utsettelse pga innleggelse"),
    UTSETTELSE_GYLDING_PGA_BARN_INNLAGT(2013, "Utsettelse pga innleggelse barn"),
    UTSETTELSE_GYLDING_PGA_SYKDOM(2014, "Utsettelse pga sykdom");

    private int id;
    private String beskrivelse;

    InnvilgetÅrsak(int id, String beskrivelse) {
        this.id = id;
        this.beskrivelse = beskrivelse;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getBeskrivelse() {
        return beskrivelse;
    }
}
