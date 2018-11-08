package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.FØRSTEGANGSSØKNAD;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.MEDHOLD;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.REVURDERING;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.SØKNAD;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.finnSøknadMottattDato;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.finnTermindato;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.InnsynRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.DokumentMapperTjenesteProvider;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.MottaksdatoBeregner;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.PeriodeBeregner;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.FeriePeriodeDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.InfoOmResterendeDagerTjeneste;
import no.nav.foreldrepenger.domene.uttak.beregnflerbarnsuker.BeregnEkstraFlerbarnsukerTjeneste;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.VurderingsstatusKode;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class DokumentTypeDtoMapper {

    private SøknadRepository søknadRepository;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private BrevParametere brevParametere;
    private InnsynRepository innsynRepository;
    private UttakRepository uttakRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private FamilieHendelseTjeneste familiehendelseTjeneste;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;
    private BeregnEkstraFlerbarnsukerTjeneste beregnEkstraFlerbarnsukerTjeneste;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private DokumentBehandlingsresultatMapper dokumentBehandlingsresultatMapper;
    private InfoOmResterendeDagerTjeneste infoOmResterendeDager;

    DokumentTypeDtoMapper() {
        // CDI
    }

    @Inject
    public DokumentTypeDtoMapper(
        BehandlingRepositoryProvider repositoryProvider,
        DokumentMapperTjenesteProvider tjenesteProvider,
        BrevParametere brevParametere,
        DokumentBehandlingsresultatMapper dokumentBehandlingsresultatMapper) {
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.innsynRepository = repositoryProvider.getInnsynRepository();
        this.personopplysningTjeneste = tjenesteProvider.getBasisPersonopplysningTjeneste();
        this.familiehendelseTjeneste = tjenesteProvider.getFamiliehendelseTjeneste();
        this.hentGrunnlagsdataTjeneste = tjenesteProvider.getHentGrunnlagsDataTjeneste();
        this.beregnEkstraFlerbarnsukerTjeneste = tjenesteProvider.getBeregnEkstraFlerbarnsukerTjeneste();
        this.dokumentBehandlingsresultatMapper = dokumentBehandlingsresultatMapper;
        this.infoOmResterendeDager = tjenesteProvider.getInfoOmResterendeDagerTjeneste();
        this.brevParametere = brevParametere;
    }

    public DokumentTypeDto mapToDto(Behandling behandling, String søkersNavn, String personstatus) {
        return mapToDto(behandling, søkersNavn, personstatus, false);
    }

    public DokumentTypeDto mapToDto(Behandling behandling, String søkersNavn, String personstatus, boolean medPerioder) {
        final DokumentTypeDto dto = medPerioder ? new DokumentTypeMedPerioderDto(behandling.getId()) : new DokumentTypeDto(behandling.getId());

        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);

        dto.setMottattDato(finnSøknadMottattDato(behandling, mottatteDokumentRepository, søknad, innsynRepository));
        dto.setMottattKlageDato(getMottattKlageDato(behandling));
        dto.setBehandlingsTypeForAvslagVedtak(utledBehandlingsTypeForAvslagVedtak(behandling));
        dto.setBehandlingsTypePositivtVedtak(utledBehandlingsTypeForPositivtVedtak(behandling));
        dto.setYtelsesTypeKode(behandling.getFagsak().getYtelseType().getKode());
        dto.setBehandlingsfristIUker(behandling.getType().getBehandlingstidFristUker());
        dto.setFritekstKlageOversendt(behandling.hentKlageVurderingResultat(KlageVurdertAv.NFP).map(KlageVurderingResultat::getBegrunnelse).orElse(null));
        dto.setBrukerKjønnKode(utledBrukersKjønn(behandling));
        dto.setRelasjonsKode(utledRelasjonskode(behandling));
        dto.setKlageAvvistÅrsakKode(getAvvistÅrsak(behandling));
        dto.setGjelderFødsel(familiehendelseTjeneste.gjelderFødsel(behandling).orElse(false));
        dto.setTermindatoFraOriginalBehandling(getTermindatoFraOriginalBehandling(behandling));
        dto.setSøkersNavn(søkersNavn);
        dto.setPersonstatus(personstatus);
        dto.setHarIkkeBehandlingsResultat(behandling.getBehandlingsresultat() == null);

        mapDataRelatertTilFamiliehendelse(behandling, dto);

        if (medPerioder) {
            mapDataRelatertTilPerioder(behandling, (DokumentTypeMedPerioderDto) dto, søknad);
        } else {
            if (behandling.getBehandlingsresultat() != null) {
                dokumentBehandlingsresultatMapper
                    .mapDataRelatertTilBehandlingsResultat(behandling, dto);
            }
        }
        return dto;
    }

    private void mapDataRelatertTilPerioder(Behandling behandling, DokumentTypeMedPerioderDto dtoMedPerioder, Optional<Søknad> søknad) {
        mapDataRelatertTilSøknad(søknad, dtoMedPerioder);
        mapDataRelatertTilBehandlingstype(behandling, dtoMedPerioder);
        if (behandling.getBehandlingsresultat() != null) {
            dokumentBehandlingsresultatMapper
                .mapDataRelatertTilBehandlingsResultat(behandling, dtoMedPerioder);
        }

        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        Optional<Beregningsgrunnlag> gjeldendeBeregningsgrunnlag = hentGrunnlagsdataTjeneste.hentGjeldendeBeregningsgrunnlag(behandling);

        if (beregningsgrunnlag.isPresent() && !beregningsgrunnlag.get().getBeregningsgrunnlagPerioder().isEmpty()) {
            DokumentBeregningsgrunnlagMapper dokumentBeregningsgrunnlagMapper = new DokumentBeregningsgrunnlagMapper();
            if (BehandlingType.REVURDERING.equals(behandling.getType()) && gjeldendeBeregningsgrunnlag.isPresent()) {
                dokumentBeregningsgrunnlagMapper.mapDataRelatertTilBeregningsgrunnlag(beregningsgrunnlag.get(), gjeldendeBeregningsgrunnlag.get(), dtoMedPerioder);
            } else {
                dokumentBeregningsgrunnlagMapper.mapDataRelatertTilBeregningsgrunnlag(beregningsgrunnlag.get(), dtoMedPerioder);
            }
        }
        mapDataRelatertTilUttak(behandling, dtoMedPerioder);
        mapDataRelatertTilYtelsefordeling(behandling, dtoMedPerioder);

        Optional<BeregningsresultatFP> beregningsresultat = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        beregningsresultat.ifPresent(beregningsresultatFP -> new DokumentBeregningsresultatMapper(beregningsgrunnlagRepository, uttakRepository, fagsakRelasjonRepository)
            .mapDataRelatertTilBeregningsresultat(behandling, beregningsresultatFP, dtoMedPerioder));

        mapDataRelatertTilInntektsmelding(behandling, dtoMedPerioder);
    }

    private void mapDataRelatertTilYtelsefordeling(Behandling behandling, DokumentTypeMedPerioderDto dtoMedPerioder) {
        Optional<FagsakRelasjon> fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak());
        int dekningsgrad = fagsakRelasjon.map(agg -> agg.getDekningsgrad().getVerdi()).orElse(100);

        dtoMedPerioder.getDokumentBeregningsgrunnlagDto().setDekningsgrad(dekningsgrad);
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregatOpt = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);

        Optional<PerioderAleneOmsorg> perioderAleneOmsorg = ytelseFordelingAggregatOpt.flatMap(YtelseFordelingAggregat::getPerioderAleneOmsorg);
        if (!perioderAleneOmsorg.isPresent()) {
            return;
        }
        int antallPerioderMedAleneomsorg = perioderAleneOmsorg.map(PerioderAleneOmsorg::getPerioder).map(List::size).orElse(0);
        if (antallPerioderMedAleneomsorg > 0) {
            dtoMedPerioder.setAleneomsorg(VurderingsstatusKode.JA.value());
        } else {
            if (VurderingsstatusKode.JA.value().equals(dtoMedPerioder.getAleneomsorg())) {
                dtoMedPerioder.setAleneomsorg(VurderingsstatusKode.NEI.value());
            }
        }
    }

    private void mapDataRelatertTilInntektsmelding(Behandling behandling, DokumentTypeMedPerioderDto dtoMedPerioder) {
        List<MottattDokument> liste = mottatteDokumentRepository.hentMottatteDokument(behandling.getId());
        Optional<MottattDokument> inntektsmeldingDokument = liste.stream().filter(mottattDokument -> mottattDokument.getDokumentTypeId().equals(DokumentTypeId.INNTEKTSMELDING)).findFirst();
        inntektsmeldingDokument.ifPresent(mottattDokument -> {
            dtoMedPerioder.setMottattInntektsmelding(mottattDokument.getMottattDato());

            Optional<Inntektsmelding> inntektsmeldingOptional = inntektArbeidYtelseRepository.hentInntektsMeldingFor(mottattDokument);
            inntektsmeldingOptional.ifPresent(inntektsmelding -> {
                dtoMedPerioder.setArbeidsgiversNavn(inntektsmelding.getVirksomhet().getNavn());
                dtoMedPerioder.setFeriePerioder(mapFeriePerioder(inntektsmelding.getUtsettelsePerioder()));
            });
        });
    }

    private List<FeriePeriodeDto> mapFeriePerioder(List<UtsettelsePeriode> utsettelsePerioder) {
        List<FeriePeriodeDto> feriePerioder = new ArrayList<>();

        utsettelsePerioder.stream().filter(up -> UtsettelseÅrsak.FERIE.equals(up.getÅrsak())).forEach(utsettelsePeriode -> {
            FeriePeriodeDto feriePeriodeDto = new FeriePeriodeDto();
            feriePeriodeDto.setFeriePeriodeFom(utsettelsePeriode.getPeriode().getFomDato().toString());
            feriePeriodeDto.setFeriePeriodeTom(utsettelsePeriode.getPeriode().getTomDato().toString());
            feriePerioder.add(feriePeriodeDto);
        });
        return feriePerioder;
    }

    private void mapDataRelatertTilSøknad(Optional<Søknad> søknad, DokumentTypeMedPerioderDto dto) {
        dto.setAnnenForelderHarRett(true);
        dto.setAleneomsorg(VurderingsstatusKode.IKKE_VURDERT.value());
        if (søknad.isPresent() && søknad.get().getRettighet() != null) {
            if (søknad.get().getRettighet().getHarAleneomsorgForBarnet()) {
                dto.setAleneomsorg(VurderingsstatusKode.JA.value());
            }
            dto.setAnnenForelderHarRett(!Boolean.FALSE.equals(søknad.get().getRettighet().getHarAnnenForeldreRett())); // => true hvis ja eller null, false hvis nei.
        }
    }

    private LocalDate getTermindatoFraOriginalBehandling(Behandling behandling) {
        Optional<Behandling> originalBehandling = behandling.getOriginalBehandling();
        LocalDate termindatoFraOriginalBehandling = null;
        if (originalBehandling.isPresent()) {
            final Optional<FamilieHendelseGrunnlag> hendelseAggregat = familieGrunnlagRepository.hentAggregatHvisEksisterer(originalBehandling.get());
            if (hendelseAggregat.isPresent()) {
                termindatoFraOriginalBehandling = finnTermindato(hendelseAggregat.get()).orElse(null);
            }
        }
        return termindatoFraOriginalBehandling;
    }

    private void mapDataRelatertTilFamiliehendelse(Behandling behandling, DokumentTypeDto dto) {
        final Optional<FamilieHendelseGrunnlag> hendelseAggregat = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        dto.setAntallBarn(0);
        if (hendelseAggregat.isPresent()) {
            final Optional<LocalDate> termindato = finnTermindato(hendelseAggregat.get());
            termindato.ifPresent(dto::setTermindato);
            dto.setAntallBarn(hendelseAggregat.get().getGjeldendeAntallBarn());
            dto.setBarnErFødt(barnErFødt(behandling));
        }
    }

    private void mapDataRelatertTilUttak(Behandling behandling, DokumentTypeMedPerioderDto dto) {
        PeriodeBeregner.setDefaultVerdier(dto);
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultat.isPresent()) {
            dto.setDagerTaptFørTermin(0);
            setDisponibleDager(behandling, dto);
            UttakResultatPerioderEntitet uttakResultatPeriodeEntitet = uttakResultat.get().getGjeldendePerioder();
            List<UttakResultatPeriodeEntitet> perioder = uttakResultatPeriodeEntitet.getPerioder();
            dto.setAntallPerioder(perioder.size());
            Optional<Stønadskonto> stønadsKontoForeldrepengerFørFødsel = fagsakRelasjonRepository.finnRelasjonFor(behandling.getFagsak()).getStønadskontoberegning().map(Stønadskontoberegning::getStønadskontoer)
                .flatMap(stønadskontoer -> PeriodeBeregner.finnStønadsKontoMedType(stønadskontoer, StønadskontoType.FORELDREPENGER_FØR_FØDSEL));
            if (fødtFørTermin(behandling)) {
                dto.setDagerTaptFørTermin(PeriodeBeregner.beregnTapteDagerFørTermin(perioder, stønadsKontoForeldrepengerFørFødsel));
            }
            PeriodeBeregner.mapUttakPerioder(dto, perioder);
            erstattSisteDagAvSistePeriodeHvisAnnenForelderHarLøpendeSakMedEnSenereSisteDag(behandling, dto);
            Integer foreldrepengerUtvidetUker = beregnEkstraFlerbarnsukerTjeneste.beregneEkstraFlerbarnsuker(behandling);
            if (foreldrepengerUtvidetUker > 0) {
                dto.setForeldrepengeperiodenUtvidetUker(foreldrepengerUtvidetUker);
            } else {
                dto.setForeldrepengeperiodenUtvidetUker(null);
            }
        }
    }

    private void setDisponibleDager(Behandling behandling, DokumentTypeMedPerioderDto dto) {
        boolean aleneOmsorg = VurderingsstatusKode.JA.value().equals(dto.getAleneomsorg());
        boolean annenForelderHarRett = dto.getAnnenForelderHarRett();
        dto.setDisponibleDager(infoOmResterendeDager.getDisponibleDager(behandling, aleneOmsorg, annenForelderHarRett));
        dto.setDisponibleFellesDager(infoOmResterendeDager.getDisponibleFellesDager(behandling));
    }

    private void erstattSisteDagAvSistePeriodeHvisAnnenForelderHarLøpendeSakMedEnSenereSisteDag(Behandling behandling, DokumentTypeMedPerioderDto dto) {
        infoOmResterendeDager.getSisteDagAvSistePeriodeTilAnnenForelder(behandling)
            .filter(dato -> dato.isAfter(dto.getSisteDagAvSistePeriode()))
            .ifPresent(dto::setSisteDagAvSistePeriode);
    }



    private boolean fødtFørTermin(Behandling behandling) {
        FamilieHendelse gjeldendeHendelse = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling).map(FamilieHendelseGrunnlag::getGjeldendeVersjon).orElse(null);
        if (gjeldendeHendelse != null && gjeldendeHendelse.getGjelderFødsel()) {
            LocalDate fødselsdato = gjeldendeHendelse.getFødselsdato().orElse(null);
            LocalDate termindato = gjeldendeHendelse.getTerminbekreftelse().map(Terminbekreftelse::getTermindato).orElse(null);
            if (fødselsdato != null && termindato != null && fødselsdato.isBefore(termindato)) {
                return true;
            }
        }
        return false;
    }

    private boolean barnErFødt(Behandling behandling) {
        boolean fødselsdatoPassert = false;
        Optional<FamilieHendelse> gjeldedeBekreftetVersjon = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling).map(FamilieHendelseGrunnlag::getGjeldendeBekreftetVersjon).orElse(Optional.empty());
        if(gjeldedeBekreftetVersjon.isPresent()){
            fødselsdatoPassert = !gjeldedeBekreftetVersjon.get().getBarna().isEmpty();
        }
        return fødselsdatoPassert;
    }

    private void mapDataRelatertTilBehandlingstype(Behandling behandling, DokumentTypeMedPerioderDto dto) {
        dto.setInntektMottattArbgiver(erEndring(behandling.getType())
            && behandling.getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).collect(Collectors.toList()).contains((BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING)));
    }

    private boolean erEndring(BehandlingType behandlingType) {
        return BehandlingType.REVURDERING.equals(behandlingType)
            || BehandlingType.KLAGE.equals(behandlingType);
    }

    private String utledBehandlingsTypeForAvslagVedtak(Behandling behandling) {
        if (BehandlingType.FØRSTEGANGSSØKNAD.equals(behandling.getType())) {
            return behandling.getFagsakYtelseType().gjelderForeldrepenger() ? FØRSTEGANGSSØKNAD : SØKNAD;
        }
        if (BehandlingType.REVURDERING.equals(behandling.getType())) {
            return REVURDERING;
        }
        return SØKNAD;
    }

    private String utledBehandlingsTypeForPositivtVedtak(Behandling behandling) {
        String behandlingsType;
        boolean etterKlage = behandling.getBehandlingÅrsaker().stream().map(BehandlingÅrsak::getBehandlingÅrsakType).anyMatch(BehandlingÅrsakType.årsakerEtterKlageBehandling()::contains);
        if (etterKlage) {
            behandlingsType = MEDHOLD;
        } else {
            behandlingsType = (BehandlingType.REVURDERING.equals(behandling.getType())) ? REVURDERING : FØRSTEGANGSSØKNAD;
        }
        return behandlingsType;
    }

    private String utledRelasjonskode(Behandling behandling) {
        return behandling.getRelasjonsRolleType() != null ? behandling.getRelasjonsRolleType().getKode() : "";
    }

    private String utledBrukersKjønn(Behandling behandling) {
        return personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)
            .map(PersonopplysningerAggregat::getSøker)
            .map(Personopplysning::getKjønn)
            .orElse(NavBrukerKjønn.UDEFINERT).getKode();
    }

    private LocalDate getMottattKlageDato(Behandling behandling) {
        return MottaksdatoBeregner.finnKlagedato(behandling, mottatteDokumentRepository).orElse(LocalDate.now(FPDateUtil.getOffset()));
    }

    private String getAvvistÅrsak(Behandling behandling) {
        Optional<KlageVurderingResultat> klageVurderingResultat = behandling.hentGjeldendeKlageVurderingResultat();

        return klageVurderingResultat.map(KlageVurderingResultat::getKlageAvvistÅrsak).map(Kodeliste::getKode).orElse(null);
    }

    public BrevParametere getBrevParametere() {
        return brevParametere;
    }
}
