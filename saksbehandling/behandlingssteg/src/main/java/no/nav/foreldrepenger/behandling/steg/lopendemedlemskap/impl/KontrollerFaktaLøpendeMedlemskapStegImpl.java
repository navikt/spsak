package no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.api.KontrollerFaktaLøpendeMedlemskapSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;

@BehandlingStegRef(kode = "KOFAK_LOP_MEDL")
@BehandlingTypeRef("BT-004") //Revurdering
@FagsakYtelseTypeRef("FP")  //Foreldrepenger
@ApplicationScoped
public class KontrollerFaktaLøpendeMedlemskapStegImpl implements KontrollerFaktaLøpendeMedlemskapSteg {

    static final String FPSAK_LØPENDE_MEDLEMSKAP = "fpsak.lopende-medlemskap";
    private Unleash unleash;
    private UtledVurderingsdatoerForMedlemskapTjeneste tjeneste;

    private BehandlingRepository behandlingRepository;

    KontrollerFaktaLøpendeMedlemskapStegImpl() {
        //CDI
    }

    @Inject
    public KontrollerFaktaLøpendeMedlemskapStegImpl(Unleash unleash,
                                                    UtledVurderingsdatoerForMedlemskapTjeneste vurderingsdatoer,
                                                    BehandlingRepositoryProvider provider) {
        this.unleash = unleash;
        this.tjeneste = vurderingsdatoer;
        this.behandlingRepository = provider.getBehandlingRepository();
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        if (unleash.isEnabled(FPSAK_LØPENDE_MEDLEMSKAP)) {
            Long behandlingId = kontekst.getBehandlingId();
            Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
            if (!behandling.erRevurdering()) {
                throw new IllegalStateException("Utvikler-feil: Behandler bare revudering i foreldrepengerkontekst!.");
            }

            Set<LocalDate> finnVurderingsdatoer = tjeneste.finnVurderingsdatoer(behandlingId);
            if (!finnVurderingsdatoer.isEmpty()) {
                return BehandleStegResultat.tilbakeførtMedAksjonspunkter(List.of(AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP));
            }
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
