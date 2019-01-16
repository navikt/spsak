package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.IAYRegisterInnhentingTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.inntekt.sigrun.SigrunTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.InstantUtil;
import no.nav.foreldrepenger.behandlingslager.aktør.Adresseinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.AdressePeriode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.Personhistorikkinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.PersonstatusPeriode;
import no.nav.foreldrepenger.behandlingslager.aktør.historikk.StatsborgerskapPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.medlem.api.FinnMedlemRequest;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.Medlemskapsperiode;
import no.nav.foreldrepenger.domene.person.PersoninfoAdapter;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class RegisterdataInnhenterImpl implements RegisterdataInnhenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterdataInnhenterImpl.class);
    private PersoninfoAdapter personinfoAdapter;
    private MedlemTjeneste medlemTjeneste;
    private BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste;
    private PersonopplysningRepository personopplysningRepository;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SigrunTjeneste sigrunTjeneste;
    private MedlemskapRepository medlemskapRepository;
    private OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste;
    private BehandlingsgrunnlagKodeverkRepository behandlingsgrunnlagKodeverkRepository;

    RegisterdataInnhenterImpl() {
        // for CDI proxy
    }

    @Inject
    public RegisterdataInnhenterImpl(PersoninfoAdapter personinfoAdapter, // NOSONAR - krever mange parametere
                                     MedlemTjeneste medlemTjeneste,
                                     SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                     BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste,
                                     GrunnlagRepositoryProvider repositoryProvider,
                                     SigrunTjeneste sigrunTjeneste,
                                     InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                     OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste) {
        this.personinfoAdapter = personinfoAdapter;
        this.medlemTjeneste = medlemTjeneste;
        this.behandlingskontrollTaskTjeneste = behandlingskontrollTaskTjeneste;
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.sigrunTjeneste = sigrunTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.behandlingsgrunnlagKodeverkRepository = repositoryProvider.getBehandlingsgrunnlagKodeverkRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.opplysningsPeriodeTjeneste = opplysningsPeriodeTjeneste;
    }

    @Override
    public Personinfo innhentSaksopplysningerForSøker(Behandling behandling) {
        AktørId aktørId = behandling.getFagsak().getNavBruker().getAktørId();
        return personinfoAdapter.innhentSaksopplysningerForSøker(aktørId);
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
        innhentPersonopplysninger(behandling, søkerInfo);
        return søkerInfo;
    }

    @Override
    public void innhentPersonopplysninger(Behandling behandling, Personinfo søkerInfo) {

        final PersonInformasjonBuilder personInformasjonBuilder = byggPersonopplysningMedRelasjoner(søkerInfo, behandling);

        personopplysningRepository.lagre(behandling, personInformasjonBuilder);
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
                                                                      Behandling behandling) {

        final PersonInformasjonBuilder informasjonBuilder = personopplysningRepository.opprettBuilderForRegisterdata(behandling);

        // Historikk for søker
        final Interval opplysningsperiooden = opplysningsPeriodeTjeneste.beregn(behandling);
        final Personhistorikkinfo personhistorikkinfo = personinfoAdapter.innhentPersonopplysningerHistorikk(søkerPersonInfo.getAktørId(), opplysningsperiooden);
        if (personhistorikkinfo != null) {
            mapAdresser(personhistorikkinfo.getAdressehistorikk(), informasjonBuilder, søkerPersonInfo);
            mapStatsborgerskap(personhistorikkinfo.getStatsborgerskaphistorikk(), informasjonBuilder, søkerPersonInfo);
            mapPersonstatus(personhistorikkinfo.getPersonstatushistorikk(), informasjonBuilder, søkerPersonInfo);
        }

        mapTilPersonopplysning(søkerPersonInfo, informasjonBuilder, false);

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

    private void mapTilPersonopplysning(Personinfo personinfo, PersonInformasjonBuilder informasjonBuilder, boolean erIkkeSøker) {
        mapInfoTilEntitet(personinfo, informasjonBuilder, erIkkeSøker);
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
