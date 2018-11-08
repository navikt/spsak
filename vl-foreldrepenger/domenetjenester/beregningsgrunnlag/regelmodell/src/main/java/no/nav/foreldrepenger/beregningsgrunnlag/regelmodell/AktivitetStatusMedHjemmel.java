package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.util.Objects;

public class AktivitetStatusMedHjemmel implements Comparable<AktivitetStatusMedHjemmel> {
    private AktivitetStatus aktivitetStatus;
    private BeregningsgrunnlagHjemmel hjemmel;

    public AktivitetStatusMedHjemmel(AktivitetStatus aktivitetStatus, BeregningsgrunnlagHjemmel hjemmel) {
        super();
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        this.aktivitetStatus = aktivitetStatus;
        this.hjemmel = hjemmel;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }
    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }
    public BeregningsgrunnlagHjemmel getHjemmel() {
        return hjemmel;
    }
    public void setHjemmel(BeregningsgrunnlagHjemmel hjemmel) {
        this.hjemmel = hjemmel;
    }

    public boolean inneholder(AktivitetStatus andelStatus) {
        if (aktivitetStatus.equals(andelStatus)) {
            return true;
        }
        switch (aktivitetStatus) {
            case ATFL_SN:
                return AktivitetStatus.SN.equals(andelStatus) || AktivitetStatus.ATFL.equals(andelStatus);
            case TY:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AktivitetStatusMedHjemmel)) {
            return false;
        }
        return aktivitetStatus.equals(((AktivitetStatusMedHjemmel)other).aktivitetStatus);
    }

    @Override
    public int compareTo(AktivitetStatusMedHjemmel other) {
        return aktivitetStatus.compareTo(other.aktivitetStatus);
    }

    @Override
    public int hashCode() {
        return aktivitetStatus.hashCode();
    }
}
