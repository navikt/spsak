package no.nav.foreldrepenger.beregning.regelmodell.beregningsgrunnlag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum AktivitetStatus {
    ATFL("Arbeidstaker/Frilanser"),
    TY("Mottaker av tilstøtende ytelse"),
    SN("Selvstendig næringsdrivende"),
    ATFL_SN("Kombinasjon av arbeidstaker/frilanser og selvstendig næringsdrivende"),
    DP("Dagpenger"),
    AAP("Mottaker av arbeidsavklaringspenger"),
    BA("Brukers andel"),
    MS("Militær/Sivil"),
    UDEFINERT("Udefinert");

    private static final Set<AktivitetStatus> GRADERBARE_AKTIVITETER = new HashSet<>(Arrays.asList(ATFL, SN, ATFL_SN));

    private final String beskrivelse;

    AktivitetStatus(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public boolean erGraderbar() {
        return GRADERBARE_AKTIVITETER.contains(this);
    }

}
