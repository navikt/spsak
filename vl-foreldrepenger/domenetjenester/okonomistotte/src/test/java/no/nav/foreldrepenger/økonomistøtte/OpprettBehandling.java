package no.nav.foreldrepenger.økonomistøtte;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;

import java.time.LocalDate;
import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandling.impl.FinnAnsvarligSaksbehandler;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.typer.AktørId;


class OpprettBehandling {

    public final static long SATS = 66221L;
    public final static LocalDateTime nå = LocalDateTime.now();

    @SuppressWarnings("deprecation")
    public static Personinfo opprettPersonInfo() {
        return new Personinfo.Builder()
            .medAktørId(new AktørId("123"))
            .medFnr("12345678901")
            .medNavn("Kari Nordmann")
            .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
            .medNavBrukerKjønn(KVINNE)
            .build();
    }

    public static ScenarioMorSøkerEngangsstønad opprettBehandlingMedTermindato() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        scenario.medSøknadHendelse()
            .medTerminbekreftelse(scenario.medSøknadHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now()))
            .medAntallBarn(1);
        scenario.medBekreftetHendelse()
            .medTerminbekreftelse(scenario.medBekreftetHendelse().getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.now().plusDays(40))
                .medNavnPå("LEGEN MIN")
                .medUtstedtDato(LocalDate.now().minusDays(7)))
            .medAntallBarn(1);
        return scenario;
    }

    public static void genererBehandlingOgResultat(Behandling behandling, VedtakResultatType resultat, long antallBarn) {
        Behandlingsresultat.builderForInngangsvilkår()
            .leggTilKonsekvensForYtelsen(KonsekvensForYtelsen.INGEN_ENDRING)
            .medRettenTil(RettenTil.HAR_RETT_TIL_FP)
            .medVedtaksbrev(Vedtaksbrev.INGEN)
            .buildFor(behandling);
        if (VedtakResultatType.INNVILGET.equals(resultat)) {
            if (antallBarn <= 0) {
                throw new IllegalStateException("Ved innvilgelse må antall barn angis");
            }
            Beregning beregning = new Beregning(SATS, antallBarn, SATS * antallBarn, nå);
            BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
        }
    }

    public static BehandlingVedtak opprettBehandlingVedtak(Behandlingsresultat behandlingsresultat, VedtakResultatType resultatType) {
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandlingsresultat.getBehandling());
        return BehandlingVedtak.builder()
            .medVedtaksdato(LocalDate.now().minusDays(3))
            .medAnsvarligSaksbehandler(ansvarligSaksbehandler)
            .medVedtakResultatType(resultatType)
            .medBehandlingsresultat(behandlingsresultat)
            .build();
    }

    public static BehandlingVedtak opprettBehandlingVedtak(Behandlingsresultat behandlingsresultat) {
        return opprettBehandlingVedtak(behandlingsresultat, VedtakResultatType.INNVILGET);
    }

}
