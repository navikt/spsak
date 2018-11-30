package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.RettenTil;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;

public class BehandlingsresultatDto {

    private Long id;
    private BehandlingResultatType type;
    private Avslagsårsak avslagsarsak;
    private String avslagsarsakFritekst;
    private RettenTil rettenTil;
    private List<KonsekvensForYtelsen> konsekvenserForYtelsen;
    private Vedtaksbrev vedtaksbrev;
    private String overskrift;
    private String fritekstbrev;
    private LocalDate skjaeringstidspunktForeldrepenger;

    public BehandlingsresultatDto() {
        // trengs for deserialisering av JSON
    }

    void setId(Long id) {
        this.id = id;
    }

    void setType(BehandlingResultatType type) {
        this.type = type;
    }

    void setAvslagsarsak(Avslagsårsak avslagsarsak) {
        this.avslagsarsak = avslagsarsak;
    }

    void setAvslagsarsakFritekst(String avslagsarsakFritekst) {
        this.avslagsarsakFritekst = avslagsarsakFritekst;
    }

    public void setRettenTil(RettenTil rettenTil) {
        this.rettenTil = rettenTil;
    }

    public void setKonsekvenserForYtelsen(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        this.konsekvenserForYtelsen = konsekvenserForYtelsen;
    }

    public void setVedtaksbrev(Vedtaksbrev vedtaksbrev) {
        this.vedtaksbrev = vedtaksbrev;
    }

    public Long getId() {
        return id;
    }

    public BehandlingResultatType getType() {
        return type;
    }

    public Avslagsårsak getAvslagsarsak() {
        return avslagsarsak;
    }

    public String getAvslagsarsakFritekst() {
        return avslagsarsakFritekst;
    }

    public RettenTil getRettenTil() {
        return rettenTil;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen;
    }

    public Vedtaksbrev getVedtaksbrev() {
        return vedtaksbrev;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public void setOverskrift(String overskrift) {
        this.overskrift = overskrift;
    }

    public String getFritekstbrev() {
        return fritekstbrev;
    }

    public void setFritekstbrev(String fritekstbrev) {
        this.fritekstbrev = fritekstbrev;
    }

    public LocalDate getSkjaeringstidspunktForeldrepenger() {
        return skjaeringstidspunktForeldrepenger;
    }

    public void setSkjaeringstidspunktForeldrepenger(LocalDate skjaeringstidspunktForeldrepenger) {
        this.skjaeringstidspunktForeldrepenger = skjaeringstidspunktForeldrepenger;
    }
}
