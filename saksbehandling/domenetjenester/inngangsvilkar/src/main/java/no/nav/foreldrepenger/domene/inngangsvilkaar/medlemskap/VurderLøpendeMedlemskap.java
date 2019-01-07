package no.nav.foreldrepenger.domene.inngangsvilkaar.medlemskap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertLøpendeMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.inngangsvilkaar.PersonStatusType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.VilkårData;
import no.nav.foreldrepenger.domene.inngangsvilkaar.impl.InngangsvilkårOversetter;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.fpsak.nare.evaluation.Evaluation;

@ApplicationScoped
public class VurderLøpendeMedlemskap {

    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private MedlemskapRepository medlemskapRepository;
    private InngangsvilkårOversetter inngangsvilkårOversetter;
    private BehandlingRepository behandlingRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    VurderLøpendeMedlemskap() {
        //CDI
    }

    @Inject
    public VurderLøpendeMedlemskap(BasisPersonopplysningTjeneste personopplysningTjeneste, GrunnlagRepositoryProvider repositoryProvider,
                                   InngangsvilkårOversetter inngangsvilkårOversetter) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.inngangsvilkårOversetter = inngangsvilkårOversetter;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    }

    public Map<LocalDate, VilkårData> vurderLøpendeMedlemskap(Long behandlingId) {
        return lagGrunnlag(behandlingId)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> evaluerGrunnlag(e.getValue())));
    }

    private VilkårData evaluerGrunnlag(MedlemskapsvilkårGrunnlag grunnlag) {
        Evaluation evaluation = new Medlemskapsvilkår().evaluer(grunnlag);
        return inngangsvilkårOversetter.tilVilkårData(VilkårType.MEDLEMSKAPSVILKÅRET, evaluation, grunnlag);
    }

    //TODO(OJR) kan flyttes..
    private Map<LocalDate, MedlemskapsvilkårGrunnlag> lagGrunnlag(Long behandlingId) {
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandlingId);
        Optional<VurdertMedlemskapPeriode> vurdertMedlemskapPeriode = medlemskap.flatMap(MedlemskapAggregat::getVurderingLøpendeMedlemskap);

        if (!vurdertMedlemskapPeriode.isPresent()) {
            return Collections.emptyMap();
        }
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Map<LocalDate, MedlemskapsvilkårGrunnlag> resulatat = new HashMap<>();

        List<VurdertLøpendeMedlemskap> vurderingsdatoer = vurdertMedlemskapPeriode.get()
            .getPerioder()
            .stream()
            .sorted(Comparator.comparing(VurdertLøpendeMedlemskap::getVurderingsdato))
            .collect(Collectors.toList());

        for (VurdertLøpendeMedlemskap vurdertLøpendeMedlemskap : vurderingsdatoer) {
            LocalDate vurderingsdato = vurdertLøpendeMedlemskap.getVurderingsdato();
            Optional<PersonopplysningerAggregat> aggregatOptional = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunktHvisEksisterer(behandling, vurderingsdato);
            MedlemskapsvilkårGrunnlag grunnlag = new MedlemskapsvilkårGrunnlag(
                MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering()), // FP VK 2.13
                tilPersonStatusType(aggregatOptional),
                brukerNorskNordisk(aggregatOptional),
                vurdertLøpendeMedlemskap.getErEøsBorger(),
                MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS.equals(vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering()));

            grunnlag.setHarSøkerArbeidsforholdOgInntekt(finnOmSøkerHarArbeidsforholdOgInntekt(behandling, vurderingsdato));
            grunnlag.setBrukerAvklartLovligOppholdINorge(vurdertLøpendeMedlemskap.getLovligOppholdVurdering());
            grunnlag.setBrukerAvklartBosatt(vurdertLøpendeMedlemskap.getBosattVurdering());
            grunnlag.setBrukerAvklartOppholdsrett(vurdertLøpendeMedlemskap.getOppholdsrettVurdering());
            grunnlag.setBrukerAvklartPliktigEllerFrivillig(erAvklartSomPliktigEllerFrivillingMedlem(vurdertLøpendeMedlemskap));

            resulatat.put(vurdertLøpendeMedlemskap.getVurderingsdato(), grunnlag);
        }
        return resulatat;
    }

    private boolean erAvklartSomPliktigEllerFrivillingMedlem(VurdertLøpendeMedlemskap vurdertLøpendeMedlemskap) {
        if (vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
            MedlemskapManuellVurderingType.MEDLEM.equals(vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering())) {
            return true;
        }
        if (vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
            MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertLøpendeMedlemskap.getMedlemsperiodeManuellVurdering())) {
            return false;
        }
        return false;
    }

    private boolean brukerNorskNordisk(Optional<PersonopplysningerAggregat> aggregatOptional) {
        if (aggregatOptional.isPresent()) {
            PersonopplysningerAggregat aggregat = aggregatOptional.get();
            final Optional<Statsborgerskap> statsborgerskap = aggregat.getStatsborgerskapFor(aggregat.getSøker().getAktørId()).stream().findFirst();
            return Region.NORDEN.equals(statsborgerskap.map(Statsborgerskap::getRegion).orElse(null));
        }
        return false;
    }

    private PersonStatusType tilPersonStatusType(Optional<PersonopplysningerAggregat> aggregatOptional) {
        if (aggregatOptional.isPresent()) {
            PersonopplysningerAggregat aggregat = aggregatOptional.get();
            PersonstatusType type = Optional.ofNullable(aggregat.getPersonstatusFor(aggregat.getSøker().getAktørId())).map(Personstatus::getPersonstatus).orElse(null);

            if (PersonstatusType.BOSA.equals(type)) {
                return PersonStatusType.BOSA;
            } else if (PersonstatusType.UTVA.equals(type)) {
                return PersonStatusType.UTVA;
            } else if (PersonstatusType.DØD.equals(type)) {
                return PersonStatusType.DØD;
            }
        }
        return null;
    }

    private boolean finnOmSøkerHarArbeidsforholdOgInntekt(Behandling behandling, LocalDate vurderingsdato) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, vurderingsdato);

        if (inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOptional.get();
            Optional<AktørArbeid> søkersArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            if (!søkersArbeid.isPresent()) {
                return false;
            }

            Optional<List<Arbeidsgiver>> arbeidsgivere = finnRelevanteArbeidsgivereMedLøpendeAvtaleEllerAvtaleSomErGyldigPåStp(vurderingsdato, søkersArbeid.get());
            if (!arbeidsgivere.isPresent()) {
                return false;
            }

            Optional<AktørInntekt> aktørInntekt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(behandling.getAktørId());
            if (!aktørInntekt.isPresent()) {
                return false;
            }

            return aktørInntekt.get().getInntektPensjonsgivende().stream()
                .anyMatch(e -> arbeidsgivere.get().contains(e.getArbeidsgiver()));
        }
        return false;
    }

    private Optional<List<Arbeidsgiver>> finnRelevanteArbeidsgivereMedLøpendeAvtaleEllerAvtaleSomErGyldigPåStp(LocalDate skjæringstidspunkt, AktørArbeid søkerArbeid) {
        List<Arbeidsgiver> relevanteArbeid = new ArrayList<>();
        for (Yrkesaktivitet yrkesaktivitet : søkerArbeid.getYrkesaktiviteter()) {
            if (yrkesaktivitet.erArbeidsforhold() && yrkesaktivitet.getAnsettelsesPeriode().isPresent()) {
                AktivitetsAvtale aktivitetsAvtale = yrkesaktivitet.getAnsettelsesPeriode().get();
                // Hvis har en løpende avtale fom før skjæringstidspunktet eller den som dekker skjæringstidspunktet
                if ((aktivitetsAvtale.getErLøpende() && aktivitetsAvtale.getFraOgMed().isBefore(skjæringstidspunkt)) || (aktivitetsAvtale.getFraOgMed().isBefore(skjæringstidspunkt) && aktivitetsAvtale.getTilOgMed().isAfter(skjæringstidspunkt))) {
                    relevanteArbeid.add(yrkesaktivitet.getArbeidsgiver());
                }
            }
        }
        return relevanteArbeid.isEmpty() ? Optional.empty() : Optional.of(relevanteArbeid);
    }
}
