package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.InstantUtil;
import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.AdressePeriode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.PersonstatusPeriode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.StatsborgerskapPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.FinnMedlemRequest;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.Medlemskapsperiode;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.person.impl.TpsFødselUtil;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class RegisterdataInnhenterImpl implements RegisterdataInnhenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterdataInnhenterImpl.class);
    private PersoninfoAdapter personinfoAdapter;
    private MedlemTjeneste medlemTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;
    private PersonopplysningRepository personopplysningRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private FamilieHendelseTjeneste familieHendelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SigrunTjeneste sigrunTjeneste;
    private MedlemskapRepository medlemskapRepository;
    private SøknadRepository søknadRepository;
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;
    private Period etterkontrollTidsromFørSøknadsdato;
    private Period etterkontrollTidsromEtterTermindato;

    RegisterdataInnhenterImpl() {
        // for CDI proxy
    }

    @Inject
    public RegisterdataInnhenterImpl(PersoninfoAdapter personinfoAdapter, // NOSONAR - krever mange parametere
                                     MedlemTjeneste medlemTjeneste,
                                     SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                     BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste,
                                     BehandlingRepositoryProvider repositoryProvider,
                                     FamilieHendelseTjeneste familieHendelseTjeneste,
                                     SigrunTjeneste sigrunTjeneste,
                                     InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                     MedlemskapRepository medlemskapRepository,
                                     BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository,
                                     OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste,
                                     @KonfigVerdi("etterkontroll.førsøknad.periode") Instance<Period> etterkontrollTidsromFørSøknadsdato,
                                     @KonfigVerdi("etterkontroll.ettertermin.periode") Instance<Period> etterkontrollTidsromEtterTermindato) {
        this.personinfoAdapter = personinfoAdapter;
        this.medlemTjeneste = medlemTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.behandlingskontrollTaskTjeneste = behandlingskontrollTaskTjeneste;
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.familieHendelseTjeneste = familieHendelseTjeneste;
        this.sigrunTjeneste = sigrunTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.medlemskapRepository = medlemskapRepository;
        this.behandlingsgrunnlagKodeverkRepository = behandlingsgrunnlagKodeverkRepository;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.opplysningsPeriodeTjeneste = opplysningsPeriodeTjeneste;
        this.etterkontrollTidsromFørSøknadsdato = etterkontrollTidsromFørSøknadsdato.get();
        this.etterkontrollTidsromEtterTermindato = etterkontrollTidsromEtterTermindato.get();
    }

    @Override
    public Personinfo innhentSaksopplysningerForSøker(Behandling behandling) {
        AktørId aktørId = behandling.getFagsak().getNavBruker().getAktørId();
        return personinfoAdapter.innhentSaksopplysningerForSøker(aktørId);
    }

    @Override
    public Optional<Personinfo> innhentSaksopplysningerForMedSøker(Behandling behandling) {
        final Optional<OppgittAnnenPart> oppgittAnnenPart = personopplysningRepository.hentPersonopplysningerHvisEksisterer(behandling)
            .flatMap(PersonopplysningGrunnlag::getOppgittAnnenPart);
        Optional<AktørId> funnetAktørId = oppgittAnnenPart.map(OppgittAnnenPart::getAktørId);

        return funnetAktørId.flatMap(
            aktørId -> personinfoAdapter.innhentSaksopplysningerForEktefelle(Optional.of(aktørId)));
    }

    @Override
    public Personinfo innhentPersonopplysninger(Behandling behandling) {
        // Innhent data fra TPS for søker
        Personinfo søkerInfo = innhentSaksopplysningerForSøker(behandling);

        if (søkerInfo == null) {
            throw SaksopplysningerFeil.FACTORY.feilVedOppslagITPS(behandling.getFagsak().getNavBruker().getAktørId().toString())
                .toException();
        }

        // Innhent øvrige data fra TPS
        Optional<Personinfo> medsøkerInfo = innhentSaksopplysningerForMedSøker(behandling);
        innhentPersonopplysninger(behandling, søkerInfo, medsøkerInfo);
        innhentFamiliehendelse(behandling);
        return søkerInfo;
    }

    @Override
    public void innhentPersonopplysninger(Behandling behandling, Personinfo søkerInfo,
                                          Optional<Personinfo> medsøkerInfo) {

        final PersonInformasjonBuilder personInformasjonBuilder = byggPersonopplysningMedRelasjoner(søkerInfo, medsøkerInfo, behandling);

        personopplysningRepository.lagre(behandling, personInformasjonBuilder);
    }


    private void innhentFamiliehendelse(Behandling behandling) {
        FamilieHendelseGrunnlag aggregat = familieHendelseRepository.hentAggregat(behandling);
        List<FødtBarnInfo> fødselRegistrertTps = personinfoAdapter.innhentAlleFødteForBehandling(behandling, aggregat);
        familieHendelseTjeneste.oppdaterFødselPåGrunnlag(behandling, fødselRegistrertTps);
    }

    @Override
    public void innhentIAYOpplysninger(Behandling behandling, Personinfo søkerInfo) {
        final IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste = getIAYRegisterInnhenterFor(behandling);
        Interval opplysningsPeriode = iayRegisterInnhentingTjeneste.beregnOpplysningsPeriode(behandling);

        //Innhent relaterte ytelser fra Infotrygd og Arena og FPSAK
        innhentOgLagreYtelser(behandling, opplysningsPeriode, iayRegisterInnhentingTjeneste);

        final InntektArbeidYtelseAggregatBuilder aggregatBuilder = iayRegisterInnhentingTjeneste.innhentOpptjeningForInnvolverteParter(behandling, opplysningsPeriode);
        iayRegisterInnhentingTjeneste.lagre(behandling, aggregatBuilder);

        // Innhent medl for søker
        List<RegistrertMedlemskapPerioder> medlemskapsperioder = innhentMedlemskapsopplysninger(søkerInfo, behandling);
        medlemskapRepository.lagreMedlemskapRegisterOpplysninger(behandling, medlemskapsperioder);

        // Innhent data fra SIGRUN for de 3 siste ferdiglignede årene
        if (iayRegisterInnhentingTjeneste.skalInnhenteNæringsInntekterFor(behandling) && behandling.getType().equals(BehandlingType.FØRSTEGANGSSØKNAD)
            && inntektArbeidYtelseTjeneste.søkerHarOppgittEgenNæring(behandling)) {
            sigrunTjeneste.hentOgLagrePGI(behandling, søkerInfo.getAktørId());
        }
    }

    private void innhentOgLagreYtelser(Behandling behandling, Interval opplysningsPeriode, IAYRegisterInnhentingTjeneste iayRegisterInnhentingTjeneste) {
        final InntektArbeidYtelseAggregatBuilder aggregatBuilder = iayRegisterInnhentingTjeneste.innhentYtelserForInvolverteParter(behandling, opplysningsPeriode);
        if (behandling.getType().equals(BehandlingType.REVURDERING)) {
            // Må sjekke om denne virker som tiltenkt. IAY gjør egen endringskontroll.
            boolean endringsresultat = inntektArbeidYtelseTjeneste.erEndret(behandling, aggregatBuilder);
            if (endringsresultat) {
                LOGGER.info("IAY-innhenting for revurdering detekterte endring i AktørYtelse: {}", behandling.getId());
            } else {
                LOGGER.info("IAY-innhenting for revurdering fant ingen endring i AktørYtelse: {}", behandling.getId());
            }
        }
        iayRegisterInnhentingTjeneste.lagre(behandling, aggregatBuilder);
    }

    /**
     * Er kun tilgjengelig for testing...
     *
     * @param behandling be
     * @return asd
     */
    protected IAYRegisterInnhentingTjeneste getIAYRegisterInnhenterFor(Behandling behandling) {
        return CDI.current()
            .select(IAYRegisterInnhentingTjeneste.class,
                new FagsakYtelseTypeRef.FagsakYtelseTypeRefLiteral(behandling.getFagsak().getYtelseType().getKode())).get();
    }

    @Override
    public PersonInformasjonBuilder byggPersonopplysningMedRelasjoner(Personinfo søkerPersonInfo,
                                                                      Optional<Personinfo> annenPartInfo,
                                                                      Behandling behandling) {

        final PersonInformasjonBuilder informasjonBuilder = personopplysningRepository.opprettBuilderForRegisterdata(behandling);
        informasjonBuilder.tilbakestill(behandling.getAktørId(), annenPartInfo.map(Personinfo::getAktørId));

        // Historikk for søker
        final Interval opplysningsperiooden = opplysningsPeriodeTjeneste.beregn(behandling);
        final Personhistorikkinfo personhistorikkinfo = personinfoAdapter.innhentPersonopplysningerHistorikk(søkerPersonInfo.getAktørId(), opplysningsperiooden);
        if (personhistorikkinfo != null) {
            mapAdresser(personhistorikkinfo.getAdressehistorikk(), informasjonBuilder, søkerPersonInfo);
            mapStatsborgerskap(personhistorikkinfo.getStatsborgerskaphistorikk(), informasjonBuilder, søkerPersonInfo);
            mapPersonstatus(personhistorikkinfo.getPersonstatushistorikk(), informasjonBuilder, søkerPersonInfo);
        }

        mapTilPersonopplysning(søkerPersonInfo, informasjonBuilder, true, false, behandling);
        // Ektefelle
        leggTilEktefelle(søkerPersonInfo, informasjonBuilder, behandling);

        // Medsøker (annen part). kan være samme person som Ektefelle
        annenPartInfo.ifPresent(annenPart -> leggTilMedsøkerAnnenPart(søkerPersonInfo, annenPart, informasjonBuilder, behandling));

        return informasjonBuilder;
    }

    private void mapPersonstatus(List<PersonstatusPeriode> personstatushistorikk, PersonInformasjonBuilder informasjonBuilder, Personinfo personinfo) {
        for (PersonstatusPeriode personstatus : personstatushistorikk) {
            final PersonstatusType status = personstatus.getPersonstatus();
            final DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(
                brukFødselsdatoHvisEtter(personstatus.getGyldighetsperiode().getFom(), personinfo.getFødselsdato()), personstatus.getGyldighetsperiode().getTom());
            final PersonInformasjonBuilder.PersonstatusBuilder builder = informasjonBuilder.getPersonstatusBuilder(personinfo.getAktørId(), periode);
            builder.medPeriode(periode)
                .medPersonstatus(status);
            informasjonBuilder.leggTil(builder);
        }
    }

    private void mapStatsborgerskap(List<StatsborgerskapPeriode> statsborgerskaphistorikk, PersonInformasjonBuilder informasjonBuilder, Personinfo personinfo) {
        for (StatsborgerskapPeriode statsborgerskap : statsborgerskaphistorikk) {
            final Landkoder landkode = kodeverkRepository.finn(Landkoder.class, statsborgerskap.getStatsborgerskap().getLandkode());

            Region region = behandlingsgrunnlagKodeverkRepository.finnHøyestRangertRegion(Collections.singletonList(statsborgerskap.getStatsborgerskap().getLandkode()));

            final DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(brukFødselsdatoHvisEtter(
                statsborgerskap.getGyldighetsperiode().getFom(), personinfo.getFødselsdato()), statsborgerskap.getGyldighetsperiode().getTom());
            final PersonInformasjonBuilder.StatsborgerskapBuilder builder = informasjonBuilder.getStatsborgerskapBuilder(personinfo.getAktørId(), periode, landkode, region);
            builder.medPeriode(periode)
                .medStatsborgerskap(landkode);
            builder.medRegion(region);

            informasjonBuilder.leggTil(builder);
        }
    }

    private void mapAdresser(List<AdressePeriode> adressehistorikk, PersonInformasjonBuilder informasjonBuilder, Personinfo personinfo) {
        AktørId aktørId = personinfo.getAktørId();
        for (AdressePeriode adresse : adressehistorikk) {
            final DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(brukFødselsdatoHvisEtter(adresse.getGyldighetsperiode().getFom(), personinfo.getFødselsdato()), adresse.getGyldighetsperiode().getTom());
            final PersonInformasjonBuilder.AdresseBuilder adresseBuilder = informasjonBuilder.getAdresseBuilder(aktørId, periode, adresse.getAdresse().getAdresseType());
            adresseBuilder.medPeriode(periode)
                .medAdresselinje1(adresse.getAdresse().getAdresselinje1())
                .medAdresselinje2(adresse.getAdresse().getAdresselinje2())
                .medAdresselinje3(adresse.getAdresse().getAdresselinje3())
                .medAdresselinje4(adresse.getAdresse().getAdresselinje4())
                .medLand(adresse.getAdresse().getLand())
                .medPostnummer(adresse.getAdresse().getPostnummer())
                .medPoststed(adresse.getAdresse().getPoststed());
            informasjonBuilder.leggTil(adresseBuilder);
        }
    }

    private LocalDate brukFødselsdatoHvisEtter(LocalDate dato, LocalDate fødseldato) {
        if (dato.isBefore(fødseldato)) {
            return fødseldato;
        }
        return dato;
    }

    private void mapTilPersonopplysning(Personinfo personinfo, PersonInformasjonBuilder informasjonBuilder, boolean skalHenteBarnRelasjoner, boolean erIkkeSøker, Behandling behandling) {
        mapInfoTilEntitet(personinfo, informasjonBuilder, erIkkeSøker);

        if (skalHenteBarnRelasjoner) {
            List<Personinfo> barna = hentBarnRelatertTil(personinfo, behandling);
            barna.forEach(barn -> {
                mapInfoTilEntitet(barn, informasjonBuilder, true);
                mapRelasjon(personinfo, barn, Collections.singletonList(RelasjonsRolleType.BARN), informasjonBuilder);
                mapRelasjon(barn, personinfo, utledRelasjonsrolleTilBarn(personinfo, barn), informasjonBuilder);
            });
        }
    }

    private List<RelasjonsRolleType> utledRelasjonsrolleTilBarn(Personinfo personinfo, Personinfo barn) {
        if (barn == null) {
            return Collections.emptyList();
        }
        return barn.getFamilierelasjoner().stream()
            .filter(fr -> fr.getPersonIdent().equals(personinfo.getPersonIdent()))
            .map(rel -> utledRelasjonsrolleTilBarn(personinfo.getKjønn(), rel.getRelasjonsrolle()))
            .collect(Collectors.toList());
    }

    private void mapRelasjon(Personinfo fra, Personinfo til, List<RelasjonsRolleType> roller, PersonInformasjonBuilder informasjonBuilder) {
        if (til == null) {
            return;
        }
        for (RelasjonsRolleType rolle : roller) {
            final PersonInformasjonBuilder.RelasjonBuilder builder = informasjonBuilder.getRelasjonBuilder(fra.getAktørId(), til.getAktørId(), rolle);
            builder.harSammeBosted(utledSammeBosted(fra, til, rolle));
            informasjonBuilder.leggTil(builder);
        }
    }

    private boolean utledSammeBosted(Personinfo personinfo, Personinfo barn, RelasjonsRolleType rolle) {
        final Optional<Boolean> sammeBosted = personinfo.getFamilierelasjoner().stream()
            .filter(fr -> fr.getRelasjonsrolle().equals(rolle) && fr.getPersonIdent().equals(barn.getPersonIdent()))
            .findAny()
            .map(Familierelasjon::getHarSammeBosted);
        return sammeBosted.orElse(false);
    }

    private void mapInfoTilEntitet(Personinfo personinfo, PersonInformasjonBuilder informasjonBuilder, boolean lagreIHistoriskeTabeller) {
        final DatoIntervallEntitet periode = getPeriode(personinfo.getFødselsdato(), Tid.TIDENES_ENDE);
        final PersonInformasjonBuilder.PersonopplysningBuilder builder = informasjonBuilder.getPersonopplysningBuilder(personinfo.getAktørId());
        builder.medKjønn(personinfo.getKjønn())
            .medFødselsdato(personinfo.getFødselsdato())
            .medNavn(personinfo.getNavn())
            .medDødsdato(personinfo.getDødsdato())
            .medSivilstand(personinfo.getSivilstandType())
            .medRegion(personinfo.getRegion());
        informasjonBuilder.leggTil(builder);

        if (lagreIHistoriskeTabeller || informasjonBuilder.harIkkeFåttStatsborgerskapHistorikk(personinfo.getAktørId())) {
            final PersonInformasjonBuilder.StatsborgerskapBuilder statsborgerskapBuilder = informasjonBuilder.getStatsborgerskapBuilder(personinfo.getAktørId(),
                periode, personinfo.getLandkode(), personinfo.getRegion());
            informasjonBuilder.leggTil(statsborgerskapBuilder);
        }

        if (lagreIHistoriskeTabeller || informasjonBuilder.harIkkeFåttAdresseHistorikk(personinfo.getAktørId())) {
            for (Adresseinfo adresse : personinfo.getAdresseInfoList()) {
                final PersonInformasjonBuilder.AdresseBuilder adresseBuilder = informasjonBuilder.getAdresseBuilder(personinfo.getAktørId(),
                    periode, adresse.getGjeldendePostadresseType());
                informasjonBuilder.leggTil(adresseBuilder
                    .medAdresselinje1(adresse.getAdresselinje1())
                    .medAdresselinje2(adresse.getAdresselinje2())
                    .medAdresselinje3(adresse.getAdresselinje3())
                    .medPostnummer(adresse.getPostNr())
                    .medPoststed(adresse.getPoststed())
                    .medLand(adresse.getLand())
                    .medAdresseType(adresse.getGjeldendePostadresseType())
                    .medPeriode(periode));
            }
        }

        if (lagreIHistoriskeTabeller || informasjonBuilder.harIkkeFåttPersonstatusHistorikk(personinfo.getAktørId())) {
            final PersonInformasjonBuilder.PersonstatusBuilder personstatusBuilder = informasjonBuilder.getPersonstatusBuilder(personinfo.getAktørId(),
                periode).medPersonstatus(personinfo.getPersonstatus());
            informasjonBuilder.leggTil(personstatusBuilder);
        }
    }

    private DatoIntervallEntitet getPeriode(LocalDate fom, LocalDate tom) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom != null ? tom : Tid.TIDENES_ENDE);
    }

    private void leggTilMedsøkerAnnenPart(Personinfo søkerPersonInfo, Personinfo annenPart, PersonInformasjonBuilder informasjonBuilder, Behandling behandling) {
        // Medsøker - kan være samme person som ektefelle
        List<Familierelasjon> fellesBarn = finnFellesBarn(annenPart, søkerPersonInfo);

        mapTilPersonopplysning(annenPart, informasjonBuilder, false, true, behandling);
        for (Familierelasjon familierelasjon : fellesBarn) {
            final Personinfo til = personinfoAdapter.innhentSaksopplysninger(familierelasjon.getPersonIdent()).orElse(null);
            mapRelasjon(annenPart, til, Collections.singletonList(RelasjonsRolleType.BARN), informasjonBuilder);
            mapRelasjon(til, annenPart, utledRelasjonsrolleTilBarn(annenPart, til), informasjonBuilder); // NOSONAR
        }
    }

    private void leggTilEktefelle(Personinfo søkerPersonInfo, PersonInformasjonBuilder informasjonBuilder, Behandling behandling) {
        // Ektefelle
        final List<Familierelasjon> familierelasjoner = søkerPersonInfo.getFamilierelasjoner()
            .stream()
            .filter(f -> f.getRelasjonsrolle().equals(RelasjonsRolleType.EKTE) ||
                f.getRelasjonsrolle().equals(RelasjonsRolleType.REGISTRERT_PARTNER) ||
                f.getRelasjonsrolle().equals(RelasjonsRolleType.SAMBOER))
            .collect(Collectors.toList());
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Optional<Personinfo> ektefelleInfo = personinfoAdapter.innhentSaksopplysninger(familierelasjon.getPersonIdent());
            if (ektefelleInfo.isPresent()) {
                final Personinfo personinfo = ektefelleInfo.get();
                mapTilPersonopplysning(personinfo, informasjonBuilder, false, true, behandling);
                mapRelasjon(søkerPersonInfo, personinfo, Collections.singletonList(familierelasjon.getRelasjonsrolle()), informasjonBuilder);
                mapRelasjon(personinfo, søkerPersonInfo, Collections.singletonList(familierelasjon.getRelasjonsrolle()), informasjonBuilder);
            }
        }
    }

    private List<Personinfo> hentBarnRelatertTil(Personinfo søkerPersonInfo, Behandling behandling) {
        List<Personinfo> relaterteBarn = hentAlleRelaterteBarn(søkerPersonInfo);
        Søknad søknad = søknadRepository.hentFørstegangsSøknad(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandling);

        DatoIntervallEntitet forventetFødselIntervall = TpsFødselUtil.forventetFødselIntervall(familieHendelseGrunnlag,
            etterkontrollTidsromFørSøknadsdato, etterkontrollTidsromEtterTermindato, søknad);

        return relaterteBarn.stream().filter(b -> forventetFødselIntervall.inkluderer(b.getFødselsdato())).collect(Collectors.toList());
    }

    private List<Personinfo> hentAlleRelaterteBarn(Personinfo søkerPersonInfo) {
        return søkerPersonInfo.getFamilierelasjoner()
            .stream()
            .filter(r -> r.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
            .map(r -> personinfoAdapter.innhentSaksopplysningerForBarn(r.getPersonIdent()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private RelasjonsRolleType utledRelasjonsrolleTilBarn(NavBrukerKjønn kjønn, RelasjonsRolleType rolle) {
        if (kjønn.equals(NavBrukerKjønn.KVINNE) && rolle.equals(RelasjonsRolleType.FARA)) {
            return RelasjonsRolleType.MEDMOR;
        }

        return NavBrukerKjønn.KVINNE.equals(kjønn) ? RelasjonsRolleType.MORA : RelasjonsRolleType.FARA;
    }

    private List<Familierelasjon> finnFellesBarn(Personinfo annenPart, Personinfo førstePart) {
        List<PersonIdent> fnrAnnenPartsBarn = annenPart.getFamilierelasjoner().stream()
            .filter(f -> f.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
            .map(Familierelasjon::getPersonIdent)
            .collect(Collectors.toList());
        return førstePart.getFamilierelasjoner().stream()
            .filter(barn -> barn.getRelasjonsrolle().equals(RelasjonsRolleType.BARN) && fnrAnnenPartsBarn.contains(barn.getPersonIdent()))
            .collect(Collectors.toList());
    }

    @Override
    public List<RegistrertMedlemskapPerioder> innhentMedlemskapsopplysninger(Personinfo søkerInfo, Behandling behandling) {
        return innhentMedlemskapsopplysningerFor(søkerInfo, behandling);
    }

    private List<RegistrertMedlemskapPerioder> innhentMedlemskapsopplysningerFor(Personinfo søkerInfo, Behandling behandling) {
        final Interval opplysningsperiode = opplysningsPeriodeTjeneste.beregn(behandling);
        FinnMedlemRequest finnMedlemRequest = new FinnMedlemRequest(søkerInfo.getPersonIdent(), InstantUtil.tilLocalDate(opplysningsperiode.getStart()),
            InstantUtil.tilLocalDate(opplysningsperiode.getEnd()));
        List<Medlemskapsperiode> medlemskapsperioder = medlemTjeneste.finnMedlemskapPerioder(finnMedlemRequest);
        ArrayList<RegistrertMedlemskapPerioder> resultat = new ArrayList<>();
        for (Medlemskapsperiode medlemskapsperiode : medlemskapsperioder) {
            resultat.add(lagMedlemskapPeriode(medlemskapsperiode));
        }
        return resultat;
    }

    private RegistrertMedlemskapPerioder lagMedlemskapPeriode(Medlemskapsperiode medlemskapsperiode) {
        return new MedlemskapPerioderBuilder()
            .medPeriode(medlemskapsperiode.getFom(), medlemskapsperiode.getTom())
            .medBeslutningsdato(medlemskapsperiode.getDatoBesluttet())
            .medErMedlem(medlemskapsperiode.isErMedlem())
            .medLovvalgLand(medlemskapsperiode.getLovvalgsland())
            .medStudieLand(medlemskapsperiode.getStudieland())
            .medDekningType(medlemskapsperiode.getTrygdedekning())
            .medKildeType(medlemskapsperiode.getKilde())
            .medMedlemskapType(medlemskapsperiode.getLovvalg())
            .medMedlId(medlemskapsperiode.getMedlId())
            .build();
    }

    @Override
    public void oppdaterSistOppdatertTidspunkt(Behandling behandling) {
        behandlingRepository.oppdaterSistOppdatertTidspunkt(behandling, LocalDateTime.now(FPDateUtil.getOffset()));
    }

    @Override
    public void opprettProsesstaskForRelaterteYtelser(Behandling behandling) {
        behandlingskontrollTaskTjeneste.opprettBehandlingskontrollTask(InnhentRelaterteYtelserTask.TASKTYPE, behandling.getFagsakId(), behandling.getId(), behandling.getAktørId());
    }
}
