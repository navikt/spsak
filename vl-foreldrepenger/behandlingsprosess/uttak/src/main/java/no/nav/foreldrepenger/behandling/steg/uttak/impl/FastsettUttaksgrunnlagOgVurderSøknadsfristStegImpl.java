package no.nav.foreldrepenger.behandling.steg.uttak.impl;

import static java.util.Collections.singletonList;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.uttak.FastsettUttaksgrunnlagOgVurderSøknadsfristSteg;
import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.FastsettUttaksgrunnlagTjeneste;

@BehandlingStegRef(kode = "SØKNADSFRIST_FP")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FastsettUttaksgrunnlagOgVurderSøknadsfristStegImpl implements FastsettUttaksgrunnlagOgVurderSøknadsfristSteg {

    private BehandlingRepository behandlingRepository;
    private SøknadsfristForeldrepengerTjeneste søknadsfristForeldrepengerTjeneste;
    private FastsettUttaksgrunnlagTjeneste fastsettUttaksgrunnlagTjeneste;

    @Inject
    public FastsettUttaksgrunnlagOgVurderSøknadsfristStegImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                                              SøknadsfristForeldrepengerTjeneste søknadsfristForeldrepengerTjeneste,
                                                              FastsettUttaksgrunnlagTjeneste fastsettUttaksgrunnlagTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider.getBehandlingRepository();
        this.søknadsfristForeldrepengerTjeneste = søknadsfristForeldrepengerTjeneste;
        this.fastsettUttaksgrunnlagTjeneste = fastsettUttaksgrunnlagTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        //Sjekk søknadsfrist for søknadsperioder
        Optional<AksjonspunktDefinisjon> søknadfristAksjonspunktDefinisjon = søknadsfristForeldrepengerTjeneste.vurderSøknadsfristForForeldrepenger(kontekst);

        //Fastsett uttaksgrunnlag
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        fastsettUttaksgrunnlagTjeneste.fastsettUttaksgrunnlag(behandling);
        behandlingRepository.lagre(behandling, lås);

        //Returner eventuelt aksjonspunkt ifm søknadsfrist
        if(søknadfristAksjonspunktDefinisjon.isPresent()) {
            return BehandleStegResultat.utførtMedAksjonspunkter(singletonList(søknadfristAksjonspunktDefinisjon.get()));
        }
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }
}
