package no.nav.foreldrepenger.behandling.steg.klage.impl;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.klage.KlageNkSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;

@BehandlingStegRef(kode = "KLAGEOI")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class KlageNkStegImpl implements KlageNkSteg {

    private BehandlingRepository behandlingRepository;

    public KlageNkStegImpl(){
        // For CDI proxy
    }

    @Inject
    public KlageNkStegImpl(BehandlingRepository behandlingRepository){
        this.behandlingRepository = behandlingRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        if(BehandlingResultatType.KLAGE_YTELSESVEDTAK_STADFESTET.equals(behandling.getBehandlingsresultat().getBehandlingResultatType())){
            List<AksjonspunktDefinisjon> aksjonspunktDefinisjons = singletonList(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NK);

            return BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunktDefinisjons);
        }

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                              TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType inngangUtgang) {
        if (Objects.equals(TransisjonType.HOPP_OVER_BAKOVER, transisjonType)) {
            behandlingRepository.slettKlageVurderingResultat(behandling, kontekst.getSkriveLås(), KlageVurdertAv.NK);
        }
    }

}
