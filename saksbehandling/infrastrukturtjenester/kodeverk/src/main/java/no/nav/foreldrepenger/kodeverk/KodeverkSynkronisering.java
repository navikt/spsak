package no.nav.foreldrepenger.kodeverk;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodelisteRelasjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeverk;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkSynkroniseringRepository;
import no.nav.foreldrepenger.kodeverk.api.KodeverkInfo;
import no.nav.foreldrepenger.kodeverk.api.KodeverkKode;
import no.nav.foreldrepenger.kodeverk.api.KodeverkTjeneste;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Tjenestelogikken for batch tjeneste for å automatisk oppdatere kodeverk.
 * Tilgjengelighet begrenses til package.
 */

@Dependent
class KodeverkSynkronisering {

    private static final Logger LOGGER = LoggerFactory.getLogger(KodeverkSynkronisering.class);

    private KodeverkSynkroniseringRepository kodeverkSynkroniseringRepository;
    private KodeverkTjeneste kodeverkTjeneste;
    private Map<String, String> kodeverkEierNavnMap;

    KodeverkSynkronisering() {
        // for CDI proxy
    }

    @Inject
    KodeverkSynkronisering(KodeverkSynkroniseringRepository kodeverkSynkroniseringRepository,
                           KodeverkTjeneste kodeverkTjeneste) {
        this.kodeverkSynkroniseringRepository = kodeverkSynkroniseringRepository;
        this.kodeverkTjeneste = kodeverkTjeneste;
    }

    void synkroniserAlleKodeverk() {
        int antallSynkronisert = 0;
        List<Kodeverk> kodeverkList = kodeverkSynkroniseringRepository.hentKodeverkForSynkronisering();
        kodeverkEierNavnMap = kodeverkSynkroniseringRepository.hentKodeverkEierNavnMap();

        List<KodeverkInfo> kodeverkInfoList = kodeverkTjeneste.hentGjeldendeKodeverkListe();

        for (Kodeverk kodeverk : kodeverkList) {
            KodeverkInfo kodeverkInfo = kodeverkInfoList.stream()
                .filter(ki -> ki.getNavn().equals(kodeverk.getKodeverkEierNavn()))
                .findFirst().orElse(null);
            if (kodeverkInfo != null && !kodeverkInfo.getVersjon().equals(kodeverk.getKodeverkEierVersjon())) {
                LOGGER.info("Ny versjon av kodeverk: {}",
                    LoggerUtils.removeLineBreaks(kodeverk.getKode() + " eier " + kodeverkInfo.getVersjon() + " lokal " + kodeverk.getKodeverkEierVersjon())); //NOSONAR
                antallSynkronisert++;
                kodeverkSynkroniseringRepository.oppdaterEksisterendeKodeVerk(kodeverk.getKode(), kodeverkInfo.getVersjon(), kodeverkInfo.getUri());
                if (kodeverk.getSammensatt()) {
                    synkroniserSammensattKodeverk(kodeverk, kodeverkInfo.getVersjon());
                } else {
                    synkroniserKodeverk(kodeverk, kodeverkInfo.getVersjon());
                }

            }
        }
        if (antallSynkronisert == 0) {
            LOGGER.info("Ingen nye versjoner av kodeverk"); //NOSONAR
        } else {
            kodeverkSynkroniseringRepository.lagre();
            LOGGER.info("Nye versjoner av kodeverk lagret"); //NOSONAR
        }
    }

    private void synkroniserSammensattKodeverk(Kodeverk kodeverk, String versjon) {
        LOGGER.info("Synkroniserer kodeverk: {}", LoggerUtils.removeLineBreaks(kodeverk.getKode())); //NOSONAR
        Map<String, KodeverkKode> masterKoderMap;
        try {
            masterKoderMap = kodeverkTjeneste.hentKodeverk(kodeverk.getKodeverkEierNavn(), versjon,null);
        } catch (IntegrasjonException ex) {
            throw KodeverkFeil.FACTORY.synkronoseringAvKodeverkFeilet(kodeverk.getKode(), ex).toException();
        }

        for (KodeverkKode masterKode : masterKoderMap.values()) {
            String kodeverkNavn = kodeverkEierNavnMap.get(masterKode.getKodeverk());
            List<KodelisteRelasjon> eksisterendeKodeRelasjoner = kodeverkSynkroniseringRepository.hentKodelisteRelasjoner(kodeverkNavn, masterKode.getKode());
            Map<String, KodelisteRelasjon> eksisterendeKodeRelasjonerMap = eksisterendeKodeRelasjoner.stream()
                .collect(Collectors.toMap(this::lagMapKeyForKodelisteRelasjon, kodelisteRelasjon -> kodelisteRelasjon));
            Map<String, KodeverkKode> mottatteKodeRelasjonerMap = new HashMap<>();
            synkroniserNyEllerEksisterendeKodeRelasjon(kodeverk, eksisterendeKodeRelasjonerMap, masterKode, mottatteKodeRelasjonerMap);
            behandleEksisterendeKodeRelasjonerIkkeMottatt(eksisterendeKodeRelasjonerMap, mottatteKodeRelasjonerMap);
        }
    }

