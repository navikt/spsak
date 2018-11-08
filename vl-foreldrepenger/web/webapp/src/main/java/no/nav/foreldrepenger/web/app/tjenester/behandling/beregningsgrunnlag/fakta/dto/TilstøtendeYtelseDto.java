
package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.fakta.dto;

import java.math.BigDecimal;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;

public class TilstøtendeYtelseDto {

    private List<TilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler;
    private Long dekningsgrad;
    private Arbeidskategori arbeidskategori;
    private RelatertYtelseType ytelseType;
    private BigDecimal bruttoBG;
    private boolean skalReduseres;
    private boolean erBesteberegning;

    public TilstøtendeYtelseDto () {
        // Hibernate
    }

    public boolean isSkalReduseres() {
        return skalReduseres;
    }

    public void setSkalReduseres(boolean skalReduseres) {
        this.skalReduseres = skalReduseres;
    }

    public Long getDekningsgrad() {
        return dekningsgrad;
    }

    public void setDekningsgrad(Long dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public RelatertYtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(RelatertYtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public BigDecimal getBruttoBG() {
        return bruttoBG;
    }

    public void setBruttoBG(BigDecimal bruttoBG) {
        this.bruttoBG = bruttoBG;
    }

    public List<TilstøtendeYtelseAndelDto> getTilstøtendeYtelseAndeler() {
        return tilstøtendeYtelseAndeler;
    }

    public void setTilstøtendeYtelseAndeler(List<TilstøtendeYtelseAndelDto> tilstøtendeYtelseAndeler) {
        this.tilstøtendeYtelseAndeler = tilstøtendeYtelseAndeler;
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public void setArbeidskategori(Arbeidskategori arbeidskategori) {
        this.arbeidskategori = arbeidskategori;
    }

    public boolean getErBesteberegning() {
        return erBesteberegning;
    }

    public void setErBesteberegning(boolean erBesteberegning) {
        this.erBesteberegning = erBesteberegning;
    }
}
