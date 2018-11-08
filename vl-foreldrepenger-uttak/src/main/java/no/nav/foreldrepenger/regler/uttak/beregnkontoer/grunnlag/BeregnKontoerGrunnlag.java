package no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag;

import java.time.LocalDate;

import no.nav.fpsak.nare.doc.RuleDocumentationGrunnlag;

@RuleDocumentationGrunnlag
public class BeregnKontoerGrunnlag {

    // Input
    private boolean erFødsel;

    private int antallBarn;
    private boolean morRett;
    private boolean farRett;
    private Dekningsgrad dekningsgrad;
    private boolean farAleneomsorg;
    private boolean morAleneomsorg;
    private LocalDate familiehendelsesdato;

    private BeregnKontoerGrunnlag() {
    }

    public int getAntallBarn() {
        return antallBarn;
    }

    public boolean isMorRett() {
        return morRett;
    }

    public boolean isFarRett() {
        return farRett;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public boolean isFarAleneomsorg() {
        return farAleneomsorg;
    }

    public boolean isMorAleneomsorg() {
        return morAleneomsorg;
    }

    public boolean erFødsel() {
        return erFødsel;
    }

    public LocalDate getFamiliehendelsesdato() {
        return familiehendelsesdato;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregnKontoerGrunnlag kladd = new BeregnKontoerGrunnlag();

        public Builder medAntallBarn(int antallBarn) {
            kladd.antallBarn = antallBarn;
            return this;
        }

        public Builder morRett(boolean morHarRett) {
            kladd.morRett = morHarRett;
            return this;
        }

        public Builder farRett(boolean farHarRett) {
            kladd.farRett = farHarRett;
            return this;
        }

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder farAleneomsorg(boolean farAleneomsorg) {
            kladd.farAleneomsorg = farAleneomsorg;
            return this;
        }

        public Builder morAleneomsorg(boolean morAleneomsorg) {
            kladd.morAleneomsorg = morAleneomsorg;
            return this;
        }

        public Builder medFamiliehendelsesdato(LocalDate famliehendelsesdato) {
            kladd.familiehendelsesdato = famliehendelsesdato;
            return this;
        }

        public Builder erFødsel(boolean erFødsel) {
            kladd.erFødsel = erFødsel;
            return this;
        }

        public BeregnKontoerGrunnlag build() {
            return kladd;
        }
    }
}
