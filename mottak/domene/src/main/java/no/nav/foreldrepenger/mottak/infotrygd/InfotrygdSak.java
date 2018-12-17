package no.nav.foreldrepenger.mottak.infotrygd;

import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema;

public class InfotrygdSak {
    private String tema;
    private String behandlingsTema;
    private String sakId;
    private LocalDate iverksatt;


    public InfotrygdSak(String sakId, String tema, String behandlingsTema, LocalDate iverksatt) { // NOSONAR
        this.sakId = sakId;
        this.tema = tema;
        this.behandlingsTema = behandlingsTema;
        this.iverksatt = iverksatt;
    }

    public String getSakId() {
        return sakId;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingsTema() {
        return behandlingsTema;
    }

    public Optional<LocalDate> getIverksatt() {
        return Optional.ofNullable(iverksatt);
    }

    public boolean gjelderEnsligForsorger() {
        return ENSLIG_FORSORGER_TEMA.getKode().equals(tema);
    }

    public boolean gjelderForeldrepenger() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderForeldrepenger(behandlingsTema);
    }

    public boolean gjelderSvangerskapspenger() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderSvangerskapspenger(behandlingsTema);
    }

    public boolean gjelderSykepenger() {
        return SYKEPENGER_TEMA.getKode().equals(tema);
    }

    public boolean gjelderEngangsstonad() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderEngangsstonad(behandlingsTema);
    }

}
