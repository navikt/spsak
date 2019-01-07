package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;


public class FastsattBeløpTilstøtendeYtelseAndelDto extends RedigerbarAndelDto {

    @NotNull
    @Min(0)
    @Max(1)
    private Double reduserendeFaktor;
    @NotNull
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer fastsattBeløp;
    @NotNull
    @ValidKodeverk
    private Inntektskategori inntektskategori;
    @Min(0)
    @Max(Integer.MAX_VALUE)
    private Integer refusjonskravPrAar;

    FastsattBeløpTilstøtendeYtelseAndelDto() { // NOSONAR
        // Jackson
    }

    public FastsattBeløpTilstøtendeYtelseAndelDto(RedigerbarAndelDto andelDto,
                                                  Integer fastsattBeløp,
                                                  Integer refusjonskravPrAar,
                                                  Inntektskategori inntektskategori,
                                                  Double reduserendeFaktor) {
        super(andelDto.getAndel(), andelDto.getNyAndel(), andelDto.getArbeidsforholdId(),
            andelDto.getAndelsnr(), andelDto.getLagtTilAvSaksbehandler());
        this.fastsattBeløp = fastsattBeløp;
        this.inntektskategori = inntektskategori;
        this.refusjonskravPrAar = refusjonskravPrAar;
        this.reduserendeFaktor = reduserendeFaktor;
    }

    public Integer getFastsattBeløp() {
        return fastsattBeløp;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Optional<Integer> getRefusjonskravPrAar() { return Optional.ofNullable(refusjonskravPrAar); }

    public void setReduserendeFaktor(Double reduserendeFaktor) {
        this.reduserendeFaktor = reduserendeFaktor;
    }

    public void setFastsattBeløp(Integer fastsattBeløp) {
        this.fastsattBeløp = fastsattBeløp;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public void setRefusjonskravPrAar(Integer refusjonskravPrAar) {
        this.refusjonskravPrAar = refusjonskravPrAar;
    }

    public Double getReduserendeFaktor() {
        return reduserendeFaktor;
    }


    public Beløp finnRedusertBeløp() {
        return new Beløp(fastsattBeløp).multipliser(reduserendeFaktor);
    }
}
