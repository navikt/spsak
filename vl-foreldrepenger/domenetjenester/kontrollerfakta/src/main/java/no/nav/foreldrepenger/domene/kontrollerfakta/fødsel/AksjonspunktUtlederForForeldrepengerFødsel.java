package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunktMedCallback;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

/**
 * Aksjonspunkter for søknad om foreldrepenger for fødsel
 */
@ApplicationScoped
public class AksjonspunktUtlederForForeldrepengerFødsel extends AksjonspunktUtlederForFødsel {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();

    AksjonspunktUtlederForForeldrepengerFødsel() {
    }

    @Inject
    public AksjonspunktUtlederForForeldrepengerFødsel(BehandlingRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(repositoryProvider, skjæringstidspunktTjeneste);
    }

    @Override
    protected Utfall gjelderSøknadenForeldrepenger() {
        return JA;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();

        if (RelasjonsRolleType.erMor(fagsak.getRelasjonsRolleType())) {
            return utledAksjonspunkterForMor(behandling);
        }

        return utledAksjonspunkterForFarMedmor(behandling);
    }

    /**
     * Utleder aksjonspunkter for far/medmor som hovedsøker
     */
    private List<AksjonspunktResultat> utledAksjonspunkterForFarMedmor(Behandling behandling) {

        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse søknadVersjon = familieHendelseGrunnlag.getSøknadVersjon();
        final FamilieHendelse bekreftetVersjon = familieHendelseGrunnlag.getBekreftetVersjon().orElse(null);

        if (erFødselenRegistrertITps(bekreftetVersjon) == JA) {
            if (samsvarerAntallBarnISøknadMedAntallBarnITps(søknadVersjon, bekreftetVersjon) == NEI) {
                return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
            }
            return INGEN_AKSJONSPUNKTER;
        } else {
            if (harSøkerOppgittTerminISøknad(søknadVersjon)) {
                if (erDagensDato25DagerEtterOppgittTerminsdato(familieHendelseGrunnlag) == JA) {
                    return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
                } else {
                    List<AksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();
                    aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE));
                    aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT));
                    return aksjonspunktResultater;
                }
            } else {
                if (erDagensDato14DagerEtterOppgittFødselsdato(søknadVersjon) == JA) {
                    return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
                } else {
                    return singletonList(opprettForAksjonspunktMedCallback(AUTO_VENT_PÅ_FØDSELREGISTRERING,
                        ap -> aksjonspunktRepository.setFrist(ap, utledVentefrist(søknadVersjon), Venteårsak.UDEFINERT)
                    ));
                }
            }
        }
    }

    /**
     * Utleder aksjonspunkter for mor som hovedsøker
     */
    private List<AksjonspunktResultat> utledAksjonspunkterForMor(Behandling behandling) {
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregat(behandling);
        final FamilieHendelse søknadVersjon = familieHendelseGrunnlag.getSøknadVersjon();
        final FamilieHendelse bekreftetVersjon = familieHendelseGrunnlag.getBekreftetVersjon().orElse(null);

        if (erFødselenRegistrertITps(bekreftetVersjon) == JA) {
            if (samsvarerAntallBarnISøknadMedAntallBarnITps(søknadVersjon, bekreftetVersjon) == NEI) {
                return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
            }
            return INGEN_AKSJONSPUNKTER;
        } else {
            if (harSøkerOppgittFødselISøknad(søknadVersjon) == NEI) {
                if (erDagensDato25DagerEtterOppgittTerminsdato(familieHendelseGrunnlag) == JA) {
                    return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
                } else {
                    if (erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(behandling) == NEI) {
                        return opprettListeForAksjonspunkt(AVKLAR_TERMINBEKREFTELSE);
                    } else {
                        return INGEN_AKSJONSPUNKTER;
                    }
                }
            } else {
                if (erDagensDato14DagerEtterOppgittFødselsdato(søknadVersjon) == JA) {
                    return opprettListeForAksjonspunkt(SJEKK_MANGLENDE_FØDSEL);
                } else {
                    return singletonList(opprettForAksjonspunktMedCallback(AUTO_VENT_PÅ_FØDSELREGISTRERING,
                        ap -> aksjonspunktRepository.setFrist(ap, utledVentefrist(søknadVersjon), Venteårsak.UDEFINERT)
                    ));
                }
            }
        }
    }

    private boolean harSøkerOppgittTerminISøknad(FamilieHendelse versjon) {
        return FamilieHendelseType.TERMIN.equals(versjon.getType());
    }

}
