package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDate;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.DokumentmottakRestKlient;

@ApplicationScoped
public class KlargjørForVLTjeneste {


    private DokumentmottakRestKlient restKlient;

    @Inject
    public KlargjørForVLTjeneste(DokumentmottakRestKlient restKlient) {
        this.restKlient = restKlient;
    }

    public KlargjørForVLTjeneste() {
        //NOSONAR: gjett hvorfor
    }

    public void klargjørForVL(String xml, String saksnummer, String arkivId, DokumentTypeId dokumenttypeId, LocalDate forsendelseMottatt,
                              BehandlingTema behandlingsTema, UUID forsendelseId, DokumentKategori dokumentKategori, String journalFørendeEnhet) {
        String behandlingsTemaOffisiellKode = null;
        String dokumentTypeIdOffisiellKode = null;
        String dokumentKategoriOffisiellKode = null;
        if (behandlingsTema != null) {
            behandlingsTemaOffisiellKode = behandlingsTema.getOffisiellKode();
        }
        if (dokumenttypeId != null) {
            dokumentTypeIdOffisiellKode = dokumenttypeId.getOffisiellKode();
        }
        if (dokumentKategori != null) {
            dokumentKategoriOffisiellKode = dokumentKategori.getOffisiellKode();
        }
        JournalpostMottakDto journalpostMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingsTemaOffisiellKode, dokumentTypeIdOffisiellKode, forsendelseMottatt, xml);
        journalpostMottakDto.setForsendelseId(forsendelseId);
        journalpostMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        journalpostMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
        restKlient.send(journalpostMottakDto);
    }

}
