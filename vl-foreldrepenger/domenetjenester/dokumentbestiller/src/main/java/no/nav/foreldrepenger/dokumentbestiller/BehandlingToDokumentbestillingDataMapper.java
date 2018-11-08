package no.nav.foreldrepenger.dokumentbestiller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.dokumentbestiller.brev.DokumentBestillerTjenesteUtil;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Adresse;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Dokumentbestillingsinformasjon;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.UtenlandskPostadresse;

@ApplicationScoped
public class BehandlingToDokumentbestillingDataMapper {
    private static final String FAGOMRÅDE_KODE = "FOR";
    private static final String JOURNALFØRENDE_ENHET_KODE = "9999";
    private static final String UKJENT_ADRESSE = "Ukjent adresse";
    private LandkodeOversetter landkodeOversetter;

    public BehandlingToDokumentbestillingDataMapper() {
        // for cdi proxy
    }

    @Inject
    public BehandlingToDokumentbestillingDataMapper(LandkodeOversetter landkodeOversetter) {
        this.landkodeOversetter = landkodeOversetter;
    }

    public Dokumentbestillingsinformasjon mapFraBehandling(DokumentData dokumentData, DokumentFelles dokumentFelles, boolean harVedlegg) {
        final Dokumentbestillingsinformasjon dokumentbestillingsinformasjon = new Dokumentbestillingsinformasjon();
        dokumentbestillingsinformasjon.setDokumenttypeId(dokumentData.getDokumentMalType().getDoksysKode());
        Fagsystemer vlfp = new Fagsystemer();
        vlfp.setKodeRef(Fagsystem.FPSAK.getOffisiellKode());
        vlfp.setValue(Fagsystem.FPSAK.getOffisiellKode());
        dokumentbestillingsinformasjon.setBestillendeFagsystem(vlfp);
        setPostadresse(dokumentFelles, dokumentbestillingsinformasjon);
        Person bruker = new Person();
        bruker.setIdent(dokumentFelles.getSakspartId());
        bruker.setNavn(dokumentFelles.getSakspartNavn());
        dokumentbestillingsinformasjon.setBruker(bruker);
        Fagomraader dokumenttilhørendeFagområde = new Fagomraader();
        dokumenttilhørendeFagområde.setKodeRef(FAGOMRÅDE_KODE);
        dokumenttilhørendeFagområde.setValue(FAGOMRÅDE_KODE);
        dokumentbestillingsinformasjon.setDokumenttilhoerendeFagomraade(dokumenttilhørendeFagområde);
        dokumentbestillingsinformasjon.setFerdigstillForsendelse(!harVedlegg);
        dokumentbestillingsinformasjon.setInkludererEksterneVedlegg(harVedlegg);
        dokumentbestillingsinformasjon.setJournalfoerendeEnhet(JOURNALFØRENDE_ENHET_KODE); // FIXME (ONYX): (bts) bruke 9999 hvis automatisk, ellers pålogget saksbehandlers enhetskode
        dokumentbestillingsinformasjon.setJournalsakId(dokumentFelles.getSaksnummer().getVerdi());
        Person mottaker = new Person();
        mottaker.setIdent(dokumentFelles.getMottakerId());
        mottaker.setNavn(dokumentFelles.getMottakerNavn());
        dokumentbestillingsinformasjon.setMottaker(mottaker);
        dokumentbestillingsinformasjon.setSaksbehandlernavn(dokumentFelles.getSignerendeBeslutterNavn() == null ? "Vedtaksløsning Prosess" : dokumentFelles.getSignerendeBeslutterNavn());
        Fagsystemer gsak = new Fagsystemer();
        gsak.setKodeRef(Fagsystem.GOSYS.getOffisiellKode());
        gsak.setValue(Fagsystem.GOSYS.getOffisiellKode());
        dokumentbestillingsinformasjon.setSakstilhoerendeFagsystem(gsak);
        return dokumentbestillingsinformasjon;
    }

    private void setPostadresse(DokumentFelles dokumentFelles, Dokumentbestillingsinformasjon dokumentbestillingsinformasjon) {
        Adresse adresse;
        if (DokumentBestillerTjenesteUtil.erNorskAdresse(dokumentFelles.getMottakerAdresse())) {
            adresse = DokumentBestillerTjenesteUtil.lagNorskPostadresse(dokumentFelles);
        } else {
            adresse = lagUtenlandskPostadresse(dokumentFelles);
        }
        dokumentbestillingsinformasjon.setAdresse(adresse);
    }

    private UtenlandskPostadresse lagUtenlandskPostadresse(DokumentFelles dokumentFelles) {
        UtenlandskPostadresse adresse = new UtenlandskPostadresse();
        adresse.setAdresselinje1(dokumentFelles.getMottakerAdresse().getAdresselinje1() == null ? UKJENT_ADRESSE : dokumentFelles.getMottakerAdresse().getAdresselinje1());
        adresse.setAdresselinje2(dokumentFelles.getMottakerAdresse().getAdresselinje2());
        adresse.setAdresselinje3(dokumentFelles.getMottakerAdresse().getAdresselinje3());
        no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder landkode = new no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.informasjon.Landkoder();
        landkode.setValue(landkodeOversetter.tilIso2(dokumentFelles.getMottakerAdresse().getLand()));
        adresse.setLand(landkode);
        return adresse;
    }
}