    /**
     * Koderelasjoner slettes aldri. Dersom en eksisterende koderelasjon ikke mottas settes den til å være ugyldig
     * ved å endre gyldighetsdato og det logges som en warning.
     */
    private void behandleEksisterendeKodeRelasjonerIkkeMottatt(Map<String, KodelisteRelasjon> eksisterendeKodeRelasjonerMap,
                                                               Map<String, KodeverkKode> mottatteKodeRelasjonerMap) {
        Set<String> eksisterendeKoderIkkeMottatt = eksisterendeKodeRelasjonerMap.entrySet().stream()
            .filter(eksisterendeKode -> mottatteKodeRelasjonerMap.entrySet().stream()
                .anyMatch(mottattKode -> kodeverkEierNavnMap.get(mottattKode.getValue().getKodeverk()).compareTo(eksisterendeKode.getValue().getKodeverk1()) == 0
                && mottattKode.getValue().getKode().compareTo(eksisterendeKode.getValue().getKode1()) == 0))
            .filter(eksisterendeKode -> !mottatteKodeRelasjonerMap.containsKey(eksisterendeKode.getKey()))
            .map(eksisterendeKode -> eksisterendeKode.getKey())
            .collect(Collectors.toSet());

        for (String ikkeMottatt : eksisterendeKoderIkkeMottatt) {
            KodelisteRelasjon ikkeMottattRelasjon =  eksisterendeKodeRelasjonerMap.get(ikkeMottatt);
            KodeverkFeil.FACTORY.eksisterendeKodeRelasjonIkkeMottatt(ikkeMottattRelasjon.getKodeverk1(), ikkeMottattRelasjon.getKode1(),
                ikkeMottattRelasjon.getKodeverk2(), ikkeMottattRelasjon.getKode2()).log(LOGGER);
            kodeverkSynkroniseringRepository.oppdaterEksisterendeKodeRelasjon(ikkeMottattRelasjon.getKodeverk1(), ikkeMottattRelasjon.getKode1(),
                ikkeMottattRelasjon.getKodeverk2(), ikkeMottattRelasjon.getKode2(), ikkeMottattRelasjon.getGyldigFom(), LocalDate.now(FPDateUtil.getOffset()));
        }
    }

    private String lagMapKeyForKodelisteRelasjon(KodelisteRelasjon kodelisteRelasjon){  // NOSONAR
        return kodelisteRelasjon.getKodeverk1() + kodelisteRelasjon.getKode1() +
            kodelisteRelasjon.getKodeverk2() + kodelisteRelasjon.getKode2();
    }

    private String lagMapKeyForKodelisteRelasjon(KodeverkKode kode1, KodeverkKode kode2){
        String kodeverk1 = kodeverkEierNavnMap.get(kode1.getKodeverk());
        String kodeverk2 = kodeverkEierNavnMap.get(kode2.getKodeverk());
        return kodeverk1 + kode1.getKode() +
            kodeverk2 + kode2.getKode();
    }

