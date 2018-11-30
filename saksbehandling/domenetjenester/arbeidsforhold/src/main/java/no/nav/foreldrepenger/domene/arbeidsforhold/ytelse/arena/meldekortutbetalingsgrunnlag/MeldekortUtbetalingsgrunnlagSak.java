package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.arena.meldekortutbetalingsgrunnlag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class MeldekortUtbetalingsgrunnlagSak {

    private RelatertYtelseType type;
    private RelatertYtelseTilstand tilstand;
    private Fagsystem kilde;
    private Saksnummer saksnummer;
    private String sakStatus;
    private String vedtakStatus;

    private LocalDate kravMottattDato;
    private LocalDate vedtattDato;
    private LocalDate vedtaksPeriodeFom;
    private LocalDate vedtaksPeriodeTom;

    List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortene;

    private MeldekortUtbetalingsgrunnlagSak() { // NOSONAR
    }

    public RelatertYtelseType getYtelseType() {
        return type;
    }

    public RelatertYtelseTilstand getYtelseTilstand() {
        return tilstand;
    }

    public Fagsystem getKilde() {
        return kilde;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public String getSakStatus() {
        return sakStatus;
    }

    public String getVedtakStatus() {
        return vedtakStatus;
    }

    public LocalDate getKravMottattDato() {
        return kravMottattDato;
    }

    public LocalDate getVedtattDato() {
        return vedtattDato;
    }

    public LocalDate getVedtaksPeriodeFom() {
        return vedtaksPeriodeFom;
    }

    public LocalDate getVedtaksPeriodeTom() {
        return vedtaksPeriodeTom;
    }

    public List<MeldekortUtbetalingsgrunnlagMeldekort> getMeldekortene() {
        return meldekortene;
    }

    public static class MeldekortSakBuilder {
        private final MeldekortUtbetalingsgrunnlagSak sak;

        MeldekortSakBuilder(MeldekortUtbetalingsgrunnlagSak sak) {
            this.sak = sak;
            sak.meldekortene = new ArrayList<>();
        }

        public static MeldekortSakBuilder ny() {
            return new MeldekortSakBuilder(new MeldekortUtbetalingsgrunnlagSak());
        }

        public MeldekortSakBuilder medType(RelatertYtelseType type) {
            this.sak.type = type;
            return this;
        }

        public MeldekortSakBuilder medTilstand(RelatertYtelseTilstand tilstand) {
            this.sak.tilstand = tilstand;
            return this;
        }

        public MeldekortSakBuilder medKilde(Fagsystem kilde) {
            this.sak.kilde = kilde;
            return this;
        }

        public MeldekortSakBuilder medSaksnummer(Saksnummer saksnummer) {
            this.sak.saksnummer = saksnummer;
            return this;
        }

        public MeldekortSakBuilder medSakStatus(String sakStatus) {
            this.sak.sakStatus = sakStatus;
            return this;
        }

        public MeldekortSakBuilder medVedtakStatus(String vedtakStatus) {
            this.sak.vedtakStatus = vedtakStatus;
            return this;
        }

        public MeldekortSakBuilder medKravMottattDato(LocalDate kravMottattDato) {
            this.sak.kravMottattDato = kravMottattDato;
            return this;
        }

        public MeldekortSakBuilder medVedtattDato(LocalDate vedtattDato) {
            this.sak.vedtattDato = vedtattDato;
            return this;
        }

        public MeldekortSakBuilder medVedtaksPeriodeFom(LocalDate vedtaksPeriodeFom) {
            this.sak.vedtaksPeriodeFom = vedtaksPeriodeFom;
            return this;
        }

        public MeldekortSakBuilder medVedtaksPeriodeTom(LocalDate vedtaksPeriodeTom) {
            this.sak.vedtaksPeriodeTom = vedtaksPeriodeTom;
            return this;
        }

        public MeldekortSakBuilder leggTilMeldekort(List<MeldekortUtbetalingsgrunnlagMeldekort> meldekortene) {
            this.sak.meldekortene.addAll(meldekortene);
            return this;
        }


        public MeldekortUtbetalingsgrunnlagSak build() {
            return this.sak;
        }

    }
}
