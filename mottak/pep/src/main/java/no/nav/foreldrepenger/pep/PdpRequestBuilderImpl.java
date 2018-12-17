package no.nav.foreldrepenger.pep;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
public class PdpRequestBuilderImpl implements PdpRequestBuilder {
    private PipRepository pipRepository;


    public PdpRequestBuilderImpl() {
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.setToken(attributter.getIdToken());
        pdpRequest.setAction(attributter.getActionType());
        pdpRequest.setResource(attributter.getResource());

        Set<String> aktørIder = utledAktørIder(attributter);
        pdpRequest.setAktørId(aktørIder);
        return pdpRequest;
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter) {
        Set<String> aktørIder = new HashSet<>(attributter.getAktørIder());
        aktørIder.addAll(pipRepository.hentAktørIdForForsendelser(attributter.getDokumentforsendelseIder()));
        return aktørIder;
    }
}
