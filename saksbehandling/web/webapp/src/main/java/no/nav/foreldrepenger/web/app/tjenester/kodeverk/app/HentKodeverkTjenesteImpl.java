package no.nav.foreldrepenger.web.app.tjenester.kodeverk.app;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetsstatus;

@ApplicationScoped
class HentKodeverkTjenesteImpl implements HentKodeverkTjeneste {

    private KodeverkRepository kodeverkRepository;
    private EnhetsTjeneste enhetsTjeneste;

    HentKodeverkTjenesteImpl() {
        // CDI
    }

    @Inject
    public HentKodeverkTjenesteImpl(KodeverkRepository kodeverkRepository, EnhetsTjeneste enhetsTjeneste) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository"); //$NON-NLS-1$
        Objects.requireNonNull(enhetsTjeneste, "enhetsTjeneste"); //$NON-NLS-1$
        this.kodeverkRepository = kodeverkRepository;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    @Override
    public Map<String, List<Kodeliste>> hentGruppertKodeliste() {
        Map<String, List<Kodeliste>> kodelistMap = kodeverkRepository.hentAlle(KODEVERK_SOM_BRUKES_PÅ_KLIENT_DISCRIMINATORS);
        Optional.ofNullable(kodelistMap.get(ArbeidType.class.getSimpleName()))
            .ifPresent(it -> it.removeIf(at -> !((ArbeidType) at).erAnnenOpptjening()));
        Optional.ofNullable(kodelistMap.get(MedlemskapManuellVurderingType.class.getSimpleName()))
            .ifPresent(it -> it.removeIf(at -> !((MedlemskapManuellVurderingType) at).visesPåKlient()));
        return kodelistMap;
    }

    @Override
    public List<OrganisasjonsEnhet> hentBehandlendeEnheter() {
        final String statusAktiv = Enhetsstatus.AKTIV.name();

        List<OrganisasjonsEnhet> orgEnhetsListe = enhetsTjeneste.hentEnhetListe();

        return orgEnhetsListe.stream()
            .filter(organisasjonsEnhet -> statusAktiv.equals(organisasjonsEnhet.getStatus()))
            .collect(toList());
    }
}
