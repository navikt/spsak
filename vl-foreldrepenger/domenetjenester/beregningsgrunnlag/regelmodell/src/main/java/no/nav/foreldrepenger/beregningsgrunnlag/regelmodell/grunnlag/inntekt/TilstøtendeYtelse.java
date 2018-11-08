package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Dekningsgrad;

public class TilstøtendeYtelse {

    private List<TilstøtendeYtelseAndel> tilstøtendeYtelseAndelList = new ArrayList<>();
    private RelatertYtelseType relatertYtelseType;
    private Dekningsgrad dekningsgrad;
    private LocalDate opprinneligSkjæringstidspunkt;
    private List<Inntektskategori> inntektskategoriListe;
    private boolean erKildeInfotrygd;

    private TilstøtendeYtelse() {
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public LocalDate getOpprinneligSkjæringstidspunkt() {
        return opprinneligSkjæringstidspunkt;
    }

    public List<TilstøtendeYtelseAndel> getTilstøtendeYtelseAndelList() {
        return Collections.unmodifiableList(tilstøtendeYtelseAndelList);
    }

    public List<Inntektskategori> getInntektskategoriListe() {
        return Collections.unmodifiableList(inntektskategoriListe);
    }

    public boolean erKildeInfotrygd() {
        return erKildeInfotrygd;
    }

    public RelatertYtelseType getRelatertYtelseType() {
        return relatertYtelseType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TilstøtendeYtelse kladd;

        private Builder() {
            kladd = new TilstøtendeYtelse();
        }

        public Builder medDekningsgrad(Dekningsgrad dekningsgrad) {
            kladd.dekningsgrad = dekningsgrad;
            return this;
        }

        public Builder medRelatertYtelseType(RelatertYtelseType relatertYtelseType) {
            kladd.relatertYtelseType = relatertYtelseType;
            return this;
        }

        public Builder medOpprinneligSkjæringstidspunkt(LocalDate opprinneligSkjæringstidspunkt) {
            kladd.opprinneligSkjæringstidspunkt = opprinneligSkjæringstidspunkt;
            return this;
        }

        public TilstøtendeYtelse build() {
            return kladd;
        }

        public Builder leggTilArbeidsforhold(TilstøtendeYtelseAndel tilstøtendeYtelseAndel) {
            kladd.tilstøtendeYtelseAndelList.add(tilstøtendeYtelseAndel);
            return this;
        }

        public Builder medInntektskategorier(List<Inntektskategori> inntektskategoriListe) {
            kladd.inntektskategoriListe = inntektskategoriListe;
            return this;
        }

        public Builder medKildeInfotrygd(boolean erKildeInfotrygd) {
            kladd.erKildeInfotrygd = erKildeInfotrygd;
            return this;
        }
    }
}