    private void synkroniserNyEllerEksisterendeKodeRelasjon(Kodeverk kodeverk, Map<String, KodelisteRelasjon> eksisterendeKodeRelasjoner,
                                                            KodeverkKode masterKode, Map<String, KodeverkKode> mottatteKodeRelasjonerMap) {
        for (KodeverkKode underKode : masterKode.getUnderkoder()) {
            String mapKey = lagMapKeyForKodelisteRelasjon(masterKode, underKode);
            mottatteKodeRelasjonerMap.put(mapKey, masterKode);

            if (kodeverk.getSynkEksisterendeKoderFraKodeverkEier() && eksisterendeKodeRelasjoner.containsKey(mapKey)) {
                synkroniserEksisterendeKodeRelasjon(masterKode, underKode, eksisterendeKodeRelasjoner.get(mapKey));
            }
            if (kodeverk.getSynkNyeKoderFraKodeverEier() && !eksisterendeKodeRelasjoner.containsKey(mapKey)) {
                synkroniserNyKodeRelasjon(masterKode, underKode);
            }

            synkroniserNyEllerEksisterendeKodeRelasjon(kodeverk, eksisterendeKodeRelasjoner, underKode, mottatteKodeRelasjonerMap);
        }
    }

    private void synkroniserKodeverk(Kodeverk kodeverk, String versjon) {
        LOGGER.info("Synkroniserer kodeverk: {}", LoggerUtils.removeLineBreaks(kodeverk.getKode())); // NOSONAR

        List<Kodeliste> eksisterendeKoder = kodeverkSynkroniseringRepository.hentKodeliste(kodeverk.getKode());
        Map<String, Kodeliste> eksisterendeKoderMap = eksisterendeKoder.stream()
            .collect(Collectors.toMap(this::finnOffisiellKode, kodeliste -> kodeliste));

        Map<String, KodeverkKode> masterKoderMap;
        try {
            masterKoderMap = kodeverkTjeneste.hentKodeverk(kodeverk.getKodeverkEierNavn(), versjon, null);
        } catch (IntegrasjonException ex) {
            throw KodeverkFeil.FACTORY.synkronoseringAvKodeverkFeilet(kodeverk.getKode(), ex).toException();
        }

        masterKoderMap.forEach((key, value) -> synkroniserNyEllerEksisterendeKode(kodeverk, eksisterendeKoderMap, value));
        behandleEksisterendeKoderIkkeMottatt(eksisterendeKoderMap, masterKoderMap);
    }

    /**
     * Koder slettes aldri. Dersom en eksisterende kode ikke mottas logges dette som en warning.
     */
    private void behandleEksisterendeKoderIkkeMottatt(Map<String, Kodeliste> eksisterendeKoderMap, Map<String, KodeverkKode> masterKoderMap){
        Set<String> eksisterendeKoderIkkeMottatt = eksisterendeKoderMap.keySet().stream()
            .filter(eksisterendeKode -> !masterKoderMap.containsKey(eksisterendeKode))
            .collect(Collectors.toSet());
        eksisterendeKoderIkkeMottatt.forEach(ikkeMottatt ->
            KodeverkFeil.FACTORY.eksisterendeKodeIkkeMottatt(eksisterendeKoderMap.get(ikkeMottatt).getKodeverk(), ikkeMottatt).log(LOGGER));
    }

    private void synkroniserNyEllerEksisterendeKode(Kodeverk kodeverk, Map<String, Kodeliste> eksisterendeKoderMap,
                                                    KodeverkKode masterKode) {
        if (kodeverk.getSynkEksisterendeKoderFraKodeverkEier() && eksisterendeKoderMap.containsKey(masterKode.getKode())) {
            synkroniserEksisterendeKode(masterKode, eksisterendeKoderMap.get(masterKode.getKode()));
        }
        if (kodeverk.getSynkNyeKoderFraKodeverEier() && !eksisterendeKoderMap.containsKey(masterKode.getKode())) {
            synkroniserNyKode(kodeverk.getKode(), masterKode);
        }
    }

    private String finnOffisiellKode(Kodeliste kodeliste) {     // NOSONAR
        return kodeliste.getOffisiellKode() != null ? kodeliste.getOffisiellKode() : kodeliste.getKode();
    }

    private void synkroniserNyKode(String kodeverk, KodeverkKode kodeverkKode) {
        LOGGER.info("Ny kode: {} {}",                               // NOSONAR
            LoggerUtils.removeLineBreaks(kodeverk),                 // NOSONAR
            LoggerUtils.removeLineBreaks(kodeverkKode.getKode()));  // NOSONAR

        kodeverkSynkroniseringRepository.opprettNyKode(kodeverk, kodeverkKode.getKode(),
            kodeverkKode.getKode(), kodeverkKode.getNavn(), kodeverkKode.getGyldigFom(), kodeverkKode.getGyldigTom());
    }

