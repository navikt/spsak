package no.nav.foreldrepenger.domene.mottak.sakogenhet.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.mottak.HendelserFeil;
import no.nav.foreldrepenger.domene.mottak.sakogenhet.KobleSakTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class KobleSakTjenesteImpl implements KobleSakTjeneste {

    private Logger logger = LoggerFactory.getLogger(KobleSakTjenesteImpl.class);

    private TpsTjeneste tpsTjeneste;
    private PersonopplysningRepository personopplysningRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;

    KobleSakTjenesteImpl() {
        //For CDI
    }

    @Inject
    public KobleSakTjenesteImpl(BehandlingRepositoryProvider provider, TpsTjeneste tpsTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.personopplysningRepository = provider.getPersonopplysningRepository();
        this.familieHendelseRepository = provider.getFamilieGrunnlagRepository();
        this.fagsakRepository = provider.getFagsakRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.fagsakRelasjonRepository = provider.getFagsakRelasjonRepository();
        this.ytelsesFordelingRepository = provider.getYtelsesFordelingRepository();
    }

    @Override
    public Optional<Fagsak> finnRelatertFagsakDersomRelevant(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();
        FagsakRelasjon fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(fagsak).orElse(null);
        if (fagsakRelasjon != null && fagsakRelasjon.getFagsakNrTo().isPresent()) {
            HendelserFeil.FACTORY.fagsakAlleredeKoblet(fagsakRelasjon.getFagsakNrEn().getId(), fagsakRelasjon.getFagsakNrTo().get().getId()).log(logger);
            return Optional.empty();
        }
        Personinfo bruker = tpsTjeneste.hentBrukerForAktør(fagsak.getAktørId()).orElse(null);

        Optional<FamilieHendelse> familieHendelseOptional = familieHendelseRepository.hentAggregatHvisEksisterer(behandling)
            .map(FamilieHendelseGrunnlag::getGjeldendeVersjon);
        if (!familieHendelseOptional.isPresent() || harSammeType(familieHendelseOptional.get(), FamilieHendelseType.UDEFINERT)
            || familieHendelseOptional.get().getSkjæringstidspunkt() == null || bruker == null) {
            HendelserFeil.FACTORY.familiehendelseUtenDato(fagsak.getId()).log(logger);
            return Optional.empty();
        }
        FamilieHendelse familieHendelse = familieHendelseOptional.get();

        List<Personinfo> barnFraTps = finnEvtBarnRundtAngittDatoTps(bruker, familieHendelse);

        List<Fagsak> aktuelleFagsaker = utledRelevanteFagsakerFraTpsEllerTermin(behandling, bruker, barnFraTps);
        if (aktuelleFagsaker.isEmpty()) {
            // Helt greit. Skal ikke koble.
            return Optional.empty();
        }

        // Finn behandlinger som omhandler samme kull - felles familiehendelse
        List<Behandling> aktuelleBehandlinger = utledAktuelleBehandlingerForAnnenPart(aktuelleFagsaker, familieHendelse, barnFraTps);

        // Utled fagsaker som er kandidat for kobling
        List<Fagsak> filtrert = new ArrayList<>();
        aktuelleBehandlinger.forEach(beh -> {
            if (!filtrert.contains(beh.getFagsak())) {
                filtrert.add(beh.getFagsak());
            }
        });

        if (filtrert.isEmpty()) {
            return Optional.empty();
        } else if (filtrert.size() > 1) {
            HendelserFeil.FACTORY.flereMuligeFagsakerÅKobleTil(fagsak.getId()).log(logger);
            return Optional.empty();
        }
        return Optional.of(filtrert.get(0));
    }

    @Override
    public Optional<FagsakRelasjon> finnFagsakRelasjonDersomOpprettet(Behandling behandling) {
        return fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak());
    }

    @Override
    public void kobleRelatertFagsakHvisDetFinnesEn(Behandling behandling) {
        final Optional<FagsakRelasjon> eksisterendeRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak());
        if (eksisterendeRelasjon.isPresent() && eksisterendeRelasjon.get().getFagsakNrTo().isPresent()) {
            return;
        }
        final Optional<Fagsak> potensiellFagsak = finnRelatertFagsakDersomRelevant(behandling);
        if (potensiellFagsak.isPresent()) {
            fagsakRelasjonRepository.kobleFagsaker(potensiellFagsak.get(), behandling.getFagsak());
        } else if (behandling.getFagsakYtelseType().gjelderForeldrepenger() && !eksisterendeRelasjon.isPresent()) {
            // Oppretter en fagsaksrelasjon hvis fagsaken er av typen foreldrepenger
            YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
            fagsakRelasjonRepository.opprettRelasjon(behandling.getFagsak(), Dekningsgrad.grad(ytelseFordelingAggregat.getOppgittDekningsgrad().getDekningsgrad()));
        }
    }

    private List<Fagsak> utledRelevanteFagsakerFraTpsEllerTermin(Behandling behandling, Personinfo bruker, List<Personinfo> barnFraTps) {

        if (!barnFraTps.isEmpty()) {
            return aktuelleFagsakerForBruker(behandling, fastsettAnnenPartFraTPS(behandling.getFagsakId(), bruker, barnFraTps).orElse(null));
        } else {
            // Termin eller Adopsjon/Omsorg på skjæringsdato og begge søkere har oppgitt hverandre
            AktørId oppgittAnnenPart = personopplysningRepository.hentPersonopplysningerHvisEksisterer(behandling)
                .flatMap(PersonopplysningGrunnlag::getOppgittAnnenPart)
                .map(OppgittAnnenPart::getAktørId).orElse(null);

            if (oppgittAnnenPart == null || oppgittAnnenPart.equals(behandling.getAktørId())) {
                // Ikke oppgitt medsøker eller medsøker ikke i TPS eller oppgitt seg selv
                return Collections.emptyList();
            }

            List<Behandling> muligeBehandlinger = new ArrayList<>();
            // Velg ut annen parts fagsaker der annen part har oppgitt bruker som annen part
            aktuelleFagsakerForBruker(behandling, oppgittAnnenPart).forEach(sak ->
                behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(sak.getSaksnummer())
                    .forEach(aktuellBehandling -> leggTilBehandlingHvisOppgittPartIkkeErEnAnnenPart(behandling.getAktørId(), muligeBehandlinger, aktuellBehandling)));
            final List<Fagsak> fagsakerAnnenPart = new ArrayList<>();

            muligeBehandlinger.forEach(beh -> {
                if (!fagsakerAnnenPart.contains(beh.getFagsak())) {
                    fagsakerAnnenPart.add(beh.getFagsak());
                }
            });
            return fagsakerAnnenPart;
        }
    }


    private List<Behandling> utledAktuelleBehandlingerForAnnenPart(List<Fagsak> aktuelleFagsaker, FamilieHendelse familieHendelse, List<Personinfo> barnFraTps) {
        List<Behandling> aktuelleBehandlinger = new ArrayList<>();
        aktuelleFagsaker.forEach(fs -> behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fs.getSaksnummer())
            .forEach(behandling1 -> familieHendelseRepository.hentAggregatHvisEksisterer(behandling1).map(FamilieHendelseGrunnlag::getGjeldendeVersjon)
                .ifPresent(gjeldendeVersjon -> {
                    if (matcher(familieHendelse, barnFraTps, gjeldendeVersjon)) {
                        aktuelleBehandlinger.add(behandling1);
                    }
                })));
        return aktuelleBehandlinger;
    }

    private boolean matcher(FamilieHendelse nåværende, List<Personinfo> barnFraTps, FamilieHendelse aktuell) {
        if (harSammeType(aktuell, nåværende.getType()) && nåværende.getGjelderFødsel()) {
            long intervall = harSammeType(nåværende, FamilieHendelseType.FØDSEL) ? 1L : 14L;
            return overlapperHendelserFor(nåværende.getSkjæringstidspunkt(), intervall + 1, aktuell.getSkjæringstidspunkt());
        } else if (nåværende.getGjelderFødsel() && !barnFraTps.isEmpty() && aktuell.getGjelderFødsel()) {
            final LocalDate fødselsdato = barnFraTps.get(0).getFødselsdato();
            return overlapperHendelserFor(fødselsdato, 2, aktuell.getSkjæringstidspunkt());
        } else if (harSammeType(aktuell, nåværende.getType()) && nåværende.getGjelderAdopsjon()) {
            return matcherBarn(nåværende, aktuell);
        } else if (gjelderStebarnsadopsjon(nåværende, aktuell) || gjelderStebarnsadopsjon(aktuell, nåværende)) {
            return matcherBarn(nåværende, aktuell);
        }
        return false;
    }

    private boolean gjelderStebarnsadopsjon(FamilieHendelse nåværende, FamilieHendelse aktuell) {
        return nåværende.getGjelderFødsel() && aktuell.getGjelderAdopsjon();
    }

    private boolean harSammeType(FamilieHendelse aktuell, FamilieHendelseType type) {
        return type.equals(aktuell.getType());
    }

    private boolean overlapperHendelserFor(LocalDate skjæringstidspunkt, long l, LocalDate skjæringstidspunkt2) {
        if (skjæringstidspunkt == null || skjæringstidspunkt2 == null) {
            return false;
        }
        return skjæringstidspunkt.minusDays(l).isBefore(skjæringstidspunkt2)
            && skjæringstidspunkt.plusDays(l).isAfter(skjæringstidspunkt2);
    }

    private boolean matcherBarn(FamilieHendelse nåværende, FamilieHendelse aktuell) {
        if (nåværende.getAntallBarn().equals(aktuell.getAntallBarn())) {
            final Set<LocalDate> nåværendeBarn = nåværende.getBarna().stream().map(UidentifisertBarn::getFødselsdato).collect(Collectors.toSet());
            final Set<LocalDate> aktuelleBarn = aktuell.getBarna().stream().map(UidentifisertBarn::getFødselsdato).collect(Collectors.toSet());

            for (LocalDate fødselsDato : aktuelleBarn) {
                boolean fant = fantBarn(nåværendeBarn, fødselsDato);
                if (!fant) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean fantBarn(Set<LocalDate> nåværende, LocalDate fødselsDato) {
        return nåværende.stream().anyMatch(it -> overlapperHendelserFor(it, 2, fødselsDato));
    }

    private void leggTilBehandlingHvisOppgittPartIkkeErEnAnnenPart(AktørId aktørId, List<Behandling> muligeBehandlinger, Behandling behandling1) {
        final Optional<AktørId> annenPartAktørId = personopplysningRepository.hentPersonopplysningerHvisEksisterer(behandling1)
            .flatMap(PersonopplysningGrunnlag::getOppgittAnnenPart)
            .map(OppgittAnnenPart::getAktørId);
        if (annenPartAktørId.isPresent()) {
            if (aktørId.equals(annenPartAktørId.get())) {
                muligeBehandlinger.add(behandling1);
            }
        } else {
            // Sjekke om barnet ikke har relasjoner til andre enn aktør
            Personinfo bruker = tpsTjeneste.hentBrukerForAktør(behandling1.getAktørId()).orElse(null);
            if (bruker != null) {
                final Optional<FamilieHendelseGrunnlag> familieHendelseGrunnlag = familieHendelseRepository.hentAggregatHvisEksisterer(behandling1);
                if (familieHendelseGrunnlag.isPresent()) {
                    final List<Personinfo> barn = finnEvtBarnRundtAngittDatoTps(bruker, familieHendelseGrunnlag.get().getGjeldendeVersjon());
                    final Set<PersonIdent> collect = barn.stream()
                        .map(Personinfo::getFamilierelasjoner)
                        .flatMap(Collection::stream)
                        .map(Familierelasjon::getPersonIdent)
                        .filter(it -> !it.equals(bruker.getPersonIdent()))
                        .collect(Collectors.toSet());
                    if (collect.isEmpty()) {
                        muligeBehandlinger.add(behandling1);
                    }
                }
            }
        }
    }

    private List<Fagsak> aktuelleFagsakerForBruker(Behandling behandling, AktørId annenPartAktørId) {
        if (annenPartAktørId == null) {
            return Collections.emptyList();
        }
        return fagsakRepository.hentForBruker(annenPartAktørId).stream()
            .filter(fa -> fa.getYtelseType().equals(behandling.getFagsakYtelseType()))
            .filter(sak -> !(fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak())
                .flatMap(FagsakRelasjon::getFagsakNrTo).isPresent()))
            .collect(Collectors.toList());
    }

    private Optional<AktørId> fastsettAnnenPartFraTPS(Long fagsakId, Personinfo bruker, List<Personinfo> barn) {
        Set<PersonIdent> annenForelderFraTPSBarn = new HashSet<>();
        barn.forEach(barnet -> annenForelderFraTPSBarn.addAll(barnet.getFamilierelasjoner().stream()
            .map(Familierelasjon::getPersonIdent)
            .filter(personIdent -> !personIdent.equals(bruker.getPersonIdent()))
            .collect(Collectors.toSet())));

        if (annenForelderFraTPSBarn.isEmpty() || annenForelderFraTPSBarn.size() > 1) {
            // Barnet har ikke annen forelder eller multiple foreldre
            HendelserFeil.FACTORY.håndtererIkkeAnnenForeldre(fagsakId).log(logger);
            return Optional.empty();
        }
        return tpsTjeneste.hentAktørForFnr(annenForelderFraTPSBarn.iterator().next());
    }

    private List<Personinfo> finnEvtBarnRundtAngittDatoTps(Personinfo bruker, FamilieHendelse familieHendelse) {
        if (bruker == null) {
            return Collections.emptyList();
        }
        LocalDate aktuellDato = fødselsDatoFraFamilieHendelse(familieHendelse).orElse(null);
        if (aktuellDato == null) {
            return Collections.emptyList();
        }
        return bruker.getFamilierelasjoner().stream()
            .filter(fr -> RelasjonsRolleType.BARN.equals(fr.getRelasjonsrolle()))
            .map(fr -> tpsTjeneste.hentBrukerForFnr(fr.getPersonIdent()).orElse(null))
            .filter(Objects::nonNull)
            .filter(pi -> overlapperHendelserFor(aktuellDato, 2, pi.getFødselsdato()))
            .collect(Collectors.toList());
    }

    private Optional<LocalDate> fødselsDatoFraFamilieHendelse(FamilieHendelse familieHendelse) {
        if (harSammeType(familieHendelse, FamilieHendelseType.TERMIN)
            || harSammeType(familieHendelse, FamilieHendelseType.FØDSEL)) {
            return Optional.of(familieHendelse.getSkjæringstidspunkt());
        } else if (harSammeType(familieHendelse, FamilieHendelseType.ADOPSJON)
            || harSammeType(familieHendelse, FamilieHendelseType.OMSORG)) {
            return familieHendelse.getAdopsjon().isPresent() ? familieHendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst() : Optional.empty();
        }
        return Optional.empty();
    }
}
