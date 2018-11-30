package no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag;

import no.nav.foreldrepenger.domene.inngangsvilkaar.PersonStatusType;
import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class MedlemskapsvilkårGrunnlag implements VilkårGrunnlag {

    /**
     * Om bruker er medlem
     */
    private boolean brukerErMedlem;
    /**
     * Status på personen (valgene er: Bosatt, Utvandret eller Død)
     */
    private PersonStatusType personStatusType;
    /**
     * Om bruker er pliktig eller frivillig medlem
     */
    private boolean brukerAvklartPliktigEllerFrivillig;
    /**
     * Om bruker er bosatt i Norge
     */
    private boolean brukerAvklartBosatt;
    /**
     * Om bruker har oppholdsrett
     */
    private boolean brukerAvklartOppholdsrett;
    /**
     * Om bruker har lovlig opphold i Norge
     */
    private boolean brukerAvklartLovligOppholdINorge;
    /**
     * Om bruker er norsk eller nordisk statsborger
     */
    private boolean brukerNorskNordisk;
    /**
     * Om bruker er EU/EØS borger
     */
    private boolean brukerBorgerAvEUEOS;

    private boolean medlemskapManueltSattTilOpphørtPgaTPSEndringer;

    /**
     * Har søker arbeidsforhold (ansettelsesperiode) uten sluttdato eller med sluttdato frem i tid som
     * dekker skjæringstidspunktet, og har pensjonsgivende inntekt i dette arbeidsforholdet?
     */
    private boolean harSøkerArbeidsforholdOgInntekt;

    public MedlemskapsvilkårGrunnlag() {
    }

    public MedlemskapsvilkårGrunnlag(boolean brukerErMedlem, PersonStatusType personStatusType, boolean brukerNorskNordisk, boolean brukerBorgerAvEUEOS, boolean medlemskapManueltSattTilOpphørtPgaTPSEndringer) {
        this.brukerErMedlem = brukerErMedlem;
        this.personStatusType = personStatusType;
        this.brukerNorskNordisk = brukerNorskNordisk;
        this.brukerBorgerAvEUEOS = brukerBorgerAvEUEOS;
        this.medlemskapManueltSattTilOpphørtPgaTPSEndringer = medlemskapManueltSattTilOpphørtPgaTPSEndringer;
    }

    public boolean isBrukerErMedlem() {
        return brukerErMedlem;
    }

    public PersonStatusType getPersonStatusType() {
        return personStatusType;
    }

    public boolean isBrukerAvklartBosatt() {
        return brukerAvklartBosatt;
    }

    public boolean isBrukerAvklartOppholdsrett() {
        return brukerAvklartOppholdsrett;
    }

    public boolean isBrukerAvklartLovligOppholdINorge() {
        return brukerAvklartLovligOppholdINorge;
    }

    public boolean isBrukerNorskNordisk() {
        return brukerNorskNordisk;
    }

    public boolean isBrukerBorgerAvEUEOS() {
        return brukerBorgerAvEUEOS;
    }

    public boolean isBrukerAvklartPliktigEllerFrivillig() {
        return brukerAvklartPliktigEllerFrivillig;
    }

    public boolean isMedlemskapOpphørtPågrunnAvEndredePersonopplysningerITPS() {
        return medlemskapManueltSattTilOpphørtPgaTPSEndringer;
    }

    public boolean harSøkerArbeidsforholdOgInntekt() {
        return harSøkerArbeidsforholdOgInntekt;
    }

    public void setBrukerAvklartBosatt(boolean brukerAvklartBosatt) {
        this.brukerAvklartBosatt = brukerAvklartBosatt;
    }

    public void setBrukerAvklartOppholdsrett(boolean brukerAvklartOppholdsrett) {
        this.brukerAvklartOppholdsrett = brukerAvklartOppholdsrett;
    }

    public void setBrukerAvklartLovligOppholdINorge(boolean brukerAvklartLovligOppholdINorge) {
        this.brukerAvklartLovligOppholdINorge = brukerAvklartLovligOppholdINorge;
    }

    public void setBrukerAvklartPliktigEllerFrivillig(boolean brukerAvklartPliktigEllerFrivillig) {
        this.brukerAvklartPliktigEllerFrivillig = brukerAvklartPliktigEllerFrivillig;
    }

    public void setHarSøkerArbeidsforholdOgInntekt(boolean harSøkerArbeidsforholdOgInntekt) {
        this.harSøkerArbeidsforholdOgInntekt = harSøkerArbeidsforholdOgInntekt;
    }
}
