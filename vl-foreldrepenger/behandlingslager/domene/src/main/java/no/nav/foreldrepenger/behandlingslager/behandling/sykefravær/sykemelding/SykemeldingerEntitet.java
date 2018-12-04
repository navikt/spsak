package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "SykemeldingerEntitet")
@Table(name = "SF_SYKEMELDINGER")
public class SykemeldingerEntitet extends BaseEntitet implements Sykemeldinger {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SF_SYKEMELDINGER")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = "sykemeldinger")
    private Set<SykemeldingEntitet> sykemeldinger = new HashSet<>();

    SykemeldingerEntitet() {
    }

    public SykemeldingerEntitet(Sykemeldinger sykemeldinger) {
        this.sykemeldinger = sykemeldinger.getSykemeldinger()
            .stream()
            .map(SykemeldingEntitet::new)
            .peek(sm -> sm.setSykemeldinger(this))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Sykemelding> getSykemeldinger() {
        return Collections.unmodifiableSet(sykemeldinger);
    }

    void leggTil(Sykemelding sykemelding) {
        SykemeldingEntitet entitet = (SykemeldingEntitet) sykemelding;
        this.sykemeldinger.removeIf(sm -> sm.getEksternReferanse().equals(sykemelding.getEksternReferanse()));
        this.sykemeldinger.add(entitet);
        entitet.setSykemeldinger(this);
    }
}
