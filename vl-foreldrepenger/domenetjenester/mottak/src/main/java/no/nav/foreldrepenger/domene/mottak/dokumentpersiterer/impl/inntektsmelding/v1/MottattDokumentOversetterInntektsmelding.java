package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.inntektsmelding.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.ArbeidsgiverperiodeEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.GraderingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.NaturalYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.RefusjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.InntektsmeldingFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentOversetter;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.NamespaceRef;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.NaturalytelseKodeliste;
import no.nav.inntektsmelding.xml.kodeliste._2018xxyy.ÅrsakInnsendingKodeliste;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.konfig.Tid;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Arbeidsforhold;
import no.seres.xsd.nav.inntektsmelding_m._20180924.EndringIRefusjonsListe;
import no.seres.xsd.nav.inntektsmelding_m._20180924.GraderingIForeldrepenger;
import no.seres.xsd.nav.inntektsmelding_m._20180924.NaturalytelseDetaljer;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Periode;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Refusjon;
import no.seres.xsd.nav.inntektsmelding_m._20180924.SykepengerIArbeidsgiverperioden;

@NamespaceRef(InntektsmeldingConstants.NAMESPACE)
@ApplicationScoped
public class MottattDokumentOversetterInntektsmelding implements MottattDokumentOversetter<MottattDokumentWrapperInntektsmelding> {

    private static Map<ÅrsakInnsendingKodeliste, InntektsmeldingInnsendingsårsak> innsendingsårsakMap;

    static {
        innsendingsårsakMap = new EnumMap<>(ÅrsakInnsendingKodeliste.class);
        innsendingsårsakMap.put(ÅrsakInnsendingKodeliste.ENDRING, InntektsmeldingInnsendingsårsak.ENDRING);
        innsendingsårsakMap.put(ÅrsakInnsendingKodeliste.NY, InntektsmeldingInnsendingsårsak.NY);
    }

    private static final LocalDate TIDENES_BEGYNNELSE = LocalDate.of(1, Month.JANUARY, 1);
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private VirksomhetTjeneste virksomhetTjeneste;
    private KodeverkRepository kodeverkRepository;


    MottattDokumentOversetterInntektsmelding() {
        // for CDI proxy
    }

