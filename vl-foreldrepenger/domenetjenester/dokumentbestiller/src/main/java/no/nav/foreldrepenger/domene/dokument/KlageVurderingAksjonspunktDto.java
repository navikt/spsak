package no.nav.foreldrepenger.domene.dokument;

import java.time.LocalDate;
import java.util.Optional;

public class KlageVurderingAksjonspunktDto {
    private String klageVurderingKode;
    private String begrunnelse;
    private LocalDate vedtaksdatoPaklagdBehandling;
    private String klageAvvistArsakKode;
    private String klageMedholdArsakKode;
    private boolean erNfpAksjonspunkt;

    public KlageVurderingAksjonspunktDto(String klageVurderingKode, String begrunnelse, LocalDate vedtaksdatoPaklagdBehandling, String klageAvvistArsakKode, String klageMedholdArsakKode, boolean erNfpAksjonspunkt) {
        this.klageVurderingKode = klageVurderingKode;
        this.begrunnelse = begrunnelse;
        this.vedtaksdatoPaklagdBehandling = vedtaksdatoPaklagdBehandling;
        this.klageAvvistArsakKode = klageAvvistArsakKode;
        this.klageMedholdArsakKode = klageMedholdArsakKode;
        this.erNfpAksjonspunkt = erNfpAksjonspunkt;
    }

    public String getKlageVurderingKode() {
        return klageVurderingKode;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public LocalDate getVedtaksdatoPaklagdBehandling() {
        return vedtaksdatoPaklagdBehandling;
    }

    public Optional<String> getKlageAvvistArsakKode() {
        return Optional.ofNullable(klageAvvistArsakKode);
    }

    public Optional<String> getKlageMedholdArsakKode() {
        return Optional.ofNullable(klageMedholdArsakKode);
    }

    public boolean getErNfpAksjonspunkt() {
        return erNfpAksjonspunkt;
    }
}
