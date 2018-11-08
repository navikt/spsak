package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.vedtak.konfig.KonfigVerdi;

/**
 * Aksjonspunkter for Manuell kontroll av om bruker har Omsorg
 */
@ApplicationScoped
public class AksjonspunktUtlederForOmsorg implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();

    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;
    private int antallUkerForbeholdtMorEtterFødsel;

    AksjonspunktUtlederForOmsorg() {
    }

    @Inject
    public AksjonspunktUtlederForOmsorg(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                        PersonopplysningTjeneste personopplysningTjeneste,
                                        @KonfigVerdi("antall.uker.forbeholdt.mor.etter.fødsel") int antallUkerForbeholdtMorEtterFødsel) {
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
        this.familieGrunnlagRepository = behandlingRepositoryProvider.getFamilieGrunnlagRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.antallUkerForbeholdtMorEtterFødsel = antallUkerForbeholdtMorEtterFødsel;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse familieHendelse = familieHendelseGrunnlag.getGjeldendeVersjon();
        final FamilieHendelse bekreftetFH = familieHendelseGrunnlag.getBekreftetVersjon().orElse(null);
        final Optional<YtelseFordelingAggregat> ytelseFordelingAggregatOptional = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
        if (!ytelseFordelingAggregatOptional.isPresent()) {
            return INGEN_AKSJONSPUNKTER;
        }

        YtelseFordelingAggregat ytelseFordelingAggregat = ytelseFordelingAggregatOptional.get();
        final PersonopplysningerAggregat personopplysningerAggregat = personopplysningTjeneste.hentPersonopplysninger(behandling);

        if (harOppgittOmsorgTilBarnetIHeleSøknadsperioden(ytelseFordelingAggregat) == Utfall.JA) {
            if (erBarnetFødt(bekreftetFH) == Utfall.JA && harBarnSammeBostedITps(personopplysningerAggregat) == Utfall.NEI) {
                return AksjonspunktResultat.opprettListeForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG);
            }
        } else {
            if (gjelderSøknadenFødsel(familieHendelse) == Utfall.JA) {
                if (erBrukerMor(behandling) == Utfall.NEI ||
                    erSøknadsperiodenLengreEnnAntallUkerForbeholdtMorEtterFødselen(familieHendelse, ytelseFordelingAggregat) == Utfall.JA) {
                    return AksjonspunktResultat.opprettListeForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG);
                }
            } else {
                return AksjonspunktResultat.opprettListeForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG);
            }
        }
        return INGEN_AKSJONSPUNKTER;
    }

    private Utfall harOppgittOmsorgTilBarnetIHeleSøknadsperioden(YtelseFordelingAggregat ytelseFordelingAggregat) {
        Boolean harOmsorgForBarnetIHelePerioden = ytelseFordelingAggregat.getOppgittRettighet().getHarOmsorgForBarnetIHelePerioden();
        Objects.requireNonNull(harOmsorgForBarnetIHelePerioden, "harOmsorgForBarnetIHelePerioden må være sett"); //$NON-NLS-1$
        return harOmsorgForBarnetIHelePerioden ? Utfall.JA : Utfall.NEI;
    }

    private Utfall erBarnetFødt(FamilieHendelse bekreftet) {
        return bekreftet != null && !bekreftet.getBarna().isEmpty() ? Utfall.JA : Utfall.NEI;
    }

    private Utfall erBrukerMor(Behandling behandling) {
        RelasjonsRolleType relasjonsRolleType = behandling.getRelasjonsRolleType();
        return RelasjonsRolleType.MORA.equals(relasjonsRolleType) ? Utfall.JA : Utfall.NEI;
    }

    private Utfall erSøknadsperiodenLengreEnnAntallUkerForbeholdtMorEtterFødselen(FamilieHendelse versjon, YtelseFordelingAggregat ytelseFordelingAggregat) {

        Optional<LocalDate> sisteSøknadsDato = finnSisteSøknadsDato(ytelseFordelingAggregat);

        if (!sisteSøknadsDato.isPresent()) {
            throw new IllegalArgumentException("Fant ikke siste søknads dato");
        }

        LocalDate familiehendelseDato = Stream.of(
            versjon.getFødselsdato(),
            versjon.getTerminbekreftelse().map(Terminbekreftelse::getTermindato)
        )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fant ikke familiehendelsedato"));

        return sisteSøknadsDato.filter(søknadsDato -> søknadsDato.isAfter(familiehendelseDato.plusWeeks(antallUkerForbeholdtMorEtterFødsel).minusDays(1)))
            .map(søknadsDato -> Utfall.JA).orElse(Utfall.NEI);

    }

    private Optional<LocalDate> finnSisteSøknadsDato(YtelseFordelingAggregat ytelseFordelingAggregat) {
        return ytelseFordelingAggregat.getGjeldendeSøknadsperioder()
            .getOppgittePerioder()
            .stream()
            .max(Comparator.comparing(OppgittPeriode::getTom))
            .map(OppgittPeriode::getTom);
    }

    private Utfall gjelderSøknadenFødsel(FamilieHendelse hendelse) {
        return hendelse.getGjelderFødsel() ? Utfall.JA : Utfall.NEI;
    }

    private Utfall harBarnSammeBostedITps(PersonopplysningerAggregat personopplysningerAggregat) {
        Optional<PersonRelasjon> ektefelleRelasjon = personopplysningerAggregat.getRelasjoner().stream()
            .filter(familierelasjon -> familierelasjon.getRelasjonsrolle().equals(RelasjonsRolleType.BARN)).findFirst();
        if (ektefelleRelasjon.isPresent() && ektefelleRelasjon.get().getHarSammeBosted() != null) {
            return ektefelleRelasjon.get().getHarSammeBosted() ? JA : NEI;
        } else {
            return harSammeAdresseSomBarn(personopplysningerAggregat) ? JA : NEI;
        }
    }

    private boolean harSammeAdresseSomBarn(PersonopplysningerAggregat personopplysningerAggregat) {
        if (!personopplysningerAggregat.getEktefelle().isPresent()) {
            return false;
        }

        for (PersonAdresse opplysningAdresseSøker : personopplysningerAggregat.getAdresserFor(personopplysningerAggregat.getSøker().getAktørId())) {
            for (Personopplysning nyPersonopplysning : personopplysningerAggregat.getBarna()) {
                for (PersonAdresse opplysningAdresseAnnenpart : personopplysningerAggregat.getAdresserFor(nyPersonopplysning.getAktørId())) {
                    if (Objects.equals(opplysningAdresseSøker.getAdresselinje1(), opplysningAdresseAnnenpart.getAdresselinje1())
                        && Objects.equals(opplysningAdresseSøker.getPostnummer(), opplysningAdresseAnnenpart.getPostnummer())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
