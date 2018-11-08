package no.nav.foreldrepenger.domene.familiehendelse.impl;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.familiehendelse.AvklarManglendeFødselAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftAdopsjonsAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.BekreftDokumentasjonAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.TerminbekreftelseAksjonspunktDto;
import no.nav.foreldrepenger.domene.familiehendelse.VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger;
import no.nav.foreldrepenger.domene.familiehendelse.adopsjon.BekreftMannAdoptererAksjonspunkt;
import no.nav.foreldrepenger.domene.familiehendelse.fødsel.AvklarManglendeFødselAksjonspunkt;
import no.nav.foreldrepenger.domene.familiehendelse.fødsel.BekreftTerminbekreftelseAksjonspunkt;
import no.nav.foreldrepenger.domene.familiehendelse.sykdom.AksjonspunktVurderingAvVilkårForMorsSykdomVedFødselForForeldrepenger;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class FamilieHendelseTjenesteImpl implements FamilieHendelseTjeneste {

    private int antallUkerFomTerminSøknadsfristStart;
    private int antallUkerTomTerminSøknadsfristSlutt;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private BehandlingRepositoryProvider repositoryProvider;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;

    FamilieHendelseTjenesteImpl() {
        // CDI
    }

    @Inject
    public FamilieHendelseTjenesteImpl(BasisPersonopplysningTjeneste personopplysningTjeneste,
                                       @KonfigVerdi(value = "søknad.uker.fom.termin.søknadsfrist.start") int antallUkerFomTerminSøknadsfristStart,
                                       @KonfigVerdi(value = "søknad.uker.tom.termin.søknadsfrist.slutt") int antallUkerTomTerminSøknadsfristSlutt,
                                       BehandlingRepositoryProvider repositoryProvider) {

        this.personopplysningTjeneste = personopplysningTjeneste;
        this.antallUkerFomTerminSøknadsfristStart = antallUkerFomTerminSøknadsfristStart;
        this.antallUkerTomTerminSøknadsfristSlutt = antallUkerTomTerminSøknadsfristSlutt;
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public List<Interval> beregnGyldigeFødselsperioder(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse søknadVersjon = familieHendelseGrunnlag.getSøknadVersjon();
        Optional<LocalDate> fødselsdato = søknadVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
        Optional<LocalDate> termindato = søknadVersjon.getTerminbekreftelse().map(Terminbekreftelse::getTermindato);
        List<LocalDate> adopsjonFødselsdatoer = søknadVersjon.getBarna().stream()
            .map(UidentifisertBarn::getFødselsdato).collect(toList());

        if (fødselsdato.isPresent() && søknadVersjon.getType().equals(FamilieHendelseType.FØDSEL)) {
            return Collections.singletonList(IntervallUtil.byggIntervall(fødselsdato.get().minusDays(1), fødselsdato.get().plusDays(1)));
        }
        if (termindato.isPresent()) {
            return Collections.singletonList(IntervallUtil.byggIntervall(termindato.get().minusWeeks(antallUkerFomTerminSøknadsfristStart),
                termindato.get().plusWeeks(antallUkerTomTerminSøknadsfristSlutt)));
        }
        if (adopsjonFødselsdatoer != null && søknadVersjon.getType().equals(FamilieHendelseType.ADOPSJON)) {
            return adopsjonFødselsdatoer.stream()
                .map(dato -> IntervallUtil.byggIntervall(dato, dato))
                .collect(toList());
        }

        // Ikke mulig å beregne gyldig fødselsperiode; returner tom liste
        return Collections.emptyList();
    }

    @Override
    public void oppdaterFødselPåGrunnlag(Behandling behandling, List<FødtBarnInfo> bekreftetTps) {
        if (bekreftetTps.isEmpty()) {
            return;
        }
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        final FamilieHendelseBuilder hendelseBuilder = familieGrunnlagRepository.opprettBuilderFor(behandling)
            .tilbakestillBarn();

        bekreftetTps.forEach(barn -> hendelseBuilder.leggTilBarn(barn.getFødselsdato(), barn.getDødsdato().orElse(null)));
        hendelseBuilder.medAntallBarn(bekreftetTps.size());

        familieGrunnlagRepository.lagreRegisterHendelse(behandling, hendelseBuilder);
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås); // TODO: Trenger vi denne nå?
    }

    @Override
    public boolean erUtstedtdatoEllerTermindatoEndret(Behandling behandling, TerminbekreftelseAksjonspunktDto adapter) {
        final FamilieHendelseGrunnlag grunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        boolean erEndret = false;
        Optional<LocalDate> orginalTermindato = grunnlag.getGjeldendeTerminbekreftelse().map(Terminbekreftelse::getTermindato);
        if (orginalTermindato.isPresent()) {
            erEndret = !Objects.equals(orginalTermindato.get(), adapter.getTermindato());
        }

        Optional<LocalDate> orginalUtstedtDato = grunnlag.getGjeldendeTerminbekreftelse().map(Terminbekreftelse::getUtstedtdato);
        if (orginalUtstedtDato.isPresent()) {
            erEndret = !Objects.equals(orginalUtstedtDato.get(), adapter.getUtstedtdato()) || erEndret;
        }
        return erEndret;
    }

    @Override
    public FamilieHendelseGrunnlag hentAggregat(Behandling behandling) {
        return familieGrunnlagRepository.hentAggregat(behandling);
    }

    @Override
    public Optional<FamilieHendelseGrunnlag> finnAggregat(Behandling behandling) {
        return familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
    }

    @Override
    public void aksjonspunktBekreftTerminbekreftelse(Behandling behandling, TerminbekreftelseAksjonspunktDto adapter) {
        new BekreftTerminbekreftelseAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktBekreftMannAdopterer(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter) {
        new BekreftMannAdoptererAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktBekreftEktefellesBarn(Behandling behandling, BekreftAdopsjonsAksjonspunktDto adapter) {
        new BekreftEktefelleAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktAvklarManglendeFødsel(Behandling behandling, AvklarManglendeFødselAksjonspunktDto adapter) {
        new AvklarManglendeFødselAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktBekreftDokumentasjon(Behandling behandling, BekreftDokumentasjonAksjonspunktDto adapter) {
        new BekreftDokumentasjonAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktVurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger(Behandling behandling, VurderingAvVilkårForMorsSyksomVedFødselForForeldrepenger syksomVedFødselForForeldrepenger) {
        new AksjonspunktVurderingAvVilkårForMorsSykdomVedFødselForForeldrepenger(repositoryProvider).oppdater(behandling, syksomVedFødselForForeldrepenger);
    }

    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling) {
        Optional<Long> funnetId = familieGrunnlagRepository.hentIdPåAktivFamiliehendelse(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(FamilieHendelseGrunnlag.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(FamilieHendelseGrunnlag.class));
    }

    @Override
    public DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer) {
        FamilieHendelseGrunnlag grunnlag1 = familieGrunnlagRepository.hentFamilieHendelserPåId(idDiff.getGrunnlagId1());
        FamilieHendelseGrunnlag grunnlag2 = familieGrunnlagRepository.hentFamilieHendelserPåId(idDiff.getGrunnlagId2());
        return familieGrunnlagRepository.diffResultat(grunnlag1, grunnlag2, ytelseType, kunSporedeEndringer);
    }

    @Override
    public Optional<FamilieHendelseGrunnlag> hentFamilieHendelseGrunnlagForBehandling(Behandling behandling) {
        Optional<Behandling> forrigeOpt = repositoryProvider.getBehandlingRepository().hentSisteBehandlingForFagsakId(behandling.getFagsakId(), BehandlingType.FØRSTEGANGSSØKNAD);
        Optional<FamilieHendelseGrunnlag> familieGrunnlagAggregatOpt = repositoryProvider.getFamilieGrunnlagRepository().hentAggregatHvisEksisterer(behandling);
        if (familieGrunnlagAggregatOpt.isPresent()) {
            return familieGrunnlagAggregatOpt;
        } else {
            if (forrigeOpt.isPresent()){
                return  repositoryProvider.getFamilieGrunnlagRepository().
                    hentAggregatHvisEksisterer(forrigeOpt.get());
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<Boolean> gjelderFødsel(Behandling behandling) {
        Optional<FamilieHendelseGrunnlag> familieGrunnlagAggregatOpt = hentFamilieHendelseGrunnlagForBehandling(behandling);
        return familieGrunnlagAggregatOpt
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon)
            .map(FamilieHendelse::getGjelderFødsel);
    }

    @Override
    public List<Personopplysning> finnBarnSøktStønadFor(Behandling behandling) {
        List<Interval> fødselsintervall =this.beregnGyldigeFødselsperioder(behandling);
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);

        return personopplysninger.getRelasjoner().stream()
            .filter(rel -> rel.getAktørId().equals(behandling.getAktørId()) && rel.getRelasjonsrolle().equals(RelasjonsRolleType.BARN))
            .map(rel -> personopplysninger.getPersonopplysninger().stream().filter(person -> person.getAktørId().equals(rel.getTilAktørId())).findAny().orElse(null))
            .filter(barn -> barn != null && erBarnRelatertTilSøknad(fødselsintervall, barn.getFødselsdato()))
            .collect(toList());
    }

    private boolean erBarnRelatertTilSøknad(List<Interval> relasjonsintervall, LocalDate dato) {
        return relasjonsintervall.stream()
            .anyMatch(periode -> periode.overlaps(IntervallUtil.tilIntervall(dato)));
    }
}
