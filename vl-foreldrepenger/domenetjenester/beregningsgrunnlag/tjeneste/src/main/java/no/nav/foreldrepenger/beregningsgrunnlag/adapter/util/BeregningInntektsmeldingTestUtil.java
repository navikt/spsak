package no.nav.foreldrepenger.beregningsgrunnlag.adapter.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class BeregningInntektsmeldingTestUtil {

    private static final String DEFAULT_DOKUMENT_ID = "432422";
    private static final String DEFAULT_JOURNALTPOST_ID = "123123123";
    private MottatteDokumentRepository mottatteDokumentRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil;

    BeregningInntektsmeldingTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningInntektsmeldingTestUtil(BehandlingRepositoryProvider repositoryProvider, BeregningArbeidsgiverTestUtil beregningArbeidsgiverTestUtil) {
        inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
        this.beregningArbeidsgiverTestUtil = beregningArbeidsgiverTestUtil;
    }

    public void opprettInntektPåRevurdering(Behandling revurdering, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger, Integer refusjon, LocalDate opphørsdato) { // NOSONAR - brukes bare til test
        if (revurdering == null) {
            throw new IllegalStateException("Du må definere en revurdering før du kan opprette inntekter på den");
        }
        if (refusjon != null) {
            if (opphørsdato != null) {
                opprettInntektsmelding(revurdering, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, refusjon, opphørsdato);
            }
            opprettInntektsmelding(revurdering, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, refusjon, 10);
        } else {
            opprettInntektsmelding(revurdering, orgnr, arbId, skjæringstidspunktOpptjening, graderinger);
        }
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgNummer, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav, BigDecimal inntekt) {
        return opprettInntektsmelding(behandling, orgNummer, null, skjæringstidspunkt, Collections.emptyList(), refusjonskrav, inntekt, Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgNummer, String arbId, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav, BigDecimal inntekt) {
        return opprettInntektsmelding(behandling, orgNummer, arbId, skjæringstidspunkt, Collections.emptyList(), refusjonskrav, inntekt, Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgNummer, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav, BigDecimal inntekt, LocalDate refusjonOpphørerFom) {
        return opprettInntektsmelding(behandling, orgNummer, null, skjæringstidspunkt, Collections.emptyList(), refusjonskrav, inntekt, refusjonOpphørerFom, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmeldingMedGradering(Behandling behandling, String orgNummer, LocalDate skjæringstidspunkt, BigDecimal refusjonskrav, BigDecimal inntekt, Gradering... graderinger) {
        return opprettInntektsmelding(behandling, orgNummer, null, skjæringstidspunkt, Arrays.asList(graderinger), refusjonskrav, inntekt, Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, Integer refusjon) {
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, Collections.emptyList(), refusjon, 10);
    }


    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening) {
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, Collections.emptyList(), 0, 10);
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger) {
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, null, 10);
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger, Integer refusjon) { // NOSONAR - brukes bare til test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, refusjonEllerNull, BigDecimal.TEN, Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger, Integer refusjon, Integer inntekt) { // NOSONAR - brukes bare til test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, refusjonEllerNull, BigDecimal.valueOf(inntekt), Tid.TIDENES_ENDE, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger,  // NOSONAR - brukes bare til test
                                                  Integer refusjon, LocalDate opphørsdatoRefusjon) { // NOSONAR - brukes bare til test
        BigDecimal refusjonEllerNull = refusjon != null ? BigDecimal.valueOf(refusjon) : null;
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunktOpptjening, graderinger, refusjonEllerNull, BigDecimal.TEN, opphørsdatoRefusjon, Collections.emptyList(), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmeldingMedNaturalYtelser(Behandling behandling,  // NOSONAR - brukes bare til test
                                                                   String orgnr,
                                                                   LocalDate skjæringstidspunkt,
                                                                   BigDecimal inntektBeløp,
                                                                   BigDecimal refusjonskrav,
                                                                   LocalDate refusjonOpphørerDato,
                                                                   NaturalYtelse... naturalYtelser) {
        return opprettInntektsmelding(behandling, orgnr, null, skjæringstidspunkt, Collections.emptyList(), refusjonskrav, inntektBeløp, refusjonOpphørerDato, Arrays.asList(naturalYtelser), Collections.emptyList());
    }

    public Inntektsmelding opprettInntektsmeldingMedEndringerIRefusjon(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunkt, BigDecimal inntektBeløp, // NOSONAR - brukes bare til test
                                                                       BigDecimal refusjonskrav, LocalDate refusjonOpphørerDato, List<Refusjon> endringRefusjon) {
        return opprettInntektsmelding(behandling, orgnr, arbId, skjæringstidspunkt, Collections.emptyList(), refusjonskrav, inntektBeløp, refusjonOpphørerDato, Collections.emptyList(), endringRefusjon);
    }

    private Inntektsmelding opprettInntektsmelding(Behandling behandling, String orgnr, String arbId, LocalDate skjæringstidspunktOpptjening, List<Gradering> graderinger,  // NOSONAR - brukes bare til test
                                                   BigDecimal refusjon, BigDecimal inntekt, LocalDate opphørsdatoRefusjon, List<NaturalYtelse> naturalYtelser, List<Refusjon> endringRefusjon) {

        final MottattDokument mottattDokument = new MottattDokument.Builder().medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medMottattDato(LocalDate.now())
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId(DEFAULT_JOURNALTPOST_ID))
            .medDokumentId(DEFAULT_DOKUMENT_ID)
            .build();
        mottatteDokumentRepository.lagre(mottattDokument);

        InntektsmeldingBuilder inntektsmeldingBuilder = InntektsmeldingBuilder.builder();
        inntektsmeldingBuilder.medStartDatoPermisjon(skjæringstidspunktOpptjening);
        inntektsmeldingBuilder.medBeløp(inntekt);
        inntektsmeldingBuilder.medInnsendingstidspunkt(LocalDateTime.now());
        if (refusjon != null) {
            inntektsmeldingBuilder.medRefusjon(refusjon, opphørsdatoRefusjon);
        }
        inntektsmeldingBuilder.medMottattDokument(mottattDokument);
        Arbeidsgiver arbeidsgiver = beregningArbeidsgiverTestUtil.forArbeidsgiverVirksomhet(orgnr);
        inntektsmeldingBuilder.medVirksomhet(arbeidsgiver.getVirksomhet());
        inntektsmeldingBuilder.medArbeidsforholdId(arbId);
        naturalYtelser.forEach(inntektsmeldingBuilder::leggTil);
        graderinger.forEach(inntektsmeldingBuilder::leggTil);
        endringRefusjon.forEach(inntektsmeldingBuilder::leggTil);

        Inntektsmelding inntektsmelding = inntektsmeldingBuilder.build();
        inntektArbeidYtelseRepository.lagre(behandling, inntektsmelding);

        return inntektsmelding;
    }
}
