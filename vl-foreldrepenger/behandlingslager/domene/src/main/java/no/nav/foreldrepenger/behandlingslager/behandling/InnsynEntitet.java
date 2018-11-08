package no.nav.foreldrepenger.behandlingslager.behandling;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "Innsyn")
@Table(name = "INNSYN")
public class InnsynEntitet extends BaseEntitet implements InnsynResultat<InnsynDokumentEntitet> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNSYN")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Column(name = "mottatt_dato", nullable = false)
    private LocalDate mottattDato;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "innsyn_resultat_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InnsynResultatType.DISCRIMINATOR + "'"))
    private InnsynResultatType innsynResultatType = InnsynResultatType.UDEFINERT;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "innsyn")
    private Set<InnsynDokumentEntitet> innsynDokumenter = new HashSet<>(1);

    InnsynEntitet() {
        // for hibernate
    }

    InnsynEntitet(InnsynResultat<?> innsynResultat) {
        Objects.requireNonNull(innsynResultat.getMottattDato(), "mottatt dato må være satt");
        Objects.requireNonNull(innsynResultat.getInnsynResultatType(), "innsynresultat må være satt");
        Objects.requireNonNull(innsynResultat.getBegrunnelse(), "begrunnelse må være satt");
        this.mottattDato = innsynResultat.getMottattDato();
        this.innsynResultatType = innsynResultat.getInnsynResultatType();
        this.begrunnelse = innsynResultat.getBegrunnelse();
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    @Override
    public LocalDate getMottattDato() {
        return mottattDato;
    }

    @Override
    public InnsynResultatType getInnsynResultatType() {
        return innsynResultatType;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Set<InnsynDokumentEntitet> getInnsynDokumenter() {
        return innsynDokumenter;
    }

    @Override
    public String getBegrunnelse() {
        return begrunnelse;
    }

    @Override
    public String toString() {
        return "InnsynEntitet{" +
            "id=" + id +
            ", innsynResultatType=" + innsynResultatType +
            ", innsynDokumenter=" + innsynDokumenter +
            '}';
    }

    public static class InnsynBuilder {

        private final InnsynEntitet kladd;

        public InnsynBuilder(InnsynEntitet innsyn) {
            this.kladd = innsyn;
        }

        public static InnsynBuilder builder() {
            return new InnsynBuilder(new InnsynEntitet());
        }

        public InnsynBuilder medMottattDato(LocalDate mottattDato) {
            this.kladd.mottattDato = mottattDato;
            return this;
        }

        public InnsynBuilder medInnsynResultatType(InnsynResultatType innsynResultatType) {
            this.kladd.innsynResultatType = innsynResultatType;
            return this;
        }

        public InnsynBuilder medBegrunnelse(String begrunnelse) {
            this.kladd.begrunnelse = begrunnelse;
            return this;
        }

        public InnsynBuilder medDokumenter(List<InnsynDokumentEntitet> innsynDokumenter) {
            kladd.innsynDokumenter.clear();
            for (InnsynDokumentEntitet innsynDokument : innsynDokumenter) {
                this.kladd.innsynDokumenter.add(innsynDokument);
            }
            return this;
        }

        public InnsynEntitet buildFor(Behandling behandling) {
            InnsynEntitet innsyn = behandling.getInnsyn();
            if (innsyn != null) {
                innsyn.innsynResultatType = kladd.innsynResultatType;
                innsyn.mottattDato = kladd.mottattDato;
            } else {
                innsyn = kladd;
            }
            behandling.setInnsyn(innsyn);

            return innsyn;
        }


    }
}