    @Inject
    public MottattDokumentOversetterInntektsmelding(BehandlingRepositoryProvider repositoryProvider, VirksomhetTjeneste virksomhetTjeneste) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.virksomhetTjeneste = virksomhetTjeneste;
    }

    @Override
    public void trekkUtDataOgPersister(MottattDokumentWrapperInntektsmelding wrapper, MottattDokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        String aarsakTilInnsending = wrapper.getSkjema().getSkjemainnhold().getAarsakTilInnsending();
        InntektsmeldingInnsendingsårsak innsendingsårsak = aarsakTilInnsending.isEmpty() ?
            InntektsmeldingInnsendingsårsak.UDEFINERT :
            innsendingsårsakMap.get(ÅrsakInnsendingKodeliste.fromValue(aarsakTilInnsending));

        InntektsmeldingBuilder builder = InntektsmeldingBuilder.builder();
        builder.medInnsendingstidspunkt(wrapper.getInnsendingstidspunkt());
        builder.medMottattDokument(mottattDokument);
        builder.medVirksomhet(virksomhetTjeneste.hentOgLagreOrganisasjon(
            wrapper.getArbeidsgiver().getVirksomhetsnummer()))
            .medNærRelasjon(wrapper.getErNærRelasjon());
        builder.medInntektsmeldingaarsak(innsendingsårsak);
        final Optional<Arbeidsforhold> arbeidsforhold = wrapper.getArbeidsforhold();
        if (arbeidsforhold.isPresent()) {
            final Arbeidsforhold arbeidsforholdet = arbeidsforhold.get();
            final JAXBElement<String> arbeidsforholdId = arbeidsforholdet.getArbeidsforholdId();
            if (arbeidsforholdId != null) {
                builder.medArbeidsforholdId(arbeidsforholdId.getValue());
            }
            builder.medBeløp(arbeidsforholdet.getBeregnetInntekt().getValue().getBeloep().getValue());
        } else {
            throw InntektsmeldingFeil.FACTORY.manglendeInformasjon().toException();
        }

        mapNaturalYtelser(wrapper, builder);
        //mapGradering(wrapper, builder);

        mapArbeidsgiverperiode(wrapper, builder);
        if (wrapper.getArbeidsgiverPeriode() != null && !wrapper.getArbeidsgiverPeriode().isEmpty()) {
            /*
            Startdato for arbeidsgiverperiode kan ikke være fram i tid
            For hver periode må fom dato være lik eller lavere enn tom dato
            Hvis flere perioder med arbeidsgiverperiode. Så skal siste dag i første perioden være før første dag i neste periode
            Ok om perioden enten er 12 eller 16 dager
            Hvis flere perioder med arbeidsgiverperiode så skal det ikke være lovlig å oppgi 16 kalenderdager eller mer mellom etterfølgende perioder.
             */
            // TODO: HACK ? Fyller ut startdato-permisjon med første dag i arbeidsgiverperioden
            builder.medStartDatoPermisjon(DateUtil.convertToLocalDate(wrapper.getArbeidsgiverPeriode().get(0).getFom().getValue()));
        }
        final Optional<SykepengerIArbeidsgiverperioden> sykepengerIArbeidsgiverperioden = wrapper.getSykepengerIArbeidsgiverperioden();
        if (sykepengerIArbeidsgiverperioden.isPresent()) {
            final SykepengerIArbeidsgiverperioden siagp = sykepengerIArbeidsgiverperioden.get();
            final JAXBElement<BigDecimal> bruttoUtbetalt = siagp.getBruttoUtbetalt();
            if (bruttoUtbetalt != null) {
                builder.medArbeidsgiverperiodeBruttoUtbetalt(bruttoUtbetalt.getValue());
            }
        } else {
            throw InntektsmeldingFeil.FACTORY.manglendeInformasjon().toException(); // TODO: Annen feilkode/tekst ?
        }
        /*
        bruttoUtbetalt:
        Må være utfylt
        Kan ikke være negativt tall
        Må være kroner 0 ved begrunnelse 'Ikke sykmeldt'
        BruttoUtbetalt > 0 og kode 'IkkeSykemeldt' oppgis vil gi feil
         */

        mapRefusjon(wrapper, builder);

        inntektArbeidYtelseRepository.lagre(behandling, builder.build());
    }

    private void mapRefusjon(MottattDokumentWrapperInntektsmelding wrapper, InntektsmeldingBuilder builder) {
        final Optional<Refusjon> optionalRefusjon = wrapper.getRefusjon();
        if (optionalRefusjon.isPresent()) {
            final Refusjon refusjon = optionalRefusjon.get();
            if (refusjon.getRefusjonsopphoersdato() != null) {
                builder.medRefusjon(refusjon.getRefusjonsbeloepPrMnd().getValue(),
                    DateUtil.convertToLocalDate(refusjon.getRefusjonsopphoersdato().getValue()));
            } else if (refusjon.getRefusjonsbeloepPrMnd() != null) {
                builder.medRefusjon(refusjon.getRefusjonsbeloepPrMnd().getValue());
            }

            //Map endring i refusjon
            Optional.ofNullable(refusjon.getEndringIRefusjonListe())
                .map(JAXBElement::getValue)
                .map(EndringIRefusjonsListe::getEndringIRefusjon)
                .orElse(Collections.emptyList())
                .stream()
                .forEach(eir -> builder.leggTil(new RefusjonEntitet(eir.getRefusjonsbeloepPrMnd().getValue(), DateUtil.convertToLocalDate(eir.getEndringsdato().getValue()))));

        }
    }

    private void mapArbeidsgiverperiode(MottattDokumentWrapperInntektsmelding wrapper, InntektsmeldingBuilder builder) {
        for (Periode periode : wrapper.getArbeidsgiverPeriode()) {
            builder.leggTil(new ArbeidsgiverperiodeEntitet(DateUtil.convertToLocalDate(periode.getFom().getValue()),
                DateUtil.convertToLocalDate(periode.getTom().getValue())));
        }
    }

    private void mapNaturalYtelser(MottattDokumentWrapperInntektsmelding wrapper, InntektsmeldingBuilder builder) {
        // Ved gjenopptakelse gjelder samme beløp
        Map<NaturalYtelseType, BigDecimal> beløp = new HashMap<>();
        for (NaturalytelseDetaljer detaljer : wrapper.getOpphørelseAvNaturalytelse()) {
            NaturalytelseKodeliste naturalytelse = NaturalytelseKodeliste.fromValue(detaljer.getNaturalytelseType().getValue());
            final NaturalYtelseType ytelseType = kodeverkRepository.finnForKodeverkEiersKode(NaturalYtelseType.class, naturalytelse.value());
            beløp.put(ytelseType, detaljer.getBeloepPrMnd().getValue());
            builder.leggTil(new NaturalYtelseEntitet(TIDENES_BEGYNNELSE, DateUtil.convertToLocalDate(detaljer.getFom().getValue()),
                beløp.get(ytelseType), ytelseType));
        }

        for (NaturalytelseDetaljer detaljer : wrapper.getGjenopptakelserAvNaturalytelse()) {
            NaturalytelseKodeliste naturalytelse = NaturalytelseKodeliste.fromValue(detaljer.getNaturalytelseType().getValue());
            final NaturalYtelseType ytelseType = kodeverkRepository.finnForKodeverkEiersKode(NaturalYtelseType.class, naturalytelse.value());
            builder.leggTil(new NaturalYtelseEntitet(DateUtil.convertToLocalDate(detaljer.getFom().getValue()), Tid.TIDENES_ENDE,
                beløp.get(ytelseType), ytelseType));
        }
    }

    /*private void mapGradering(MottattDokumentWrapperInntektsmelding wrapper, InntektsmeldingBuilder builder) {
        for (GraderingIForeldrepenger detaljer : wrapper.getGradering()) {
            builder.leggTil(new GraderingEntitet(DateUtil.convertToLocalDate(detaljer.getPeriode().getValue().getFom().getValue()),
                DateUtil.convertToLocalDate(detaljer.getPeriode().getValue().getTom().getValue()),
                new BigDecimal(detaljer.getArbeidstidprosent().getValue())));
        }
    }*/
}
