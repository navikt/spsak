package no.nav.foreldrepenger.domene.kontrollerfakta.fødsel;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunktMedCallback;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_FØDSELREGISTRERING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

/**
 * Aksjonspunkter for søknad om engangsstønad for fødsel
 */
@ApplicationScoped
public class AksjonspunktUtlederForEngangsstønadFødsel extends AksjonspunktUtlederForFødsel {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();

    AksjonspunktUtlederForEngangsstønadFødsel() {
        super();
    }

    @Inject
    public AksjonspunktUtlederForEngangsstønadFødsel(BehandlingRepositoryProvider repositoryProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(repositoryProvider, skjæringstidspunktTjeneste);
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) { // NOSONAR Metode rendrer flytdia.
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
                    if (gjelderSøknadenForeldrepenger() == NEI
                        || erSøkerRegistrertArbeidstakerMedLøpendeArbeidsforholdIAARegisteret(behandling) == NEI) {
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


    @Override
    protected Utfall gjelderSøknadenForeldrepenger() {
        return NEI;
    }

}
