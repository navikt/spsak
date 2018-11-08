package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public abstract class KlageVurderingResultatAksjonspunktDto extends BekreftetAksjonspunktDto {

    @NotNull
    @ValidKodeverk
    private KlageVurdering klageVurdering;

    @Size(max = 2000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String begrunnelse;

    @ValidKodeverk
    private KlageAvvistÅrsak klageAvvistArsak;

    @ValidKodeverk
    private KlageMedholdÅrsak klageMedholdArsak;

    @Valid
    private LocalDate vedtaksdatoPaklagdBehandling;

    KlageVurderingResultatAksjonspunktDto() { // NOSONAR
        // For Jackson
    }

    public KlageVurderingResultatAksjonspunktDto( // NOSONAR
            String begrunnelse,
            KlageVurdering klageVurdering,
            KlageMedholdÅrsak klageMedholdArsak,
            KlageAvvistÅrsak klageAvvistArsak,
            LocalDate vedtaksdatoPaklagdBehandling) {
        super(begrunnelse);
        this.klageVurdering = klageVurdering;
        this.begrunnelse = begrunnelse;
        this.klageAvvistArsak = klageAvvistArsak;
        this.klageMedholdArsak = klageMedholdArsak;
        this.vedtaksdatoPaklagdBehandling = vedtaksdatoPaklagdBehandling;
    }

    public KlageVurdering getKlageVurdering() {
        return klageVurdering;
    }

    @Override
    public String getBegrunnelse() {
        return begrunnelse;
    }

    public KlageAvvistÅrsak getKlageAvvistArsak() {
        return klageAvvistArsak;
    }

    public KlageMedholdÅrsak getKlageMedholdArsak() {
        return klageMedholdArsak;
    }

    public LocalDate getVedtaksdatoPaklagdBehandling() {
        return vedtaksdatoPaklagdBehandling;
    }
    
    @JsonTypeName(KlageVurderingResultatNfpAksjonspunktDto.AKSJONSPUNKT_KODE)
    public static class KlageVurderingResultatNfpAksjonspunktDto extends KlageVurderingResultatAksjonspunktDto {

        static final String AKSJONSPUNKT_KODE = "5035";

        KlageVurderingResultatNfpAksjonspunktDto() {
            super();
        }

        public KlageVurderingResultatNfpAksjonspunktDto(String begrunnelse, KlageVurdering klageVurdering,
                                                        KlageMedholdÅrsak klageMedholdÅrsak, KlageAvvistÅrsak klageAvvistÅrsak,
                                                        LocalDate vedtaksdatoPaklagdBehandling) {
            super(begrunnelse, klageVurdering, klageMedholdÅrsak, klageAvvistÅrsak, vedtaksdatoPaklagdBehandling);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }
    
    @JsonTypeName(KlageVurderingResultatNkAksjonspunktDto.AKSJONSPUNKT_KODE)
    public static class KlageVurderingResultatNkAksjonspunktDto extends KlageVurderingResultatAksjonspunktDto {

        static final String AKSJONSPUNKT_KODE = "5036";

        KlageVurderingResultatNkAksjonspunktDto() {
            super();
        }

        public KlageVurderingResultatNkAksjonspunktDto(String begrunnelse, KlageVurdering klageVurdering,
                                                       KlageMedholdÅrsak klageMedholdÅrsak, KlageAvvistÅrsak klageAvvistÅrsak,
                                                       LocalDate vedtaksdatoPaklagdBehandling) {
            super(begrunnelse, klageVurdering, klageMedholdÅrsak, klageAvvistÅrsak, vedtaksdatoPaklagdBehandling);
        }

        @Override
        public String getKode() {
            return AKSJONSPUNKT_KODE;
        }
    }

}
