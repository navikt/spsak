package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType.NÆRING;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType.UTDANNINGSPERMISJON;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningsUtils.hentUtDatoIntervall;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningsUtils.lagOpptjeningsnøkkel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningAktivitetVurdering;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class OpptjeningsperioderTjenesteImpl implements OpptjeningsperioderTjeneste {

    private static final OpptjeningAktivitetType UDEFINERT = OpptjeningAktivitetType.UDEFINERT;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private KodeverkRepository kodeverkRepository;
    private OpptjeningRepository opptjeningRepository;
    private OpptjeningAktivitetVurdering vurderForSaksbehandling;
    private OpptjeningAktivitetVurdering vurderForVilkår;
    private BehandlingRepository behandlingRepository;

    OpptjeningsperioderTjenesteImpl() {
        //CDI
    }

    @Inject
    public OpptjeningsperioderTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                           GrunnlagRepositoryProvider provider, ResultatRepositoryProvider resultatRepositoryProvider,
                                           AksjonspunktutlederForVurderOpptjening vurderOpptjening) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.kodeverkRepository = provider.getKodeverkRepository();
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.behandlingRepository = resultatRepositoryProvider.getBehandlingRepository();
        this.vurderForSaksbehandling = new OpptjeningAktivitetVurderingAksjonspunkt(vurderOpptjening);
        this.vurderForVilkår = new OpptjeningAktivitetVurderingVilkår(vurderOpptjening);
    }

    @Override
    public List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForSaksbehandling(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            return mapOpptjeningsperiodeForSaksbehandling(behandling, grunnlagOpt.get(), vurderForSaksbehandling);
        }
        return Collections.emptyList();
    }

    @Override
    public List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForVilkårVurdering(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (grunnlagOpt.isPresent()) {
            return mapOpptjeningsperiodeForSaksbehandling(behandling, grunnlagOpt.get(), vurderForVilkår);
        }
        return Collections.emptyList();
    }

    @Override
    public List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForSaksbehandling(Behandling behandling, Long inntektArbeidYtelseGrunnlagId) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatPåIdHvisEksisterer(inntektArbeidYtelseGrunnlagId);
        if (grunnlagOpt.isPresent()) {
            return mapOpptjeningsperiodeForSaksbehandling(behandling, grunnlagOpt.get(), vurderForSaksbehandling);
        }
        return Collections.emptyList();
    }

    private List<OpptjeningsperiodeForSaksbehandling> mapOpptjeningsperiodeForSaksbehandling(Behandling behandling, InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        return mapPerioderForSaksbehandling(behandling, inntektArbeidYtelseGrunnlag, vurderForSaksbehandling);
    }

    private List<OpptjeningsperiodeForSaksbehandling> mapPerioderForSaksbehandling(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag, OpptjeningAktivitetVurdering vurderOpptjening) {
        List<OpptjeningsperiodeForSaksbehandling> perioder = new ArrayList<>();
        final Optional<InntektArbeidYtelseAggregat> registerFørVersjon = grunnlag.getOpplysningerFørSkjæringstidspunkt();
        if (registerFørVersjon.isPresent()) {
            // map yrkesaktivitet
            final Optional<AktørArbeid> søkerOpt = registerFørVersjon.get().getAktørArbeid().stream().filter(it -> it.getAktørId().equals(behandling.getAktørId())).findFirst();
            if (søkerOpt.isPresent()) {
                final AktørArbeid arbeid = søkerOpt.get();
                for (Yrkesaktivitet yrkesaktivitet : arbeid.getYrkesaktiviteter()) {
                    mapYrkesaktivitet(behandling, perioder, yrkesaktivitet, grunnlag, vurderOpptjening);
                }
            }
        }
        final Optional<OppgittOpptjening> oppgittOpptjening = grunnlag.getOppgittOpptjening();
        if (oppgittOpptjening.isPresent()) {
            // map
            final OppgittOpptjening opptjening = oppgittOpptjening.get();
            for (Map.Entry<ArbeidType, List<AnnenAktivitet>> annenAktivitet : opptjening.getAnnenAktivitet().stream().collect(Collectors.groupingBy(AnnenAktivitet::getArbeidType)).entrySet()) {
                mapAnnenAktivitet(perioder, annenAktivitet, grunnlag, behandling, vurderOpptjening);
            }
            opptjening.getOppgittArbeidsforhold() //.filter(utenlandskArbforhold -> utenlandskArbforhold.getArbeidType().equals(ArbeidType.UDEFINERT))
                .forEach(oppgittArbeidsforhold -> perioder.add(mapOppgittArbeidsforhold(oppgittArbeidsforhold, grunnlag, behandling, vurderOpptjening)));

            opptjening.getEgenNæring().forEach(egenNæring -> {
                OpptjeningsperiodeForSaksbehandling periode = mapEgenNæring(egenNæring, grunnlag, behandling, vurderOpptjening);
                perioder.add(periode);
            });
        }
        Optional<AktørYtelse> aktørYtelse = grunnlag.getAktørYtelseFørStp(behandling.getAktørId());
        List<OpptjeningsperiodeForSaksbehandling> ytelsePerioder = new ArrayList<>();
        aktørYtelse.ifPresent(aktørYtelse1 -> aktørYtelse1.getYtelser().stream()
            .filter(ytelse -> !FagsystemUnderkategori.INFOTRYGD_SAK.equals(ytelse.getFagsystemUnderkategori()))
            .filter(ytelse -> !(ytelse.getKilde().equals(Fagsystem.FPSAK) && ytelse.getSaksnummer().equals(behandling.getFagsak().getSaksnummer())))
            .forEach(behandlingRelaterteYtelse -> {
                List<OpptjeningsperiodeForSaksbehandling> periode = mapYtelseAnvist(behandlingRelaterteYtelse, behandling, vurderOpptjening);
                ytelsePerioder.addAll(periode);
            }));
        perioder.addAll(slåSammenYtelsePerioder(ytelsePerioder));
        final Optional<InntektArbeidYtelseAggregat> saksbehandletVersjon = grunnlag.getSaksbehandletVersjon();
        if (saksbehandletVersjon.isPresent()) {
            håndterManueltLagtTilAktiviteter(behandling, grunnlag, vurderOpptjening, perioder, saksbehandletVersjon.get());
        }
        lagOpptjeningsperiodeForFrilansAktivitet(behandling, oppgittOpptjening.orElse(null), grunnlag, perioder).ifPresent(perioder::add);

        return perioder.stream().sorted(Comparator.comparing(OpptjeningsperiodeForSaksbehandling::getPeriode)).collect(Collectors.toList());
    }

    private void håndterManueltLagtTilAktiviteter(Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag, OpptjeningAktivitetVurdering vurderOpptjening, List<OpptjeningsperiodeForSaksbehandling> perioder, InntektArbeidYtelseAggregat saksbehandletVersjon) {
        final Optional<AktørArbeid> aktørArbeid = saksbehandletVersjon.getAktørArbeid().stream().filter(aa -> aa.getAktørId().equals(behandling.getAktørId())).findFirst();
        aktørArbeid.ifPresent(aktørArbeid1 -> aktørArbeid1.getYrkesaktiviteter()
            .stream()
            .filter(Yrkesaktivitet::erArbeidsforhold)
            .forEach(yr -> yr.getAnsettelsesPeriode().ifPresent(avtale -> {
                if (perioder.stream()
                    .noneMatch(p -> p.getOpptjeningAktivitetType().equals(utledOpptjeningType(yr.getArbeidType())) && p.getPeriode().equals(avtale.getPeriode()))) {
                    leggTilManuelleAktiviteter(yr, avtale, perioder, behandling, grunnlag, vurderOpptjening);
                }
            })));
        aktørArbeid.ifPresent(aktørArbeid1 -> aktørArbeid1.getYrkesaktiviteter()
            .stream()
            .filter(yr -> !yr.erArbeidsforhold())
            .forEach(yr -> yr.getAktivitetsAvtaler().stream().filter(av ->
                perioder.stream()
                    .noneMatch(p -> p.getOpptjeningAktivitetType().equals(utledOpptjeningType(yr.getArbeidType())) &&
                        p.getPeriode().equals(av.getPeriode())))
                .forEach(avtale -> leggTilManuelleAktiviteter(yr, avtale, perioder, behandling, grunnlag, vurderOpptjening))));
    }

    private void leggTilManuelleAktiviteter(Yrkesaktivitet yr, AktivitetsAvtale avtale, List<OpptjeningsperiodeForSaksbehandling> perioder,
                                            Behandling behandling, InntektArbeidYtelseGrunnlag grunnlag, OpptjeningAktivitetVurdering vurderOpptjening) {
        final OpptjeningAktivitetType type = utledOpptjeningType(yr.getArbeidType());
        OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny();
        builder.medPeriode(avtale.getPeriode())
            .medOpptjeningAktivitetType(type)
            .medBegrunnelse(avtale.getBeskrivelse())
            .medVurderingsStatus(vurderOpptjening.vurderStatus(type, behandling, yr, grunnlag.harBlittSaksbehandlet()));
        harSaksbehandlerVurdert(builder, type, behandling, null, grunnlag.harBlittSaksbehandlet(), vurderOpptjening);
        builder.medErManueltRegistrert();
        perioder.add(builder.build());
    }

    private OpptjeningsperiodeForSaksbehandling mapOppgittArbeidsforhold(OppgittArbeidsforhold oppgittArbeidsforhold, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling, OpptjeningAktivitetVurdering vurderOpptjening) {
        final OpptjeningAktivitetType type = utledOpptjeningType(oppgittArbeidsforhold.getArbeidType());
        final Yrkesaktivitet overstyrt = finnTilsvarende(behandling.getAktørId(), grunnlag.getSaksbehandletVersjon().orElse(null),
            oppgittArbeidsforhold.getArbeidType(), oppgittArbeidsforhold.getPeriode()).orElse(null);
        final OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny();
        DatoIntervallEntitet periode = utledPeriode(oppgittArbeidsforhold.getPeriode(), overstyrt);
        builder.medOpptjeningAktivitetType(type)
            .medPeriode(periode)
            .medArbeidsgiverNavn(oppgittArbeidsforhold.getUtenlandskVirksomhet().getUtenlandskVirksomhetNavn())
            .medVurderingsStatus(vurderOpptjening.vurderStatus(type, behandling, overstyrt, grunnlag.harBlittSaksbehandlet()));

        if (harEndretPåPeriode(oppgittArbeidsforhold.getPeriode(), overstyrt)) {
            builder.medErPeriodenEndret();
        }

        if (overstyrt != null) {
            overstyrt.getAktivitetsAvtaler()
                .stream()
                .filter(aa -> aa.getPeriode().equals(periode))
                .findFirst()
                .ifPresent(aa -> builder.medBegrunnelse(aa.getBeskrivelse()));
        }
        return builder.build();
    }

    private Optional<Yrkesaktivitet> finnTilsvarende(AktørId aktørId, InntektArbeidYtelseAggregat aggregat, Yrkesaktivitet registerAktivitet) {
        if (aggregat == null) {
            return Optional.empty();
        }
        final Optional<AktørArbeid> aktørArbeid = aggregat.getAktørArbeid().stream().filter(it -> it.getAktørId().equals(aktørId)).findFirst();
        if (!aktørArbeid.isPresent()) {
            return Optional.empty();
        }
        final AktørArbeid arbeid = aktørArbeid.get();

        return arbeid.getYrkesaktiviteter().stream().filter(yr -> matcher(yr, registerAktivitet)).findFirst();
    }

    private Optional<Yrkesaktivitet> finnTilsvarende(AktørId aktørId, InntektArbeidYtelseAggregat aggregat, ArbeidType type, DatoIntervallEntitet periode) {
        if (aggregat == null) {
            return Optional.empty();
        }
        final Optional<AktørArbeid> aktørArbeid = aggregat.getAktørArbeid().stream().filter(it -> it.getAktørId().equals(aktørId)).findFirst();
        if (!aktørArbeid.isPresent()) {
            return Optional.empty();
        }
        final AktørArbeid arbeid = aktørArbeid.get();

        return arbeid.getYrkesaktiviteter().stream().filter(yr -> matcher(yr, type) && inneholderPeriode(yr, periode)).findFirst();
    }

    private boolean inneholderPeriode(Yrkesaktivitet yr, DatoIntervallEntitet periode) {
        return yr.getAktivitetsAvtaler().stream().anyMatch(aa -> aa.getPeriode().overlapper(periode));
    }

    private boolean matcher(Yrkesaktivitet yr, Yrkesaktivitet registerAktivitet) {
        if (!yr.getArbeidType().equals(registerAktivitet.getArbeidType())) {
            return false;
        }
        if (!Objects.equals(yr.getArbeidsgiver(), registerAktivitet.getArbeidsgiver())) {
            return false;
        }
        return yr.getArbeidsforholdRef()
            .orElse(ArbeidsforholdRef.ref(null))
            .gjelderFor(registerAktivitet.getArbeidsforholdRef()
                .orElse(ArbeidsforholdRef.ref(null)));
    }

    private boolean matcher(Yrkesaktivitet yr, ArbeidType type) {
        return yr.getArbeidType().equals(type);
    }

    private void mapYrkesaktivitet(Behandling behandling, List<OpptjeningsperiodeForSaksbehandling> perioder, Yrkesaktivitet registerAktivitet,
                                   InntektArbeidYtelseGrunnlag grunnlag, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        final OpptjeningAktivitetType type = utledOpptjeningType(registerAktivitet.getArbeidType());
        final InntektArbeidYtelseAggregat saksbehandlet = grunnlag.getSaksbehandletVersjon().orElse(null);
        final Yrkesaktivitet overstyrtAktivitet = finnTilsvarende(behandling.getAktørId(), saksbehandlet, registerAktivitet).orElse(null);

        for (AktivitetsAvtale avtale : gjeldendeAvtaler(registerAktivitet, overstyrtAktivitet)) {
            OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny()
                .medOpptjeningAktivitetType(type)
                .medPeriode(avtale.getPeriode())
                .medBegrunnelse(avtale.getBeskrivelse())
                .medStillingsandel(finnStillingsprosent(registerAktivitet));
            harSaksbehandlerVurdert(builder, type, behandling, registerAktivitet, grunnlag.harBlittSaksbehandlet(), vurderForSaksbehandling);
            settArbeidsgiverInformasjon(gjeldendeAktivitet(registerAktivitet, overstyrtAktivitet), builder);
            builder.medVurderingsStatus(vurderForSaksbehandling.vurderStatus(type, behandling, registerAktivitet, overstyrtAktivitet, grunnlag.harBlittSaksbehandlet()));
            if (harEndretPåPeriode(avtale.getPeriode(), overstyrtAktivitet)) {
                builder.medErPeriodenEndret();
            }
            perioder.add(builder.build());
        }
        for (Permisjon permisjon : registerAktivitet.getPermisjon()) {
            if (PermisjonsbeskrivelseType.UTDANNINGSPERMISJON.equals(permisjon.getPermisjonsbeskrivelseType())) {
                OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny()
                    .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(permisjon.getFraOgMed(), permisjon.getTilOgMed()))
                    .medOpptjeningAktivitetType(UTDANNINGSPERMISJON)
                    .medStillingsandel(permisjon.getProsentsats());
                if (registerAktivitet.getArbeidsgiver() != null) {
                    builder.medOrgnr(registerAktivitet.getArbeidsgiver().getIdentifikator());
                    builder.medOpptjeningsnøkkel(lagOpptjeningsnøkkel(registerAktivitet.getArbeidsgiver(), registerAktivitet.getArbeidsforholdRef().orElse(null)));
                    builder.medVurderingsStatus(vurderForSaksbehandling.vurderStatus(UTDANNINGSPERMISJON, behandling, registerAktivitet, overstyrtAktivitet, grunnlag.harBlittSaksbehandlet()));
                }
                perioder.add(builder.build());
            }
        }
    }

    private Stillingsprosent finnStillingsprosent(Yrkesaktivitet registerAktivitet) {
        final Stillingsprosent defaultStillingsprosent = new Stillingsprosent(0);
        if (registerAktivitet.erArbeidsforhold()) {
            return registerAktivitet.getAktivitetsAvtaler()
                .stream()
                .map(AktivitetsAvtale::getProsentsats)
                .max(Comparator.comparing(Stillingsprosent::getVerdi))
                .orElse(defaultStillingsprosent);
        }
        return defaultStillingsprosent;
    }

    private Collection<AktivitetsAvtale> gjeldendeAvtaler(Yrkesaktivitet registerAktivitet, Yrkesaktivitet overstyrtAktivitet) {
        if (registerAktivitet.erArbeidsforhold()) {
            return Stream.of(gjeldendeAktivitet(registerAktivitet, overstyrtAktivitet).getAnsettelsesPeriode()
                .orElse(registerAktivitet.getAnsettelsesPeriode()
                    .orElse(null)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        return gjeldendeAktivitet(registerAktivitet, overstyrtAktivitet).getAktivitetsAvtaler();
    }

    private Yrkesaktivitet gjeldendeAktivitet(Yrkesaktivitet registerAktivitet, Yrkesaktivitet overstyrtAktivitet) {
        return overstyrtAktivitet == null ? registerAktivitet : overstyrtAktivitet;
    }

    private void harSaksbehandlerVurdert(OpptjeningsperiodeForSaksbehandling.Builder builder, OpptjeningAktivitetType type,
                                         Behandling behandling, Yrkesaktivitet registerAktivitet,
                                         boolean harBlittSaksbehandlet, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        if (vurderForSaksbehandling.vurderStatus(type, behandling, registerAktivitet, null, harBlittSaksbehandlet).equals(VurderingsStatus.TIL_VURDERING)) {
            builder.medErManueltBehandlet();
        }
    }

    @Override
    public Optional<Opptjening> hentOpptjeningHvisFinnes(Behandling behandling) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandlingsresultat.isEmpty()) {
            return Optional.empty();
        }
        return opptjeningRepository.finnOpptjening(behandlingsresultat.get());
    }

    private void mapAnnenAktivitet(List<OpptjeningsperiodeForSaksbehandling> perioder, Map.Entry<ArbeidType, List<AnnenAktivitet>> annenAktivitet,
                                   InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        final InntektArbeidYtelseAggregat saksbehandlet = grunnlag.getSaksbehandletVersjon().orElse(null);
        OpptjeningAktivitetType opptjeningAktivitetType = utledOpptjeningType(annenAktivitet.getKey());
        for (AnnenAktivitet aktivitet : annenAktivitet.getValue()) {
            final Yrkesaktivitet overstyrtAktivitet = finnTilsvarende(behandling.getAktørId(), saksbehandlet, aktivitet.getArbeidType(), aktivitet.getPeriode()).orElse(null);
            OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny();
            builder.medPeriode(utledPeriode(aktivitet.getPeriode(), overstyrtAktivitet))
                .medOpptjeningAktivitetType(opptjeningAktivitetType)
                .medVurderingsStatus(vurderForSaksbehandling.vurderStatus(opptjeningAktivitetType, behandling, overstyrtAktivitet, grunnlag.harBlittSaksbehandlet()));
            if (overstyrtAktivitet != null) {
                final Optional<AktivitetsAvtale> aktivitetsAvtale = utledAktivitetAvtale(aktivitet.getPeriode(), overstyrtAktivitet);
                aktivitetsAvtale.ifPresent(aktivitetsAvtale1 -> builder.medBegrunnelse(aktivitetsAvtale1.getBeskrivelse()));
                builder.medErManueltBehandlet();
            }
            if (harEndretPåPeriode(aktivitet.getPeriode(), overstyrtAktivitet)) {
                builder.medErPeriodenEndret();
            }
            perioder.add(builder.build());
        }
    }

    private DatoIntervallEntitet utledPeriode(DatoIntervallEntitet periode, Yrkesaktivitet overstyrtAktivitet) {
        if (overstyrtAktivitet == null) {
            return periode;
        }
        return utledAktivitetAvtale(periode, overstyrtAktivitet).map(AktivitetsAvtale::getPeriode).orElse(periode);
    }

    private Optional<AktivitetsAvtale> utledAktivitetAvtale(DatoIntervallEntitet periode, Yrkesaktivitet overstyrtAktivitet) {
        return overstyrtAktivitet.getAktivitetsAvtaler()
            .stream()
            .filter(it -> it.getPeriode().overlapper(periode))
            .findFirst();
    }

    private OpptjeningAktivitetType utledOpptjeningType(ArbeidType arbeidType) {
        return kodeverkRepository.hentKodeRelasjonForKodeverk(ArbeidType.class, OpptjeningAktivitetType.class).get(arbeidType)
            .stream()
            .findFirst()
            .orElse(OpptjeningAktivitetType.UDEFINERT);
    }

    private OpptjeningsperiodeForSaksbehandling mapEgenNæring(EgenNæring egenNæring, InntektArbeidYtelseGrunnlag grunnlag, Behandling behandling, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        final OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny()
            .medOpptjeningAktivitetType(NÆRING);
        final Yrkesaktivitet overstyrt = finnTilsvarende(behandling.getAktørId(), grunnlag.getSaksbehandletVersjon().orElse(null),
            ArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE, egenNæring.getPeriode()).orElse(null);
        builder.medPeriode(utledPeriode(egenNæring.getPeriode(), overstyrt));
        if (egenNæring.getVirksomhet() != null) {
            builder.medOpptjeningsnøkkel(lagOpptjeningsnøkkel(Arbeidsgiver.virksomhet(egenNæring.getVirksomhet()), null))
                .medOrgnr(egenNæring.getVirksomhet().getOrgnr());
        }
        builder.medVurderingsStatus(vurderForSaksbehandling.vurderStatus(NÆRING, behandling, overstyrt, grunnlag.harBlittSaksbehandlet()));
        if (grunnlag.harBlittSaksbehandlet()) {
            builder.medErManueltBehandlet();
        }
        builder.medStillingsandel(new Stillingsprosent(BigDecimal.valueOf(100)));
        return builder.build();
    }

    private List<OpptjeningsperiodeForSaksbehandling> mapYtelseAnvist(Ytelse ytelse, Behandling behandling, OpptjeningAktivitetVurdering vurderForSaksbehandling) {
        OpptjeningAktivitetType type = mapYtelseType(ytelse);
        List<OpptjeningsperiodeForSaksbehandling> ytelserAnvist = new ArrayList<>();
        Optional<String> orgnummer = ytelse.getYtelseGrunnlag()
            .map(YtelseGrunnlag::getYtelseStørrelse)
            .flatMap(list -> list.stream().filter(ys -> ys.getVirksomhet().isPresent()).findFirst())
            .flatMap(YtelseStørrelse::getVirksomhet)
            .map(Virksomhet::getOrgnr);

        ytelse.getYtelseAnvist().forEach(ytelseAnvist -> {
            OpptjeningsperiodeForSaksbehandling.Builder builder = OpptjeningsperiodeForSaksbehandling.Builder.ny()
                .medPeriode(hentUtDatoIntervall(ytelse, ytelseAnvist))
                .medOpptjeningAktivitetType(type)
                .medVurderingsStatus(vurderForSaksbehandling.vurderStatus(type, behandling, null, false));
            orgnummer.ifPresent(orgnr -> builder.medOrgnr(orgnr).medOpptjeningsnøkkel(Opptjeningsnøkkel.forOrgnummer(orgnr)));
            ytelserAnvist.add(builder.build());
        });
        return ytelserAnvist;
    }

    private List<OpptjeningsperiodeForSaksbehandling> slåSammenYtelsePerioder(List<OpptjeningsperiodeForSaksbehandling> ytelser) {
        List<OpptjeningsperiodeForSaksbehandling> resultat = new ArrayList<>();
        if (ytelser.isEmpty()) {
            return resultat;
        }
        Map<OpptjeningAktivitetType, List<OpptjeningsperiodeForSaksbehandling>> sortering = ytelser.stream()
            .collect(Collectors.groupingBy(OpptjeningsperiodeForSaksbehandling::getOpptjeningAktivitetType));
        sortering.entrySet().forEach(entry -> {
            resultat.addAll(slåSammenYtelsePerioderSammeType(entry.getValue()));
        });
        return resultat;
    }

    private List<OpptjeningsperiodeForSaksbehandling> slåSammenYtelsePerioderSammeType(List<OpptjeningsperiodeForSaksbehandling> ytelser) {
        if (ytelser.size() < 2) {
            return ytelser;
        }
        List<OpptjeningsperiodeForSaksbehandling> sorterFom = ytelser.stream()
            .sorted(Comparator.comparing(opfs -> opfs.getPeriode().getFomDato()))
            .collect(Collectors.toList());
        List<OpptjeningsperiodeForSaksbehandling> fusjonert = new ArrayList<>();

        Iterator<OpptjeningsperiodeForSaksbehandling> iterator = sorterFom.iterator();
        OpptjeningsperiodeForSaksbehandling prev = iterator.next();
        OpptjeningsperiodeForSaksbehandling next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (OpptjeningsUtils.erTilgrensende(prev.getPeriode(), next.getPeriode())) {
                prev = slåSammenToPerioder(prev, next);
            } else {
                fusjonert.add(prev);
                prev = next;
            }
        }
        fusjonert.add(prev);
        return fusjonert;
    }

    private OpptjeningsperiodeForSaksbehandling slåSammenToPerioder(OpptjeningsperiodeForSaksbehandling opp1, OpptjeningsperiodeForSaksbehandling opp2) {
        return OpptjeningsperiodeForSaksbehandling.Builder.ny()
            .medPeriode(OpptjeningsUtils.slåSammenOverlappendeDatoIntervall(opp1.getPeriode(), opp2.getPeriode()))
            .medOpptjeningAktivitetType(opp1.getOpptjeningAktivitetType())
            .medVurderingsStatus(opp1.getVurderingsStatus())
            .medOrgnr(opp1.getOrgnr())
            .medOpptjeningsnøkkel(opp1.getOpptjeningsnøkkel())
            .build();

    }

    private OpptjeningAktivitetType mapYtelseType(Ytelse ytelse) {
        if (RelatertYtelseType.PÅRØRENDESYKDOM.equals(ytelse.getRelatertYtelseType())) {
            return kodeverkRepository.hentKodeRelasjonForKodeverk(TemaUnderkategori.class, OpptjeningAktivitetType.class)
                .getOrDefault(ytelse.getBehandlingsTema(), Collections.singleton(UDEFINERT)).stream().findFirst().orElse(UDEFINERT);
        }
        return kodeverkRepository.hentKodeRelasjonForKodeverk(RelatertYtelseType.class, OpptjeningAktivitetType.class).getOrDefault(ytelse.getRelatertYtelseType(), Collections.singleton(UDEFINERT)).stream().findFirst().orElse(UDEFINERT);
    }

    private void settArbeidsgiverInformasjon(Yrkesaktivitet yrkesaktivitet, OpptjeningsperiodeForSaksbehandling.Builder builder) {
        if (yrkesaktivitet.getArbeidsgiver() != null) {
            builder.medOrgnr(yrkesaktivitet.getArbeidsgiver().getIdentifikator());
            builder.medOpptjeningsnøkkel(lagOpptjeningsnøkkel(yrkesaktivitet.getArbeidsgiver(), yrkesaktivitet.getArbeidsforholdRef().orElse(null)));
        }
        if (yrkesaktivitet.getNavnArbeidsgiverUtland() != null) {
            builder.medArbeidsgiverNavn(yrkesaktivitet.getNavnArbeidsgiverUtland());
        }
    }

    private boolean harEndretPåPeriode(DatoIntervallEntitet periode, Yrkesaktivitet overstyrtAktivitet) {
        if (overstyrtAktivitet == null) {
            return false;
        }

        return overstyrtAktivitet.getAktivitetsAvtaler().stream().map(AktivitetsAvtale::getPeriode).noneMatch(p -> p.equals(periode));
    }

    private Optional<OpptjeningsperiodeForSaksbehandling> lagOpptjeningsperiodeForFrilansAktivitet(Behandling behandling, OppgittOpptjening oppgittOpptjening,
                                                                                                   InntektArbeidYtelseGrunnlag grunnlag,
                                                                                                   List<OpptjeningsperiodeForSaksbehandling> perioder) {
        // Hvis oppgitt frilansaktivitet brukes perioden derfra og det er allerede laget en OFS.
        if (oppgittOpptjening != null && oppgittOpptjening.getAnnenAktivitet().stream().anyMatch(oaa -> ArbeidType.FRILANSER.equals(oaa.getArbeidType())) ||
            perioder.stream().anyMatch(oaa -> OpptjeningAktivitetType.FRILANS.equals(oaa.getOpptjeningAktivitetType()))) {
            return Optional.empty();
        }
        Optional<Opptjening> opptjeningOptional = hentOpptjeningHvisFinnes(behandling);
        if (opptjeningOptional.isEmpty()) {
            return Optional.empty();
        }
        Optional<AktørArbeid> aktørArbeid = grunnlag.getAktørArbeidFørStp(behandling.getAktørId());
        if (aktørArbeid.isPresent() && !aktørArbeid.get().getFrilansOppdrag().isEmpty()) {
            return Optional.of(OpptjeningsperiodeForSaksbehandling.Builder.ny()
                .medOpptjeningAktivitetType(utledOpptjeningType(ArbeidType.FRILANSER))
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(opptjeningOptional.get().getFom(), opptjeningOptional.get().getTom()))
                .medVurderingsStatus(VurderingsStatus.TIL_VURDERING)
                .build());
        }
        return Optional.empty();
    }
}
