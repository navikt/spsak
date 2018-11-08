package no.nav.foreldrepenger.web.app.tjenester.kodeverk.app;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
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

    @Inject
    public HentKodeverkTjenesteImpl(KodeverkRepository kodeverkRepository, EnhetsTjeneste enhetsTjeneste) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository"); //$NON-NLS-1$
        Objects.requireNonNull(enhetsTjeneste, "enhetsTjeneste"); //$NON-NLS-1$
        this.kodeverkRepository = kodeverkRepository;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    @Override
    public Map<String, List<Kodeliste>> hentGruppertKodeliste() {
        Map<String, List<Kodeliste>> klientKoder = new HashMap<>();
        KODEVERK_SOM_BRUKES_PÅ_KLIENT.forEach(k -> {
            //TODO (TOR) Kjører repository-kall for kvar kodeliste. Er nok ikkje naudsynt
            List<Kodeliste> filtrertKodeliste = kodeverkRepository.hentAlle(k).stream()
                .filter(this::filterArbeidType)
                .filter(this::filtrerManuellVurderingType)
                .filter(ads -> !"-".equals(ads.getKode()))
                .collect(Collectors.toList());
            klientKoder.put(k.getSimpleName(), filtrertKodeliste);
        });

        return klientKoder;
    }

    private boolean filterArbeidType(Kodeliste kode) {
        if (kode instanceof ArbeidType) {
            ArbeidType arbeidType = (ArbeidType) kode;
            return arbeidType.erAnnenOpptjening();
        }
        return true;
    }

    private boolean filtrerManuellVurderingType(Kodeliste kode) {
        if (kode instanceof MedlemskapManuellVurderingType) {
            return ((MedlemskapManuellVurderingType) kode).visesPåKlient();
        }
        return true;
    }

    @Override
    public List<OrganisasjonsEnhet> hentBehandlendeEnheter() {
        final String statusAktiv = Enhetsstatus.AKTIV.name();
        final BehandlingTema behandlingTema = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        List<OrganisasjonsEnhet> orgEnhetsListe = enhetsTjeneste.hentEnhetListe(behandlingTema);

        return orgEnhetsListe.stream()
            .filter(organisasjonsEnhet -> statusAktiv.equals(organisasjonsEnhet.getStatus()))
            .collect(toList());
    }
}
