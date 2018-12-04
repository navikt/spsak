package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Representerer et grunnlag av medlemskap opplysninger brukt i en Behandling. De ulike aggregatene (fra register, fra
 * vurdering, fra søker) kan gjenbrukes på tvers av Behandlinger, mens grunnlaget tilhører en Behandling.
 *
 */
@Entity(name = "MedlemskapBehandlingsgrunnlag")
@Table(name = "GR_MEDLEMSKAP")
public class MedlemskapBehandlingsgrunnlagEntitet extends BaseEntitet {

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private Boolean aktiv = true;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til behandling! */ }, fetch = FetchType.LAZY)
    @JoinColumn(name = "behandling_id", updatable = false, nullable = false)
    private Behandling behandling;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_MEDLEMSKAP")
    @Column(columnDefinition = "NUMERIC")
    private Long id;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til et selvstendig aggregat! */ }, fetch = FetchType.EAGER)
    @JoinColumn(name = "OPPGITT_ID", nullable = true, unique = true, columnDefinition = "NUMERIC")
    private OppgittTilknytningEntitet oppgittTilknytning;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til et selvstendig aggregat! */ }, fetch = FetchType.EAGER)
    @JoinColumn(name = "REGISTRERT_ID", nullable = true, unique = true, columnDefinition = "NUMERIC")
    @ChangeTracked
    private MedlemskapRegistrertEntitet registerMedlemskap;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til et selvstendig aggregat! */ }, fetch = FetchType.EAGER)
    @JoinColumn(name = "VURDERING_ID", nullable = true, unique = true, columnDefinition = "NUMERIC")
    private VurdertMedlemskapEntitet vurderingMedlemskapSkjæringstidspunktet;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til et selvstendig aggregat! */ }, fetch = FetchType.EAGER)
    @JoinColumn(name = "VURDERING_LOPENDE_ID", nullable = true, unique = true, columnDefinition = "NUMERIC")
    private VurdertMedlemskapPeriodeEntitet vurderingLøpendeMedlemskap;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    MedlemskapBehandlingsgrunnlagEntitet() {
        // default tom entitet
    }

    MedlemskapBehandlingsgrunnlagEntitet(Behandling behandling) {
        this.behandling = behandling;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        } else if (!(obj instanceof MedlemskapBehandlingsgrunnlagEntitet)) {
            return false;
        }
        MedlemskapBehandlingsgrunnlagEntitet that = (MedlemskapBehandlingsgrunnlagEntitet) obj;

        return Objects.equals(this.getBehandling(), that.getBehandling())
                && Objects.equals(this.registerMedlemskap, that.registerMedlemskap)
                && Objects.equals(this.oppgittTilknytning, that.oppgittTilknytning)
                && Objects.equals(this.vurderingMedlemskapSkjæringstidspunktet, that.vurderingMedlemskapSkjæringstidspunktet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBehandling(), this.registerMedlemskap, this.oppgittTilknytning, this.vurderingMedlemskapSkjæringstidspunktet);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<id=" + getId() //$NON-NLS-1$
                + ", vurdertMedlemskap=" + this.vurderingMedlemskapSkjæringstidspunktet //$NON-NLS-1$
                + ", oppgittTilknytning=" + this.oppgittTilknytning //$NON-NLS-1$
                + ", registerMedlemskap=" + this.registerMedlemskap //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public Behandling getBehandling() {
        return behandling;
    }

    /* eksponeres ikke public for andre. */
    Long getId() {
        return id;
    }

    OppgittTilknytningEntitet getOppgittTilknytning() {
        return oppgittTilknytning;
    }

    MedlemskapRegistrertEntitet getRegisterMedlemskap() {
        return registerMedlemskap;
    }

    Set<RegistrertMedlemskapPerioder> getRegistertMedlemskapPerioder() {
        if (registerMedlemskap == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(registerMedlemskap.getMedlemskapPerioder());
        }
    }

    VurdertMedlemskapEntitet getVurderingMedlemskapSkjæringstidspunktet() {
        return vurderingMedlemskapSkjæringstidspunktet;
    }

    void setAktiv(final boolean aktiv) {
        this.aktiv = aktiv;
    }

    MedlemskapAggregat tilAggregat() {
        return new MedlemskapAggregat(
            this.getVurderingMedlemskapSkjæringstidspunktet(),
            this.getRegistertMedlemskapPerioder(),
            this.getOppgittTilknytning(),
            this.getVurderingLøpendeMedlemskap());
    }

    private static MedlemskapBehandlingsgrunnlagEntitet kopierTidligerGrunnlag(
            Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt, Behandling nyBehandling) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = new MedlemskapBehandlingsgrunnlagEntitet(nyBehandling);

        if (tidligereGrunnlagOpt.isPresent()) {
            MedlemskapBehandlingsgrunnlagEntitet tidligereGrunnlag = tidligereGrunnlagOpt.get();
            nyttGrunnlag.oppgittTilknytning = tidligereGrunnlag.oppgittTilknytning;
            nyttGrunnlag.vurderingMedlemskapSkjæringstidspunktet = tidligereGrunnlag.vurderingMedlemskapSkjæringstidspunktet;
            nyttGrunnlag.registerMedlemskap = tidligereGrunnlag.registerMedlemskap;
        }
        return nyttGrunnlag;
    }

    static MedlemskapBehandlingsgrunnlagEntitet fra(Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt, Behandling nyBehandling,
            MedlemskapRegistrertEntitet nyeData) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = kopierTidligerGrunnlag(tidligereGrunnlagOpt, nyBehandling);
        nyttGrunnlag.registerMedlemskap = nyeData;
        return nyttGrunnlag;
    }

    static MedlemskapBehandlingsgrunnlagEntitet fra(Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt,Behandling nyBehandling,
            OppgittTilknytningEntitet nyeData) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = kopierTidligerGrunnlag(tidligereGrunnlagOpt, nyBehandling);
        nyttGrunnlag.oppgittTilknytning = nyeData;
        return nyttGrunnlag;
    }

    static MedlemskapBehandlingsgrunnlagEntitet fra(Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt, Behandling nyBehandling,
            VurdertMedlemskapEntitet nyeData) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = kopierTidligerGrunnlag(tidligereGrunnlagOpt, nyBehandling);
        nyttGrunnlag.vurderingMedlemskapSkjæringstidspunktet = nyeData;
        return nyttGrunnlag;
    }

    static MedlemskapBehandlingsgrunnlagEntitet fra(Optional<MedlemskapBehandlingsgrunnlagEntitet> tidligereGrunnlagOpt,
                                                           Behandling nyBehandling, VurdertMedlemskapPeriodeEntitet nyeData) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = kopierTidligerGrunnlag(tidligereGrunnlagOpt, nyBehandling);
        nyttGrunnlag.vurderingLøpendeMedlemskap = nyeData;
        return nyttGrunnlag;
    }

    static MedlemskapBehandlingsgrunnlagEntitet fra(Optional<MedlemskapBehandlingsgrunnlagEntitet> eksisterendeGrunnlag, Behandling nyBehandling) {
        return kopierTidligerGrunnlag(eksisterendeGrunnlag, nyBehandling);
    }

    static MedlemskapBehandlingsgrunnlagEntitet forRevurdering(Optional<MedlemskapBehandlingsgrunnlagEntitet> eksisterendeGrunnlag, Behandling nyBehandling) {
        MedlemskapBehandlingsgrunnlagEntitet nyttGrunnlag = new MedlemskapBehandlingsgrunnlagEntitet(nyBehandling);

        if (eksisterendeGrunnlag.isPresent()) {
            MedlemskapBehandlingsgrunnlagEntitet tidligereGrunnlag = eksisterendeGrunnlag.get();
            nyttGrunnlag.oppgittTilknytning = tidligereGrunnlag.oppgittTilknytning;
        }
        return nyttGrunnlag;
    }

    public VurdertMedlemskapPeriodeEntitet getVurderingLøpendeMedlemskap() {
        return vurderingLøpendeMedlemskap;
    }
}