    private void synkroniserNyKodeRelasjon(KodeverkKode kode1, KodeverkKode kode2) {
        String kodeverk1 = kodeverkEierNavnMap.get(kode1.getKodeverk());
        String kodeverk2 = kodeverkEierNavnMap.get(kode2.getKodeverk());

        boolean beggKoderEksisterer = true;
        if (!kodeverkSynkroniseringRepository.eksistererKode(kodeverk1, kode1.getKode())){
            KodeverkFeil.FACTORY.nyKodeRelasjonMedIkkeEksisterendeKode(kodeverk1, kode1.getKode()).log(LOGGER);
            beggKoderEksisterer = false;
        }
        if (!kodeverkSynkroniseringRepository.eksistererKode(kodeverk2, kode2.getKode())){
            KodeverkFeil.FACTORY.nyKodeRelasjonMedIkkeEksisterendeKode(kodeverk2, kode2.getKode()).log(LOGGER);
            beggKoderEksisterer = false;
        }

        if (beggKoderEksisterer) {
            LOGGER.info("Ny koderelasjon: {} {} -> {} {}",      // NOSONAR
                LoggerUtils.removeLineBreaks(kodeverk1),        // NOSONAR
                LoggerUtils.removeLineBreaks(kode1.getKode()),  // NOSONAR
                LoggerUtils.removeLineBreaks(kodeverk2),        // NOSONAR
                LoggerUtils.removeLineBreaks(kode2.getKode())); // NOSONAR

            kodeverkSynkroniseringRepository.opprettNyKodeRelasjon(kodeverk1, kode1.getKode(), kodeverk2, kode2.getKode(), kode1.getGyldigFom(), kode1.getGyldigTom());
        }
    }

    private void synkroniserEksisterendeKodeRelasjon(KodeverkKode kode1, KodeverkKode kode2, KodelisteRelasjon kodelisteRelasjon) {
        if (!erLike(kode1, kodelisteRelasjon)){
            String kodeverk1 = kodeverkEierNavnMap.get(kode1.getKodeverk());
            String kodeverk2 = kodeverkEierNavnMap.get(kode2.getKodeverk());

            LOGGER.info("Oppdaterer koderelasjon: {} {} -> {} {}", // NOSONAR
                LoggerUtils.removeLineBreaks(kodeverk1),        // NOSONAR
                LoggerUtils.removeLineBreaks(kode1.getKode()),  // NOSONAR
                LoggerUtils.removeLineBreaks(kodeverk2),        // NOSONAR
                LoggerUtils.removeLineBreaks(kode2.getKode())); // NOSONAR

            kodeverkSynkroniseringRepository.oppdaterEksisterendeKodeRelasjon(kodeverk1, kode1.getKode(), kodeverk2, kode2.getKode(), kode1.getGyldigFom(), kode1.getGyldigTom());
        }
    }

    private void synkroniserEksisterendeKode(KodeverkKode kodeverkKode, Kodeliste kodeliste) {
        if (!erLike(kodeverkKode, kodeliste)) {
            LOGGER.info("Oppdaterer kode: {} {}",                          // NOSONAR
                LoggerUtils.removeLineBreaks(kodeliste.getKodeverk()),     // NOSONAR
                LoggerUtils.removeLineBreaks(kodeliste.getKode()));        // NOSONAR

            kodeverkSynkroniseringRepository.oppdaterEksisterendeKode(kodeliste.getKodeverk(), kodeliste.getKode(),
                kodeverkKode.getKode(), kodeverkKode.getNavn(), kodeverkKode.getGyldigFom(), kodeverkKode.getGyldigTom());
        }
    }

    private boolean erLike(KodeverkKode kodeverkKode, Kodeliste kodeliste) {
        return kodeverkKode.getGyldigFom().compareTo(kodeliste.getGyldigFraOgMed()) == 0
            && kodeverkKode.getGyldigTom().compareTo(kodeliste.getGyldigTilOgMed()) == 0
            && kodeverkKode.getNavn().compareTo(kodeliste.getNavn()) == 0;
    }

    private boolean erLike(KodeverkKode kodeverkKode, KodelisteRelasjon kodelisteRelasjon) { //NOSONAR
        return kodeverkKode.getGyldigFom().compareTo(kodelisteRelasjon.getGyldigFom()) == 0
            && kodeverkKode.getGyldigTom().compareTo(kodelisteRelasjon.getGyldigTom()) == 0;
    }
}
