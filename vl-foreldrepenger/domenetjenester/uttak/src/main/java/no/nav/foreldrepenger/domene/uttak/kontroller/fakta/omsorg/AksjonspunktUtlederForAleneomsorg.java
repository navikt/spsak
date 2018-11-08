package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG;

/**
 * Aksjonspunkter for Manuell kontroll av om bruker har aleneomsorg
 */
@ApplicationScoped
public class AksjonspunktUtlederForAleneomsorg implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private PersonopplysningTjeneste personopplysningTjeneste;

    AksjonspunktUtlederForAleneomsorg() {
    }

    @Inject
    AksjonspunktUtlederForAleneomsorg(BehandlingRepositoryProvider repositoryProvider, PersonopplysningTjeneste personopplysningTjeneste) {
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        final Optional<YtelseFordelingAggregat> ytelseFordelingAggregatOptional = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
        if(!ytelseFordelingAggregatOptional.isPresent()) {
            return INGEN_AKSJONSPUNKTER;
        }
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelseFordelingAggregatOptional.get();

        final PersonopplysningerAggregat personopplysningerAggregat = personopplysningTjeneste.hentPersonopplysninger(behandling);
        final Optional<AktørId> annenPartAktørId = personopplysningerAggregat.getOppgittAnnenPart().map(OppgittAnnenPart::getAktørId);
        final Personopplysning søker = personopplysningerAggregat.getSøker();

        if (harOppgittÅHaAleneomsorg(ytelseFordelingAggregat) == JA) {
            if (harOppgittAndreforelderen(annenPartAktørId) == JA) {
                if(harAnnenforeldreSammeBostedITps(personopplysningerAggregat) == JA) {
                    return opprettListeForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);
                }
            } else {
                if(harSivilstatusGiftITps(søker) == JA && harEktefelleSammeBostedITps(personopplysningerAggregat) == JA) {
                    return opprettListeForAksjonspunkt(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_ALENEOMSORG);
                }
            }
        }
        return INGEN_AKSJONSPUNKTER;
    }

    private Utfall harOppgittÅHaAleneomsorg(YtelseFordelingAggregat ytelseFordelingAggregat) {
        Boolean harAleneomsorgForBarnet = ytelseFordelingAggregat.getOppgittRettighet().getHarAleneomsorgForBarnet();
        Objects.requireNonNull(harAleneomsorgForBarnet, "harAleneomsorgForBarnet må være sett"); //$NON-NLS-1$
        return harAleneomsorgForBarnet ? JA : NEI;
    }

    private Utfall harOppgittAndreforelderen(Optional<AktørId> annenPartAktørId) {
        return annenPartAktørId.isPresent() ? JA : NEI;
    }

    private Utfall harAnnenforeldreSammeBostedITps(PersonopplysningerAggregat personopplysningerAggregat) {
        final Optional<Personopplysning> annenPart = personopplysningerAggregat.getAnnenPart();
        if(annenPart.isPresent()) {
            // ANNEN PART HAR IKKE RELASJON
            return personopplysningerAggregat.søkerHarSammeAdresseSom(annenPart.get().getAktørId(), RelasjonsRolleType.UDEFINERT) ? JA : NEI;
        }
        return NEI;
    }

    private Utfall harSivilstatusGiftITps(Personopplysning søker) {
        return søker.getSivilstand().equals(SivilstandType.GIFT) ? JA : NEI;
    }

    private Utfall harEktefelleSammeBostedITps(PersonopplysningerAggregat personopplysningerAggregat) {
        final Optional<Personopplysning> ektefelle = personopplysningerAggregat.getEktefelle();
        if(ektefelle.isPresent()) {
            return personopplysningerAggregat.søkerHarSammeAdresseSom(ektefelle.get().getAktørId(), RelasjonsRolleType.EKTE) ? JA : NEI;
        }
        return NEI;
    }

}
