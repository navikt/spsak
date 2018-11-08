package no.nav.foreldrepenger.dokumentbestiller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalRestriksjon;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

class DokumentBestillerApplikasjonTjenesteTestUtil {
    static final AktørId AKTØR_ID_BRUKER = new AktørId("100001");
    static final AktørId AKTØR_ID_VERGE = new AktørId("100020");
    static final AktørId AKTØR_ID_UTLENDING = new AktørId("100030");
    static final String SAKSPART_ID = "12345678901";
    static final String SAKSPART_NAVN = "Bjellesauen";

    static TpsTjeneste mockHentBrukerForAktør() {
        TpsTjeneste tpsTjeneste = mock(TpsTjeneste.class);
        PersonIdent personIdent = new PersonIdent(SAKSPART_ID);
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(AKTØR_ID_BRUKER)
            .medPersonIdent(personIdent)
            .medNavn(SAKSPART_NAVN)
            .medFødselsdato(LocalDate.now())
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .build();
        when(tpsTjeneste.hentBrukerForAktør(any(AktørId.class))).thenReturn(Optional.of(personinfo));
        when(tpsTjeneste.hentAdresseinformasjon(Mockito.eq(personIdent))).thenReturn(lagReferanseAdresseForDokumentutsending(false));
        return tpsTjeneste;
    }

    static Adresseinfo lagReferanseAdresseForDokumentutsending(boolean norsk) {
        return new Adresseinfo.Builder(AdresseType.BOSTEDSADRESSE, new PersonIdent(SAKSPART_ID), SAKSPART_NAVN, PersonstatusType.BOSA)
            .medAdresselinje1("linje1")
            .medAdresselinje2("linje2")
            .medAdresselinje3("linje3")
            .medAdresselinje4("linje4")
            .medLand(norsk ? null : "SWE")
            .build();
    }

    static void oppsettForDokumentMalType(DokumentMalType dokumentMalTypeMock, String type, boolean generisk, DokumentMalRestriksjon restriksjon, DokumentRepository dokumentRepository) {
        when(dokumentMalTypeMock.getKode()).thenReturn(type);
        when(dokumentMalTypeMock.erGenerisk()).thenReturn(generisk);
        when(dokumentMalTypeMock.getDokumentMalRestriksjon()).thenReturn(restriksjon);
        when(dokumentRepository.hentDokumentMalType(type)).thenReturn(dokumentMalTypeMock);
    }

    static BeregningsresultatFP buildBeregningsresultatFP(Boolean brukerErMottaker, VirksomhetEntitet virksomhet) {
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        BeregningsresultatPeriode brPeriode1 = buildBeregningsresultatPeriode(beregningsresultatFP, 11, 20);
        buildBeregningsresultatAndel(brPeriode1, brukerErMottaker, 2160, virksomhet);
        if (!brukerErMottaker) {
            buildBeregningsresultatAndel(brPeriode1, true, 0, virksomhet);
        }
        BeregningsresultatPeriode brPeriode2 = buildBeregningsresultatPeriode(beregningsresultatFP, 21, 30);
        buildBeregningsresultatAndel(brPeriode2, brukerErMottaker, 2160, virksomhet);
        if (!brukerErMottaker) {
            buildBeregningsresultatAndel(brPeriode2, true, 0, virksomhet);
        }
        return beregningsresultatFP;
    }

    private static void buildBeregningsresultatAndel(BeregningsresultatPeriode beregningsresultatPeriode, Boolean brukerErMottaker, int dagsats, VirksomhetEntitet virksomhet) {
        BeregningsresultatAndel.builder()
            .medBrukerErMottaker(brukerErMottaker)
            .medVirksomhet(virksomhet)
            .medDagsats(dagsats)
            .medStillingsprosent(BigDecimal.valueOf(100))
            .medUtbetalingsgrad(BigDecimal.ZERO)
            .medDagsatsFraBg(dagsats)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetstatus(AktivitetStatus.ARBEIDSTAKER)
            .build(beregningsresultatPeriode);
    }

    private static BeregningsresultatPeriode buildBeregningsresultatPeriode(BeregningsresultatFP beregningsresultatFP, int fom, int tom) {
        return BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(LocalDate.now().plusDays(fom), LocalDate.now().plusDays(tom))
            .build(beregningsresultatFP);
    }
}
