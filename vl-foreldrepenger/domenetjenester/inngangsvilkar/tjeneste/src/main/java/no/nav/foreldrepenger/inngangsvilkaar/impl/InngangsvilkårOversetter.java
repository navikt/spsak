package no.nav.foreldrepenger.inngangsvilkaar.impl;

import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.inngangsvilkaar.regelmodell.adapter.RegelintegrasjonFeil.FEILFACTORY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
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
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.inngangsvilkår.VilkårData;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnMorsMaksdatoTjeneste;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.BekreftetAdopsjon;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.BekreftetAdopsjonBarn;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.VilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.FagsakÅrsak;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.Kjoenn;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.PersonStatusType;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class InngangsvilkårOversetter {

    private VilkårKodeverkRepository kodeverkRepository;
    private MedlemskapRepository medlemskapRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;
    private SøknadRepository søknadRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private BeregnMorsMaksdatoTjeneste beregnMorsMaksdatoTjeneste;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    InngangsvilkårOversetter() {
        // for CDI proxy
    }

    @Inject
    public InngangsvilkårOversetter(BehandlingRepositoryProvider repositoryProvider,
                                    MedlemskapPerioderTjeneste medlemskapPerioderTjeneste,
                                    SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                    BasisPersonopplysningTjeneste personopplysningTjeneste,
                                    BeregnMorsMaksdatoTjeneste beregnMorsMaksdatoTjeneste) {
        this.kodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.medlemskapPerioderTjeneste = medlemskapPerioderTjeneste;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.beregnMorsMaksdatoTjeneste = beregnMorsMaksdatoTjeneste;
    }

    public FødselsvilkårGrunnlag oversettTilRegelModellFødsel(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final Søknad søknad = søknadRepository.hentSøknadHvisEksisterer(behandling).orElseGet(() -> {
            if (behandling.getOriginalBehandling().isPresent()) {
                return søknadRepository.hentSøknad(behandling.getOriginalBehandling().get());
            }
            throw new IllegalStateException("Utvikler feil: Finnes ingen søknad knyttet til fagsaken.");
        });
        Optional<FamilieHendelse> familieHendelse = familieHendelseGrunnlag.getGjeldendeBekreftetVersjon();
        FødselsvilkårGrunnlag grunnlag = new FødselsvilkårGrunnlag(
            tilSoekerKjoenn(getSøkersKjønn(behandling)),
            finnSoekerRolle(behandling),
            søknad.getSøknadsdato(),
            familieHendelse.map(FamilieHendelse::erMorForSykVedFødsel).orElse(false),
            erSøktOmTermin(familieHendelseGrunnlag.getSøknadVersjon()));
        final Optional<LocalDate> fødselsDato = familieHendelse.flatMap(FamilieHendelse::getFødselsdato);
        fødselsDato.ifPresent(grunnlag::setBekreftetFoedselsdato);

        grunnlag.setAntallBarn(familieHendelse.map(FamilieHendelse::getAntallBarn).orElse(0));

        final Optional<Terminbekreftelse> terminbekreftelse = familieHendelseGrunnlag.getGjeldendeTerminbekreftelse();
        terminbekreftelse.ifPresent(terminbekreftelse1 -> grunnlag.setBekreftetTermindato(terminbekreftelse1.getTermindato()));
        return grunnlag;
    }

    private boolean erSøktOmTermin(FamilieHendelse familieHendelse) {
        FamilieHendelseType type = familieHendelse.getType();
        return FamilieHendelseType.TERMIN.equals(type);
    }

    private NavBrukerKjønn getSøkersKjønn(Behandling behandling) {
        return personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)
            .map(PersonopplysningerAggregat::getSøker)
            .map(Personopplysning::getKjønn).orElse(NavBrukerKjønn.UDEFINERT);
    }

    private SoekerRolle finnSoekerRolle(Behandling behandling) {
        RelasjonsRolleType relasjonsRolleType = finnRelasjonRolle(behandling);
        if (Objects.equals(RelasjonsRolleType.MORA, relasjonsRolleType)) {
            return SoekerRolle.MORA;
        } else if (Objects.equals(RelasjonsRolleType.FARA, relasjonsRolleType)) {
            return SoekerRolle.FARA;
        } else if (Objects.equals(RelasjonsRolleType.MEDMOR, relasjonsRolleType)) {
            return SoekerRolle.MEDMOR;
        }
        return null;
    }

    private RelasjonsRolleType finnRelasjonRolle(Behandling behandling) {
        final FamilieHendelseGrunnlag hendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        if (!hendelseGrunnlag.getGjeldendeBekreftetVersjon().isPresent()) {
            // Kan ikke finne relasjonsrolle dersom fødsel ikke er bekreftet.
            return null;
        }
        final FamilieHendelse familieHendelse = hendelseGrunnlag.getGjeldendeBekreftetVersjon().get();
        final Optional<LocalDate> fødselsdato = familieHendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();

        if (!fødselsdato.isPresent()) {
            return null;
        }

        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);

        final Interval fødselIntervall = byggIntervall(fødselsdato.get(), fødselsdato.get());
        List<Personopplysning> alleBarnPåFødselsdato = personopplysninger.getAlleBarnFødtI(fødselIntervall);

        Personopplysning søkerPersonopplysning = personopplysninger.getSøker();
        AktørId søkersAktørId = søkerPersonopplysning.getAktørId();

        if (alleBarnPåFødselsdato.size() > 0) {
            // Forutsetter at barn som er født er tvillinger, og sjekker derfor bare første barn.
            final Optional<PersonRelasjon> personRelasjon = personopplysninger.getRelasjoner()
                .stream()
                .filter(relasjon -> relasjon.getTilAktørId().equals(søkersAktørId))
                .filter(familierelasjon -> RelasjonsRolleType.erRegistrertForeldre(familierelasjon.getRelasjonsrolle()))
                .findFirst();

            return personRelasjon.map(PersonRelasjon::getRelasjonsrolle).orElse(behandling.getRelasjonsRolleType());
        }
        // Har ingenting annet å gå på så benytter det søker oppgir.
        return behandling.getRelasjonsRolleType();
    }

    private Interval byggIntervall(LocalDate fomDato, LocalDate tomDato) {
        LocalDateTime døgnstart = Tid.TIDENES_ENDE.equals(tomDato) ? tomDato.atStartOfDay() : tomDato.atStartOfDay().plusDays(1);
        return Interval.of(
            fomDato.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
            døgnstart.atZone(ZoneId.systemDefault()).toInstant());
    }

    public SoeknadsfristvilkarGrunnlag oversettTilRegelModellSøknad(Behandling behandling) {
        final Søknad søknad = søknadRepository.hentSøknad(behandling);
        LocalDate skjæringsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktForEngangsstønadFraBekreftedeData(behandling)
            // Må godta skjæringstidspunkt fra oppgitte data, ettersom tillatt å godkj. SRB-vilkår uten å bekrefte data
            .orElseGet(() -> skjæringstidspunktTjeneste.utledSkjæringstidspunktForEngangsstønadFraOppgitteData(behandling));
        return new SoeknadsfristvilkarGrunnlag(
            søknad.getElektroniskRegistrert(),
            skjæringsdato,
            søknad.getMottattDato());
    }

    public AdopsjonsvilkårGrunnlag oversettTilRegelModellAdopsjon(Behandling behandling) {
        BekreftetAdopsjon bekreftetAdopsjon = byggBekreftetAdopsjon(behandling);
        List<BekreftetAdopsjonBarn> adopsjonBarn = bekreftetAdopsjon.getAdopsjonBarn();
        return new AdopsjonsvilkårGrunnlag(
            adopsjonBarn,
            bekreftetAdopsjon.isEktefellesBarn(),
            tilSoekerKjoenn(getSøkersKjønn(behandling)),
            bekreftetAdopsjon.isAdoptererAlene(),
            bekreftetAdopsjon.getOmsorgsovertakelseDato(),
            erStønadperiodeBruktOpp(behandling));
    }

    private boolean erStønadperiodeBruktOpp(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        Optional<FamilieHendelse> versjon = familieHendelseGrunnlag.getGjeldendeBekreftetVersjon();
        FamilieHendelse familieHendelse = versjon.orElseGet(familieHendelseGrunnlag::getSøknadVersjon);

        // TODO PK-48734 - er omsorgsovertakelseDato riktig dato?
        if (familieHendelse.getAdopsjon().isPresent()) {
            LocalDate omsorgsovertakelseDato = familieHendelse.getAdopsjon().get().getOmsorgsovertakelseDato();
            Optional<LocalDate> maksdatoForeldrepenger = beregnMorsMaksdatoTjeneste.beregnMaksdatoForeldrepenger(behandling);

            if (!maksdatoForeldrepenger.isPresent() || omsorgsovertakelseDato.isBefore(maksdatoForeldrepenger.get())) {
                return false; // stønadsperioden er ikke brukt opp av annen forelder
            }
        }
        return true;
    }

    public MedlemskapsvilkårGrunnlag oversettTilRegelModellMedlemskap(Behandling behandling) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);

        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);

        MedlemskapsvilkårGrunnlag grunnlag = new MedlemskapsvilkårGrunnlag(
            brukerErMedlemEllerIkkeRelevantPeriode(medlemskap, behandling, personopplysninger), // FP VK 2.13
            tilPersonStatusType(personopplysninger), // FP VK 2.1
            brukerNorskNordisk(personopplysninger), // FP VK 2.11
            brukerBorgerAvEOS(vurdertMedlemskap, personopplysninger), // FP VIK 2.12
            sjekkOmSaksbehandlerHarSattOophørtIMedlemskapPgaEndringerITps(vurdertMedlemskap));

        grunnlag.setHarSøkerArbeidsforholdOgInntekt(finnOmSøkerHarArbeidsforholdOgInntekt(behandling));

        // defaulter uavklarte fakta til true
        grunnlag.setBrukerAvklartLovligOppholdINorge(
            vurdertMedlemskap.map(VurdertMedlemskap::getLovligOppholdVurdering).orElse(true));
        grunnlag.setBrukerAvklartBosatt(
            vurdertMedlemskap.map(VurdertMedlemskap::getBosattVurdering).orElse(true));
        grunnlag.setBrukerAvklartOppholdsrett(
            vurdertMedlemskap.map(VurdertMedlemskap::getOppholdsrettVurdering).orElse(true));

        // FP VK 2.2 Er bruker avklart som pliktig eller frivillig medlem?
        grunnlag.setBrukerAvklartPliktigEllerFrivillig(erAvklartSomPliktigEllerFrivillingMedlem(behandling, medlemskap));

        return grunnlag;
    }

    private boolean finnOmSøkerHarArbeidsforholdOgInntekt(Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, skjæringstidspunkt);

        if (inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOptional.get();
            Optional<AktørArbeid> søkersArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(behandling.getAktørId());
            if (!søkersArbeid.isPresent()) {
                return false;
            }

            Optional<List<Arbeidsgiver>> arbeidsgivere = finnRelevanteArbeidsgivereMedLøpendeAvtaleEllerAvtaleSomErGyldigPåStp(skjæringstidspunkt, søkersArbeid.get());
            if (!arbeidsgivere.isPresent()) {
                return false;
            }

            Optional<AktørInntekt> aktørInntekt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(behandling.getAktørId());
            if (!aktørInntekt.isPresent()) {
                return false;
            }

            return sjekkOmGjelderRelevantArbeidsgiver(aktørInntekt.get(), arbeidsgivere.get());
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

    private boolean sjekkOmGjelderRelevantArbeidsgiver(AktørInntekt inntekt, List<Arbeidsgiver> aktørArbeid) {
        return inntekt.getInntektPensjonsgivende().stream()
            .anyMatch(e -> aktørArbeid.contains(e.getArbeidsgiver()));
    }

    private boolean sjekkOmSaksbehandlerHarSattOophørtIMedlemskapPgaEndringerITps(Optional<VurdertMedlemskap> vurdertMedlemskap) {
        if (vurdertMedlemskap.isPresent()) {
            if (MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS.equals(vurdertMedlemskap.get().getMedlemsperiodeManuellVurdering())) {
                return true;
            }
        }
        return false;
    }

    /**
     * True dersom saksbehandler har vurdert til å være medlem i relevant periode
     */
    private boolean erAvklartSomPliktigEllerFrivillingMedlem(Behandling behandling, Optional<MedlemskapAggregat> medlemskap) {
        if (medlemskap.isPresent()) {
            Optional<VurdertMedlemskap> vurdertMedlemskapOpt = medlemskap.get().getVurdertMedlemskap();
            if (vurdertMedlemskapOpt.isPresent()) {
                VurdertMedlemskap vurdertMedlemskap = vurdertMedlemskapOpt.get();
                if (vurdertMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
                    MedlemskapManuellVurderingType.MEDLEM.equals(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())) {
                    return true;
                }
                if (vurdertMedlemskap.getMedlemsperiodeManuellVurdering() != null &&
                    MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertMedlemskap.getMedlemsperiodeManuellVurdering())) {
                    return false;
                }
            }
            return medlemskapPerioderTjeneste.brukerMaskineltAvklartSomFrivilligEllerPliktigMedlem(behandling, familieGrunnlagRepository.hentAggregat(behandling),
                medlemskap.map(MedlemskapAggregat::getRegistrertMedlemskapPerioder).orElse(Collections.emptySet()));

        } else {
            return false;
        }
    }

    /**
     * True dersom saksbehandler har vurdert til ikke å være medlem i relevant periode
     */
    private boolean erAvklartSomIkkeMedlem(Optional<VurdertMedlemskap> medlemskap) {
        return medlemskap.isPresent() && medlemskap.get().getMedlemsperiodeManuellVurdering() != null
            && MedlemskapManuellVurderingType.UNNTAK.equals(medlemskap.get().getMedlemsperiodeManuellVurdering());
    }

    private boolean brukerErMedlemEllerIkkeRelevantPeriode(Optional<MedlemskapAggregat> medlemskap, Behandling behandling,
                                                           PersonopplysningerAggregat søker) {
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);
        if (vurdertMedlemskap.isPresent()
            && MedlemskapManuellVurderingType.IKKE_RELEVANT.equals(vurdertMedlemskap.get().getMedlemsperiodeManuellVurdering())) {
            return true;
        }

        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = medlemskap.isPresent() ? medlemskap.get().getRegistrertMedlemskapPerioder()
            : Collections.emptySet();
        boolean erAvklartMaskineltSomIkkeMedlem = medlemskapPerioderTjeneste.brukerMaskineltAvklartSomIkkeMedlem(behandling, søker,
            familieGrunnlagRepository.hentAggregat(behandling), medlemskapPerioder);
        boolean erAvklartManueltSomIkkeMedlem = erAvklartSomIkkeMedlem(vurdertMedlemskap);

        return !(erAvklartMaskineltSomIkkeMedlem || erAvklartManueltSomIkkeMedlem);
    }

    private boolean brukerBorgerAvEOS(Optional<VurdertMedlemskap> medlemskap, PersonopplysningerAggregat aggregat) {
        // Tar det første for det er det som er prioritert høyest rangert på region
        final Optional<Statsborgerskap> statsborgerskap = aggregat.getStatsborgerskapFor(aggregat.getSøker().getAktørId()).stream().findFirst();
        return medlemskap
            .map(VurdertMedlemskap::getErEøsBorger)
            .orElse(Region.EOS.equals(statsborgerskap.map(Statsborgerskap::getRegion).orElse(null)));
    }

    private boolean brukerNorskNordisk(PersonopplysningerAggregat aggregat) {
        // Tar det første for det er det som er prioritert høyest rangert på region
        final Optional<Statsborgerskap> statsborgerskap = aggregat.getStatsborgerskapFor(aggregat.getSøker().getAktørId()).stream().findFirst();
        return Region.NORDEN.equals(statsborgerskap.map(Statsborgerskap::getRegion).orElse(null));
    }

    private PersonStatusType tilPersonStatusType(PersonopplysningerAggregat personopplysninger) {
        // Bruker overstyrt personstatus hvis det finnes
        PersonstatusType type = Optional.ofNullable(personopplysninger.getPersonstatusFor(personopplysninger.getSøker().getAktørId())).map(Personstatus::getPersonstatus).orElse(null);

        if (PersonstatusType.BOSA.equals(type)) {
            return PersonStatusType.BOSA;
        } else if (PersonstatusType.UTVA.equals(type)) {
            return PersonStatusType.UTVA;
        } else if (PersonstatusType.DØD.equals(type)) {
            return PersonStatusType.DØD;
        }

        return null;
    }

    private BekreftetAdopsjon byggBekreftetAdopsjon(Behandling behandling) {
        final Optional<FamilieHendelse> bekreftetVersjon = familieGrunnlagRepository.hentAggregat(behandling).getGjeldendeBekreftetVersjon();
        final Optional<Adopsjon> adopsjon = bekreftetVersjon.flatMap(FamilieHendelse::getAdopsjon);

        if (!adopsjon.isPresent()) {
            throw FEILFACTORY.kanIkkeOversetteAdopsjonsgrunnlag(String.valueOf(behandling.getId())).toException();
        }
        if (!bekreftetVersjon.isPresent()) {
            throw FEILFACTORY.kanIkkeOversetteAdopsjonsgrunnlag(String.valueOf(behandling.getId())).toException();
        }

        List<BekreftetAdopsjonBarn> bekreftetAdopsjonBarn = bekreftetVersjon.get().getBarna().stream()
            .map(barn -> new BekreftetAdopsjonBarn(barn.getFødselsdato()))
            .collect(toList());
        BekreftetAdopsjon bekreftetAdopsjon = new BekreftetAdopsjon(adopsjon.get().getOmsorgsovertakelseDato(), bekreftetAdopsjonBarn);
        bekreftetAdopsjon.setAdoptererAlene(getBooleanOrDefaultFalse(adopsjon.get().getAdoptererAlene()));
        bekreftetAdopsjon.setEktefellesBarn(getBooleanOrDefaultFalse(adopsjon.get().getErEktefellesBarn()));
        return bekreftetAdopsjon;
    }

    private boolean getBooleanOrDefaultFalse(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool;
    }

    private Kjoenn tilSoekerKjoenn(NavBrukerKjønn søkerKjønn) {
        Kjoenn kjoenn = Kjoenn.hentKjoenn(søkerKjønn.getKode());
        Objects.requireNonNull(kjoenn, "Fant ingen kjonn for: " + søkerKjønn.getKode());
        return kjoenn;
    }

    public OpptjeningsperiodeGrunnlag oversettTilRegelModellOpptjeningsperiode(Behandling behandling) {

        OpptjeningsperiodeGrunnlag grunnlag = new OpptjeningsperiodeGrunnlag();

        final FamilieHendelseGrunnlag hendelseAggregat = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse hendelse = hendelseAggregat.getGjeldendeVersjon();
        final FamilieHendelseType hendelseType = hendelse.getType();

        grunnlag.setFagsakÅrsak(finnFagsakÅrsak(behandling));
        grunnlag.setSøkerRolle(finnFagsakSøkerRolle(behandling));
        beregnMorsMaksdatoTjeneste.beregnMorsMaksdato(behandling).ifPresent(grunnlag::setMorsMaksdato);
        if (grunnlag.getFagsakÅrsak() == null || grunnlag.getSøkerRolle() == null) {
            throw new IllegalArgumentException("Utvikler-feil: Finner ikke årsak/rolle for behandling:" + behandling.getId());
        }

        if (grunnlag.getFagsakÅrsak().equals(FagsakÅrsak.FØDSEL)) {
            if (hendelse.getTerminbekreftelse().isPresent()) {
                grunnlag.setTerminDato(hendelse.getTerminbekreftelse().get().getTermindato());
            } else if (hendelseAggregat.getGjeldendeTerminbekreftelse().isPresent()) {
                grunnlag.setTerminDato(hendelseAggregat.getGjeldendeTerminbekreftelse().get().getTermindato());
            }
            grunnlag.setHendelsesDato(hendelseAggregat.finnGjeldendeFødselsdato());
        } else {
            if (hendelseType.equals(FamilieHendelseType.ADOPSJON) || hendelseType.equals(FamilieHendelseType.OMSORG)) {
                hendelse.getAdopsjon().ifPresent(adopsjon1 -> grunnlag.setHendelsesDato(adopsjon1.getOmsorgsovertakelseDato()));
            }
        }
        if (grunnlag.getHendelsesDato() == null) {
            throw new IllegalArgumentException("Utvikler-feil: Finner ikke hendelsesdato for behandling:" + behandling.getId());
        }

        grunnlag.setFørsteUttaksDato(skjæringstidspunktTjeneste.førsteUttaksdag(behandling));

        return grunnlag;
    }

    // TODO(Termitt): Håndtere MMOR, SAMB mm.
    private SoekerRolle finnFagsakSøkerRolle(Behandling behandling) {
        RelasjonsRolleType relasjonsRolleType = behandling.getRelasjonsRolleType();
        if (RelasjonsRolleType.MORA.equals(relasjonsRolleType)) {
            return SoekerRolle.MORA;
        }
        if (RelasjonsRolleType.UDEFINERT.equals(relasjonsRolleType) || RelasjonsRolleType.BARN.equals(relasjonsRolleType)) {
            return null;
        }
        return SoekerRolle.FARA;
    }

    private FagsakÅrsak finnFagsakÅrsak(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelseType type = familieHendelseGrunnlag.getGjeldendeVersjon().getType();
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getGjelderFødsel()) {
            return FagsakÅrsak.FØDSEL;
        } else if (FamilieHendelseType.ADOPSJON.equals(type)) {
            return FagsakÅrsak.ADOPSJON;
        } else if (FamilieHendelseType.OMSORG.equals(type)) {
            return FagsakÅrsak.OMSORG;
        }
        return null;
    }

    public VilkårData tilVilkårData(VilkårType vilkårType, Evaluation evaluation, VilkårGrunnlag grunnlag) {
        return new VilkårUtfallOversetter(kodeverkRepository).oversett(vilkårType, evaluation, grunnlag);
    }
}
