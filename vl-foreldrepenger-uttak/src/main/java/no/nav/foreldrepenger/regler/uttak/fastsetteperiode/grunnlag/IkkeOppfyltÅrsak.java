package no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag;

public enum IkkeOppfyltÅrsak implements Årsak {

    // Uttak årsaker
    IKKE_STØNADSDAGER_IGJEN(4002, "Ikke Stønadsdager igjen"),
    MOR_HAR_IKKE_OMSORG(4003, "Mor har ikke omsorg"),
    HULL_MELLOM_FORELDRENES_PERIODER(4005, "Hull mellom foreldrenes perioder"),
    FAR_HAR_IKKE_OMSORG(4012, "Far har ikke omsorg"),
    MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL(4013, "Mor søker fellesperiode før 12 uker før termin/fødsel"),
    SØKNADSFRIST(4020, "Brudd på søknadsfrist", false),
    UTTAK_ETTER_3_ÅRSGRENSE(4022, "Uttak etter 3 årsgrense"),
    ARBEID_MER_ENN_NULL_PROSENT(4023, "Arbeider mer enn 0 prosent"),
    ARBEID_HUNDRE_PROSENT_ELLER_MER(4025, "Arbeider 100 prosent eller mer"),
    OPPHOLD_IKKE_SAMTIDIG_UTTAK(4084, "Opphold på grunn av den andre forelderens vedtak"),
    IKKE_SAMTYKKE(4085, "Ikke samtykke mellom foreldrene"),
    OPPHOLD_UTSETTELSE(4086, "Opphold på grunn av den andre forelderens vedtak"),
    MOR_TAR_IKKE_ALLE_UKENE(4095, "Mor tar ikke alle ukene"),
    AKTIVITETSKRAVET(4070, "Aktivitetskrav - innleggelse ikke dokumentert"),

    // Overføring årsaker
    DEN_ANDRE_PART_SYK_SKADET_IKKE_OPPFYLT(4007, "Den andre part syk/skadet ikke oppfylt"),
    DEN_ANDRE_PART_INNLEGGELSE_IKKE_OPPFYLT(4008, "Den andre part innleggelse ikke oppfylt"),

    // Utsettelse årsaker
    UTSETTELSE_FØR_TERMIN_FØDSEL(4030, "Avslag utsettelse før termin/fødsel"),
    FERIE_INNENFOR_DE_FØRSTE_6_UKENE(4031, "Ferie/arbeid innenfor de første 6 ukene"),
    FERIE_SELVSTENDIG_NÆRINGSDRIVENDSE_FRILANSER(4032, "Ferie - selvstendig næringsdrivende/frilanser"),
    IKKE_LOVBESTEMT_FERIE(4033, "Ikke lovbestemt ferie"),
    INGEN_STØNADSDAGER_IGJEN_UTSETTELSE(4034, "Avslag utsettelse - ingen stønadsdager igjen"),
    IKKE_HELTIDSARBEID(4037, "Ikke heltidsarbeid"),
    SØKERS_SYKDOM_SKADE_IKKE_OPPFYLT(4038, "Søkers sykdom/skade ikke oppfylt"),
    SØKERS_INNLEGGELSE_IKKE_OPPFYLT(4039, "Søkers innleggelse ikke oppfylt"),
    BARNETS_INNLEGGELSE_IKKE_OPPFYLT(4040, "Barnets innleggelse ikke oppfylt"),
    UTSETTELSE_FERIE_PÅ_BEVEGELIG_HELLIGDAG(4041, "Avslag utsettelse ferie på bevegelig helligdag"),

    //Endringssøknad
    SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT(4080 , "Søker har søkt om gradert uttak etter at perioden med delvis arbeid er påbegynt"),
    SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT(4081, "Søker har søkt om utsettelse pga ferie etter ferien er begynt"),
    SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT(4082, "Søker har søkt om utsettelse pga arbeid etter ferien er begynt");

    private int id;
    private String beskrivelse;
    private boolean trekkDager = true;

    IkkeOppfyltÅrsak(int id, String beskrivelse, boolean trekkDager) {
        this(id, beskrivelse);
        this.trekkDager = trekkDager;
    }

    IkkeOppfyltÅrsak(int id, String beskrivelse) {
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

    public boolean isTrekkDager() {
        return trekkDager;
    }
}
