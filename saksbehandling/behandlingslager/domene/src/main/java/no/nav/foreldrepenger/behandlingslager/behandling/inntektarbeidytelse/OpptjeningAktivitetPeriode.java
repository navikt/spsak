package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class OpptjeningAktivitetPeriode {
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private Opptjeningsnøkkel grupperingNøkkel;
    private String orgnr;
    private Stillingsprosent stillingsprosent;
    private DatoIntervallEntitet periode;
    private VurderingsStatus vurderingsStatus;
    private String begrunnelse;
    private Boolean manueltBehandlet = false;

    private OpptjeningAktivitetPeriode() {
    }

    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Opptjeningsnøkkel getOpptjeningsnøkkel() {
        return grupperingNøkkel;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public Stillingsprosent getStillingsprosent() {
        return stillingsprosent;
    }

    public VurderingsStatus getVurderingsStatus() {
        return vurderingsStatus;
    }

    public void setVurderingsStatus(VurderingsStatus vurderingsStatus) {
        this.vurderingsStatus = vurderingsStatus;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public boolean erManueltBehandlet() {
        return manueltBehandlet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpptjeningAktivitetPeriode other = (OpptjeningAktivitetPeriode) o;
        return Objects.equals(opptjeningAktivitetType, other.opptjeningAktivitetType) &&
            Objects.equals(grupperingNøkkel, other.grupperingNøkkel) &&
            Objects.equals(begrunnelse, other.begrunnelse);
    }

    @Override
    public int hashCode() {

        return Objects.hash(opptjeningAktivitetType, grupperingNøkkel, begrunnelse);
    }

    public static class Builder {
        OpptjeningAktivitetPeriode kladd;

        private Builder(OpptjeningAktivitetPeriode periode) {
            kladd = periode;
        }

        public static Builder ny() {
            return new Builder(new OpptjeningAktivitetPeriode());
        }

        public Builder medPeriode(DatoIntervallEntitet periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType type) {
            Objects.requireNonNull(type, "opptjeningAktivitetType");
            kladd.opptjeningAktivitetType = type;
            return this;
        }

        public Builder medOpptjeningsnøkkel(Opptjeningsnøkkel opptjeningsnøkkel) {
            kladd.grupperingNøkkel = opptjeningsnøkkel;
            return this;
        }

        public Builder medOrgnr(String orgnr) {
            kladd.orgnr = orgnr;
            return this;
        }

        public Builder medStillingsandel(Stillingsprosent stillingsprosent) {
            kladd.stillingsprosent = stillingsprosent;
            return this;
        }

        public Builder medVurderingsStatus(VurderingsStatus status) {
            kladd.vurderingsStatus = status;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            kladd.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medErManueltBehandlet() {
            kladd.manueltBehandlet = true;
            return this;
        }

        public OpptjeningAktivitetPeriode build() {
            return kladd;
        }
    }
}
