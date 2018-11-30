package no.nav.foreldrepenger.kodeverk;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kodeverk.api.KodeverkInfo;
import no.nav.foreldrepenger.kodeverk.api.KodeverkKode;
import no.nav.foreldrepenger.kodeverk.api.KodeverkTjeneste;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.EnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.IdentifiserbarEntitet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Kode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Kodeverkselement;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Node;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Periode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.SammensattKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.Term;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.finnkodeverkliste.Kodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.FinnKodeverkListeRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.FinnKodeverkListeResponse;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.kodeverk.KodeverkConsumer;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class KodeverkTjenesteImpl implements KodeverkTjeneste {

    private KodeverkConsumer kodeverkConsumer;

    private static final String NORSK_BOKMÅL = "nb";

    KodeverkTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public KodeverkTjenesteImpl(KodeverkConsumer kodeverkConsumer) {
        this.kodeverkConsumer = kodeverkConsumer;
    }

    @Override
    public List<KodeverkInfo> hentGjeldendeKodeverkListe() {
        List<KodeverkInfo> resultat = new ArrayList<>();
        FinnKodeverkListeRequest request = new FinnKodeverkListeRequest();

        FinnKodeverkListeResponse response = kodeverkConsumer.finnKodeverkListe(request);
        if (response != null) {
            oversettFraKodeverkListe(response, resultat);
        }
        return resultat;
    }

    private void oversettFraKodeverkListe(FinnKodeverkListeResponse response, List<KodeverkInfo> resultat) {
        for (Kodeverk kodeverkResponse : response.getKodeverkListe()) {
            KodeverkInfo kodeverk = new KodeverkInfo.Builder()
                .medEier("Kodeverkforvaltning")
                .medNavn(kodeverkResponse.getNavn())
                .medUri(kodeverkResponse.getUri())
                .medVersjon(kodeverkResponse.getVersjonsnummer())
                .medVersjonDato(DateUtil.convertToLocalDate(kodeverkResponse.getVersjoneringsdato()))
                .build();
            resultat.add(kodeverk);
        }
    }

    @Override
    public Map<String, KodeverkKode> hentKodeverk(String kodeverkNavn, String kodeverkVersjon, String kodeverkSpråk){
        HentKodeverkRequest request = new HentKodeverkRequest();
        request.setNavn(kodeverkNavn);
        if (kodeverkVersjon != null) {
            request.setVersjonsnummer(kodeverkVersjon);
        }
        if (kodeverkSpråk != null) {
            request.setSpraak(kodeverkSpråk);
        }
        Map<String, KodeverkKode> kodeverkKodeMap = Collections.emptyMap();
        try {
            HentKodeverkResponse response = kodeverkConsumer.hentKodeverk(request);
            if (response != null) {
                kodeverkKodeMap = oversettFraHentKodeverkResponse(response, kodeverkSpråk);
            }
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet ex) {
            throw KodeverkFeil.FACTORY.hentKodeverkKodeverkIkkeFunnet(ex).toException();
        }
        return kodeverkKodeMap;
    }

    private Map<String, KodeverkKode> oversettFraHentKodeverkResponse(HentKodeverkResponse response, String kodeverkSpråk) {
        String kodeverkNavn = response.getKodeverk().getNavn();
        if (response.getKodeverk() instanceof EnkeltKodeverk) {
            return ((EnkeltKodeverk) response.getKodeverk()).getKode().stream()
                .map(k -> oversettFraKode(k, kodeverkNavn, kodeverkSpråk))
                .collect(Collectors.toMap(KodeverkKode::getKode, kodeverkKode -> kodeverkKode));
        } else if (response.getKodeverk() instanceof SammensattKodeverk) {
            SammensattKodeverk sammensattKodeverk = (SammensattKodeverk) response.getKodeverk();
            return sammensattKodeverk.getInneholderNode().stream()
                .map(node -> oversettFraSammensattKode(sammensattKodeverk.getBrukerKodeverk(), node, kodeverkNavn, kodeverkSpråk))
                .collect(Collectors.toMap(KodeverkKode::getKode, kodeverkKode -> kodeverkKode));
        } else {
            throw KodeverkFeil.FACTORY.hentKodeverkKodeverkTypeIkkeStøttet(response.getKodeverk().getClass().getSimpleName()).toException();
        }
    }

    private KodeverkKode oversettFraSammensattKode(List<EnkeltKodeverk> brukerKodeverk, Node node, String kodeverkNavn, String kodeverkSpråk){
        Kode kode = node.getInneholderKode();
        Optional<Tuple<LocalDate, LocalDate>> gyldighetsperiode = finnGyldighetsperiode(kode.getGyldighetsperiode());
        Optional<String> term = finnTerm(kode.getTerm(), kodeverkSpråk);
        Optional<String> kodeverkOptional = getKodeverkFraUri(kode.getUri(), brukerKodeverk);
        return new KodeverkKode.Builder()
            .medKodeverk(kodeverkOptional.orElse(kodeverkNavn))
            .medKode(kode.getNavn())
            .medUri(kode.getUri())
            .medNavn(term.orElse(null))
            .medGyldigFom(gyldighetsperiode.isPresent() ? gyldighetsperiode.get().getElement1() : null)
            .medGyldigTom(gyldighetsperiode.isPresent() ? gyldighetsperiode.get().getElement2() : null)
            .leggTilUnderkoder(node.getUndernode().stream()
                .map(undernode -> oversettFraSammensattKode(brukerKodeverk, undernode, kodeverkNavn, kodeverkSpråk))
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Finner kodeverkstype for sammensatte kodeverk.
     *
     * NB: Sorterer liste i reverse order for at LandkoderISO2 skal komme foran Landkoder.
     */
    private Optional<String> getKodeverkFraUri(String uri, List<EnkeltKodeverk> brukerKodeverk){
        return brukerKodeverk.stream()
            .map(EnkeltKodeverk::getNavn)
            .sorted(Comparator.reverseOrder())
            .filter(uri::contains)
            .findFirst();
    }

    private KodeverkKode oversettFraKode(Kode kode, String kodeverkNavn, String kodeverkSpråk){
        Optional<Tuple<LocalDate, LocalDate>> gyldighetsperiode = finnGyldighetsperiode(kode.getGyldighetsperiode());
        Optional<String> term = finnTerm(kode.getTerm(), kodeverkSpråk);
        return new KodeverkKode.Builder()
            .medKodeverk(kodeverkNavn)
            .medKode(kode.getNavn())
            .medUri(kode.getUri())
            .medNavn(term.orElse(null))
            .medGyldigFom(gyldighetsperiode.isPresent() ? gyldighetsperiode.get().getElement1() : null)
            .medGyldigTom(gyldighetsperiode.isPresent() ? gyldighetsperiode.get().getElement2() : null)
            .build();
    }

    /**
     * Finner term navnet med nyeste gyldighetsdato og angitt språk (default norsk bokmål).
     */
    private Optional<String> finnTerm(List<Term> termList, String kodeverkSpråk){
        Comparator<Kodeverkselement> vedGyldigFom = (e1, e2) ->
            e1.getGyldighetsperiode().get(0).getFom().compare(e2.getGyldighetsperiode().get(0).getFom());
        String språk = kodeverkSpråk != null ? kodeverkSpråk : NORSK_BOKMÅL;
        return termList.stream()
            .filter(term -> term.getSpraak().compareToIgnoreCase(språk) == 0)
            .sorted(vedGyldigFom.reversed())
            .map(IdentifiserbarEntitet::getNavn)
            .findFirst();
    }

    /**
     * Finner nyeste gyldighetsperiode ut fra fom dato.
     */
    private Optional<Tuple<LocalDate, LocalDate>> finnGyldighetsperiode(List<Periode> periodeList){
        Comparator<Periode> vedGyldigFom = (p1, p2) -> p1.getFom().compare(p2.getFom());
        Optional<Periode> periodeOptional = periodeList.stream()
            .sorted(vedGyldigFom.reversed())
            .findFirst();
        return periodeOptional.map(periode -> new Tuple<>(DateUtil.convertToLocalDate(periode.getFom()),
            DateUtil.convertToLocalDate(periode.getTom())));
    }
}
