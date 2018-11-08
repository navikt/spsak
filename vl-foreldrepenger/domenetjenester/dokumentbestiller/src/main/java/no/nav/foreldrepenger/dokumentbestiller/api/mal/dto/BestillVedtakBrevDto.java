package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class BestillVedtakBrevDto implements AbacDto {
    @NotNull
    @Min(0)
    @Max(Long.MAX_VALUE)
    private Long behandlingId;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekst;

    @Size(max = 5000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String fritekstBrev;

    @Size(max = 200)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String overskrift;

    private boolean skalBrukeOverstyrendeFritekstBrev;
    private boolean finnesAllerede;

    public BestillVedtakBrevDto() { // NOSONAR
        // For Jackson
    }

    public BestillVedtakBrevDto(Long behandlingId, String fritekst) { // NOSONAR
        Objects.requireNonNull(behandlingId, "behandlingid");
        this.behandlingId = behandlingId;
        this.fritekst = fritekst;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public void setBehandlingId(Long behandlingId) {
        this.behandlingId = behandlingId;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String fritekst) {
        this.fritekst = fritekst;
    }

    public String getFritekstBrev() {
        return fritekstBrev;
    }

    public void setFritekstBrev(String fritekstBrev) {
        this.fritekstBrev = fritekstBrev;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public void setOverskrift(String overskrift) {
        this.overskrift = overskrift;
    }

    public boolean skalBrukeOverstyrendeFritekstBrev() {
        return skalBrukeOverstyrendeFritekstBrev;
    }

    public void setSkalBrukeOverstyrendeFritekstBrev(boolean skalBrukeOverstyrendeFritekstBrev) {
        this.skalBrukeOverstyrendeFritekstBrev = skalBrukeOverstyrendeFritekstBrev;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilBehandlingsId(behandlingId);
    }

    public boolean finnesAllerede() {
        return finnesAllerede;
    }

    public void setFinnesAllerede(boolean finnesAllerede) {
        this.finnesAllerede = finnesAllerede;
    }
}
