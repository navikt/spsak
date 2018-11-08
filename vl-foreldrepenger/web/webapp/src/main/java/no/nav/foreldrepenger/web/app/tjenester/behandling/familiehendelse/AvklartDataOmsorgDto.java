package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import java.time.LocalDate;

import javax.validation.Valid;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

public class AvklartDataOmsorgDto extends FamiliehendelseDto {

    private LocalDate omsorgsovertakelseDato;
    
    @Valid
    private OmsorgsovertakelseVilkårType vilkarType;
    
    private Integer antallBarnTilBeregning;
    private LocalDate foreldreansvarDato;

    public AvklartDataOmsorgDto() {
        // trengs for deserialisering av JSON
        super();
    }

    public AvklartDataOmsorgDto(SøknadType søknadType) {
        super(søknadType);
    }

    public LocalDate getForeldreansvarDato() { return foreldreansvarDato;}

    public LocalDate getOmsorgsovertakelseDato() {
        return omsorgsovertakelseDato;
    }

    public OmsorgsovertakelseVilkårType getVilkarType() {
        return vilkarType;
    }

    public Integer getAntallBarnTilBeregning() {
        return antallBarnTilBeregning;
    }


    public void setForeldreansvarDato(LocalDate foreldreansvarDato) {this.foreldreansvarDato = foreldreansvarDato;}

    void setOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
        this.omsorgsovertakelseDato = omsorgsovertakelseDato;
    }

    void setVilkarType(OmsorgsovertakelseVilkårType vilkarType) {
        this.vilkarType = vilkarType;
    }

    void setAntallBarnTilBeregning(Integer antallBarnTilBeregning) {
        this.antallBarnTilBeregning = antallBarnTilBeregning;
    }
}
