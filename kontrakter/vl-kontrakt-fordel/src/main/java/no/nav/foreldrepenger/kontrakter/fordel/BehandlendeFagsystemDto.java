package no.nav.foreldrepenger.kontrakter.fordel;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class BehandlendeFagsystemDto implements AbacDto {
    private boolean behandlesIVedtaksløsningen;
    private boolean sjekkMotInfotrygd;
    private boolean manuellVurdering;
    private boolean prøvIgjen;

    private LocalDateTime prøvIgjenTidspunkt;


    @Valid
    @JsonProperty
    private SaksnummerDto saksnummerDto;

    public BehandlendeFagsystemDto(String saksnummer) {
        this(new SaksnummerDto(saksnummer));
    }

    public BehandlendeFagsystemDto(SaksnummerDto saksnummerDto) {
        this.saksnummerDto = saksnummerDto;
    }

    public BehandlendeFagsystemDto() {  // For Jackson
    }

    public boolean isBehandlesIVedtaksløsningen() {
        return behandlesIVedtaksløsningen;
    }

    public void setBehandlesIVedtaksløsningen(boolean behandlesIVedtaksløsningen) {
        this.behandlesIVedtaksløsningen = behandlesIVedtaksløsningen;
    }

    public boolean isSjekkMotInfotrygd() {
        return sjekkMotInfotrygd;
    }

    public void setSjekkMotInfotrygd(boolean sjekkMotInfotrygd) {
        this.sjekkMotInfotrygd = sjekkMotInfotrygd;
    }

    public boolean isManuellVurdering() {
        return manuellVurdering;
    }

    public void setManuellVurdering(boolean manuellVurdering) {
        this.manuellVurdering = manuellVurdering;
    }

    public boolean isPrøvIgjen() {
        return prøvIgjen;
    }

    public void setPrøvIgjen(boolean prøvIgjen) {
        this.prøvIgjen = prøvIgjen;
    }

    public Optional<LocalDateTime> getPrøvIgjenTidspunkt() {
        return Optional.ofNullable(prøvIgjenTidspunkt);
    }

    public void setPrøvIgjenTidspunkt(LocalDateTime prøvIgjenTidspunkt) {
        this.prøvIgjenTidspunkt = prøvIgjenTidspunkt;
    }

    @JsonIgnore
    public Optional<String> getSaksnummer() {
        if(saksnummerDto != null){
            return Optional.of(saksnummerDto.getSaksnummer());
        }
        return Optional.empty();
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return saksnummerDto.abacAttributter();
    }
}
