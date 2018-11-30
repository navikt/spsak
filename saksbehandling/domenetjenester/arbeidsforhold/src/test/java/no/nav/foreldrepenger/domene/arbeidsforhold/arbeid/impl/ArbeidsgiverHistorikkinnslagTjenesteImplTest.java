package no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class ArbeidsgiverHistorikkinnslagTjenesteImplTest {

    private static final String PRIVATPERSON_NAVN = "Mikke Mus";
    private static final AktørId AKTØR_ID = new AktørId("123123123123");
    private static final String ORGNR = "999888777";
    private static final String ORG_NAVN = "Andeby Bank";
    private static final Virksomhet VIRKSOMHET = lagVirksomhet();
    private static final ArbeidsforholdRef ARBEIDSFORHOLD_REF = ArbeidsforholdRef.ref("f29f40fjcm30abcd");
    private static BeregningsgrunnlagPrStatusOgAndel bgAndel;

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private EntityManager em = repoRule.getEntityManager();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(em);

    private static AktivitetStatus aktivitetstatus;
    private static OpptjeningAktivitetType arbeidsforholdType;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();
    @Mock
    private TpsTjeneste tpsTjeneste;

    private ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste;

    @Before
    public void setup() {
        aktivitetstatus = kodeverkRepository.finn(AktivitetStatus.class, AktivitetStatus.ARBEIDSTAKER);
        arbeidsforholdType = kodeverkRepository.finn(OpptjeningAktivitetType.class, OpptjeningAktivitetType.VARTPENGER);
        bgAndel = mockBeregningsgrunnlag();
        when(tpsTjeneste.hentBrukerForAktør(any(AktørId.class))).thenReturn(Optional.of(lagPersoninfo()));
        arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(tpsTjeneste);
    }

    private Personinfo lagPersoninfo() {
        return new Personinfo.Builder()
            .medAktørId(AKTØR_ID)
            .medPersonIdent(new PersonIdent("123123123"))
            .medNavBrukerKjønn(NavBrukerKjønn.MANN)
            .medFødselsdato(LocalDate.now())
            .medNavn(PRIVATPERSON_NAVN)
            .build();
    }

    @Test
    public void skal_lage_tekst_for_arbeidsgiver_privatperson_uten_arbref() {
        // Act
        String arbeidsgiverNavn = arbeidsgiverHistorikkinnslagTjeneste.lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver.person(AKTØR_ID));

        // Assert
        assertThat(arbeidsgiverNavn).isEqualTo("Mikke Mus");
        }

    @Test
    public void skal_lage_tekst_for_arbeidsgiver_privatperson_med_arbref() {
        // Act
        String arbeidsgiverNavn = arbeidsgiverHistorikkinnslagTjeneste.lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver.person(AKTØR_ID), ARBEIDSFORHOLD_REF);

        // Assert
        assertThat(arbeidsgiverNavn).isEqualTo("Mikke Mus ...abcd");
    }

    @Test
    public void skal_lage_tekst_for_arbeidsgiver_virksomhet_uten_arbref() {
        // Act
        String arbeidsgiverNavn = arbeidsgiverHistorikkinnslagTjeneste.lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver.virksomhet(VIRKSOMHET));

        // Assert
        assertThat(arbeidsgiverNavn).isEqualTo("Andeby Bank (999888777)");
    }

    @Test
    public void skal_lage_tekst_for_arbeidsgiver_virksomhet_med_arbref() {
        // Act
        String arbeidsgiverNavn = arbeidsgiverHistorikkinnslagTjeneste.lagArbeidsgiverHistorikkinnslagTekst(Arbeidsgiver.virksomhet(VIRKSOMHET), ARBEIDSFORHOLD_REF);

        // Assert
        assertThat(arbeidsgiverNavn).isEqualTo("Andeby Bank (999888777) ...abcd");
    }

    @Test
    public void skal_returnere_opptjeningsaktivitet_dersom_arbeidsgiver_er_null() {
        // Act
        String arbeidsgiverNavn = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(bgAndel);

        // Assert
        assertThat(arbeidsgiverNavn).isEqualTo("Vartpenger");
    }


    private static BeregningsgrunnlagPrStatusOgAndel mockBeregningsgrunnlag() {
        Beregningsgrunnlag bg = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode p = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(LocalDate.now(), null).build(bg);
        return buildAndel(p);
    }

    private static BeregningsgrunnlagPrStatusOgAndel buildAndel(BeregningsgrunnlagPeriode periode) {
        return BeregningsgrunnlagPrStatusOgAndel.builder().medAktivitetStatus(aktivitetstatus).medArbforholdType(arbeidsforholdType).build(periode);
    }

    private static Virksomhet lagVirksomhet() {
        VirksomhetEntitet.Builder b = new VirksomhetEntitet.Builder();
        b.medOrgnr(ORGNR).medNavn(ORG_NAVN);
        return b.build();
    }

}
