package no.nav.foreldrepenger.web.app.tjenester.behandling.medlem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertLøpendeMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.EndringsresultatPersonopplysningerForMedlemskap;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskap;
import no.nav.foreldrepenger.domene.medlem.api.VurderingsÅrsak;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning.PersonopplysningDtoTjeneste;

@ApplicationScoped
public class MedlemDtoTjenesteImpl implements MedlemDtoTjeneste {
    private final static List<AksjonspunktDefinisjon> MEDL_AKSJONSPUNKTER = List.of(AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT,
        AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE,
        AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD,
        AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT);

    private MedlemskapRepository medlemskapRepository;
    private TpsTjeneste tpsTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BehandlingRepository behandlingRepository;
    private MedlemTjeneste medlemTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private PersonopplysningDtoTjeneste personopplysningDtoTjeneste;

    @Inject
    public MedlemDtoTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                 TpsTjeneste tpsTjeneste,
                                 SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                 InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, BehandlingRepository behandlingRepository,
                                 MedlemTjeneste medlemTjeneste, PersonopplysningTjeneste personopplysningTjeneste,
                                 PersonopplysningDtoTjeneste personopplysningDtoTjeneste) {

        this.medlemskapRepository = behandlingRepositoryProvider.getMedlemskapRepository();
        this.tpsTjeneste = tpsTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.behandlingRepository = behandlingRepository;
        this.medlemTjeneste = medlemTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.personopplysningDtoTjeneste = personopplysningDtoTjeneste;
    }

    MedlemDtoTjenesteImpl() {
        // CDI
    }

    private static List<MedlemskapPerioderDto> lagMedlemskapPerioderDto(Set<RegistrertMedlemskapPerioder> perioder) {
        return perioder.stream().map(mp -> {
            MedlemskapPerioderDto dto = new MedlemskapPerioderDto();
            dto.setFom(mp.getFom());
            dto.setTom(mp.getTom());
            dto.setMedlemskapType(mp.getMedlemskapType());
            dto.setKildeType(mp.getKildeType());
            dto.setDekningType(mp.getDekningType());
            dto.setBeslutningsdato(mp.getBeslutningsdato());
            return dto;
        }).collect(Collectors.toList());
    }

    private static void mapInntekt(Collection<InntektDto> inntektDto, final String navn, Inntekt inntekt) {
        inntekt.getInntektspost()
            .forEach(inntektspost -> {
                InntektDto dto = new InntektDto(); // NOSONAR
                dto.setNavn(navn);
                // TODO(OJR) fix denne
                if (inntekt.getArbeidsgiver() != null) {
                    dto.setUtbetaler(inntekt.getArbeidsgiver().getIdentifikator());
                } else {
                    if (inntektspost.getYtelseType() != null) {
                        dto.setUtbetaler(inntektspost.getYtelseType().getNavn());
                    }
                }

                dto.setFom(inntektspost.getFraOgMed());
                dto.setTom(inntektspost.getTilOgMed());
                dto.setYtelse(inntektspost.getInntektspostType().equals(InntektspostType.YTELSE));
                dto.setBelop(inntektspost.getBeløp().getVerdi().intValue());
                inntektDto.add(dto);
            });
    }

    @Override
    public Optional<MedlemDto> lagMedlemDto(Long behandlingId) {
        Optional<MedlemskapAggregat> medlemskapOpt = medlemskapRepository.hentMedlemskap(behandlingId);
        MedlemDto dto = new MedlemDto();
        inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandlingId).ifPresent(aggregat -> dto.setInntekt(lagInntektDto(aggregat)));
        // TODO(OJR) oppgitte eller bekreftet her? :D
        dto.setSkjearingstidspunkt(skjæringstidspunktTjeneste.utledSkjæringstidspunktForEngangsstønadFraOppgitteData(behandlingId));

        if (medlemskapOpt.isPresent()) {
            MedlemskapAggregat aggregat = medlemskapOpt.get();
            dto.setMedlemskapPerioder(lagMedlemskapPerioderDto(aggregat.getRegistrertMedlemskapPerioder()));
            Optional<VurdertMedlemskap> vurdertMedlemskapOpt = aggregat.getVurdertMedlemskap();
            if (vurdertMedlemskapOpt.isPresent()) {
                VurdertMedlemskap vurdertMedlemskap = vurdertMedlemskapOpt.get();
                dto.setOppholdsrettVurdering(vurdertMedlemskap.getOppholdsrettVurdering());
                dto.setLovligOppholdVurdering(vurdertMedlemskap.getLovligOppholdVurdering());
                dto.setBosattVurdering(vurdertMedlemskap.getBosattVurdering());
                dto.setMedlemskapManuellVurderingType(vurdertMedlemskap.getMedlemsperiodeManuellVurdering());
                dto.setErEosBorger(vurdertMedlemskap.getErEøsBorger());
                dto.setFom(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandlingRepository.hentBehandling(behandlingId)));
            }
        }

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Optional<PersonopplysningerAggregat> aggregatOptional = personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling);
        // TODO Diamant (Denne gjelder kun revurdering og foreldrepenger, bør skilles ut som egen DTO for FP+BT-004)
        if (aggregatOptional.isPresent()) {
            EndringsresultatPersonopplysningerForMedlemskap endringerIPersonopplysninger = medlemTjeneste.søkerHarEndringerIPersonopplysninger(behandling);
            List<EndringsresultatPersonopplysningerForMedlemskap.Endring> endredeAttributter = endringerIPersonopplysninger.getEndredeAttributter();
            if (!endredeAttributter.isEmpty()) {
                if (!Optional.ofNullable(dto.getFom()).isPresent()) {
                    dto.setFom(endringerIPersonopplysninger.getGjeldendeFra().get());
                }
                List<EndringIPersonopplysningDto> endringer = new ArrayList<>();
                endredeAttributter.forEach(e -> {
                    endringer.add(new EndringIPersonopplysningDto(e));
                });
                dto.setEndringer(endringer);
            } else {
                /**
                 * Ingen endringer i personopplysninger (siden siste vedtatte medlemskapsperiode), så vi setter
                 * gjeldende f.o.m fra nyeste endring i personstatus. Denne vises b.a. ifm. aksjonspunkt 5022
                 */
                if (dto.getFom() != null && aggregatOptional.get().getPersonstatusFor(behandling.getAktørId()) != null) {
                    if (dto.getFom().isBefore(aggregatOptional.get().getPersonstatusFor(behandling.getAktørId()).getPeriode().getFomDato())) {
                        dto.setFom(aggregatOptional.get().getPersonstatusFor(behandling.getAktørId()).getPeriode().getFomDato());
                    }
                }
            }
        }

        return Optional.of(dto);
    }

    @Override
    public Optional<MedlemV2Dto> lagMedlemPeriodisertDto(Long behandlingId) {
        Optional<MedlemskapAggregat> medlemskapOpt = medlemskapRepository.hentMedlemskap(behandlingId);
        final MedlemV2Dto dto = new MedlemV2Dto();

        mapInntekter(dto, behandlingId);
        final Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        mapSkjæringstidspunkt(dto, medlemskapOpt.orElse(null), behandling);
        mapAndrePerioder(dto, medlemskapOpt.flatMap(MedlemskapAggregat::getVurderingLøpendeMedlemskap).map(VurdertMedlemskapPeriode::getPerioder).orElse(Collections.emptySet()), behandling);
        mapRegistrerteMedlPerioder(dto, medlemskapOpt.map(MedlemskapAggregat::getRegistrertMedlemskapPerioder).orElse(Collections.emptySet()));

        return Optional.of(dto);
    }

    private void mapRegistrerteMedlPerioder(MedlemV2Dto dto, Set<RegistrertMedlemskapPerioder> perioder) {
        dto.setMedlemskapPerioder(lagMedlemskapPerioderDto(perioder));
    }

    private void mapAndrePerioder(MedlemV2Dto dto, Set<VurdertLøpendeMedlemskap> perioder, Behandling behandling) {
        final Map<LocalDate, VurderMedlemskap> vurderingspunkter = medlemTjeneste.utledVurderingspunkterMedAksjonspunkt(behandling);
        final Set<MedlemPeriodeDto> dtoPerioder = dto.getPerioder();
        for (Map.Entry<LocalDate, VurderMedlemskap> entrySet : vurderingspunkter.entrySet()) {
            final MedlemPeriodeDto medlemPeriodeDto = mapTilPeriodeDto(behandling, finnVurderMedlemskap(perioder, entrySet), entrySet.getKey(), entrySet.getValue().getÅrsaker());
            medlemPeriodeDto.setAksjonspunkter(entrySet.getValue().getAksjonspunkter().stream().map(KodeverkTabell::getKode).collect(Collectors.toSet()));
            dtoPerioder.add(medlemPeriodeDto);
        }
    }

    private Optional<VurdertMedlemskap> finnVurderMedlemskap(Set<VurdertLøpendeMedlemskap> perioder, Map.Entry<LocalDate, VurderMedlemskap> entrySet) {
        return perioder.stream()
            .filter(it -> it.getVurderingsdato().equals(entrySet.getKey())).map(it -> (VurdertMedlemskap) it).findAny();
    }

    private void mapInntekter(MedlemV2Dto dto, Long behandlingId) {
        inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandlingId).ifPresent(aggregat -> dto.setInntekt(lagInntektDto(aggregat)));
    }

    private void mapSkjæringstidspunkt(MedlemV2Dto dto, MedlemskapAggregat aggregat, Behandling behandling) {
        final Optional<MedlemskapAggregat> aggregatOpts = Optional.ofNullable(aggregat);
        final Optional<VurdertMedlemskap> vurdertMedlemskapOpt = aggregatOpts.flatMap(MedlemskapAggregat::getVurdertMedlemskap);
        final Set<MedlemPeriodeDto> periodeSet = new HashSet<>();
        final LocalDate vurderingsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        final MedlemPeriodeDto periodeDto = mapTilPeriodeDto(behandling, vurdertMedlemskapOpt, vurderingsdato, Set.of(VurderingsÅrsak.SKJÆRINGSTIDSPUNKT));
        periodeDto.setAksjonspunkter(behandling.getAksjonspunkter().stream()
            .map(Aksjonspunkt::getAksjonspunktDefinisjon)
            .filter(MEDL_AKSJONSPUNKTER::contains)
            .map(KodeverkTabell::getKode).collect(Collectors.toSet()));
        periodeSet.add(periodeDto);
        dto.setPerioder(periodeSet);
    }

    private MedlemPeriodeDto mapTilPeriodeDto(Behandling behandling, Optional<VurdertMedlemskap> vurdertMedlemskapOpt, LocalDate vurderingsdato, Set<VurderingsÅrsak> årsaker) {
        final MedlemPeriodeDto periodeDto = new MedlemPeriodeDto();
        periodeDto.setÅrsaker(årsaker);
        personopplysningDtoTjeneste.lagPersonopplysningDto(behandling.getId(), vurderingsdato).ifPresent(periodeDto::setPersonopplysninger);
        periodeDto.setVurderingsdato(vurderingsdato);

        if (vurdertMedlemskapOpt.isPresent()) {
            final VurdertMedlemskap vurdertMedlemskap = vurdertMedlemskapOpt.get();
            periodeDto.setBosattVurdering(vurdertMedlemskap.getBosattVurdering());
            periodeDto.setOppholdsrettVurdering(vurdertMedlemskap.getOppholdsrettVurdering());
            periodeDto.setLovligOppholdVurdering(vurdertMedlemskap.getLovligOppholdVurdering());
            periodeDto.setErEosBorger(vurdertMedlemskap.getErEøsBorger());
            periodeDto.setMedlemskapManuellVurderingType(vurdertMedlemskap.getMedlemsperiodeManuellVurdering());
        }
        return periodeDto;
    }

    @Override
    public Optional<MedlemDto> lagMedlemDto(Behandling behandling) {
        return lagMedlemDto(behandling.getId());
    }

    private List<InntektDto> lagInntektDto(InntektArbeidYtelseGrunnlag aggregat) {
        List<InntektDto> inntektDto = new ArrayList<>();
        aggregat.getAktørInntektForFørStp().forEach(aktørInntekt -> mapAktørInntekt(inntektDto, aktørInntekt));
        return inntektDto;
    }

    private void mapAktørInntekt(List<InntektDto> inntektDto, AktørInntekt aktørInntekt) {
        String navn = hentNavnFraTps(aktørInntekt.getAktørId());
        aktørInntekt.getInntektPensjonsgivende().forEach(inntekt -> mapInntekt(inntektDto, navn, inntekt));
    }

    private String hentNavnFraTps(AktørId aktørId) {
        Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(aktørId);
        return personinfo.map(Personinfo::getNavn).orElse("UKJENT NAVN"); //$NON-NLS-1$
    }

}
