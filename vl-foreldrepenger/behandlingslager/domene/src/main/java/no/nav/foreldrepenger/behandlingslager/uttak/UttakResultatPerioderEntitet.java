package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity
@Table(name = "UTTAK_RESULTAT_PERIODER")
public class UttakResultatPerioderEntitet extends BaseEntitet {
    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_RESULTAT_PERIODER")
    private Long id;

    @OneToMany(mappedBy = "perioder")
    private List<UttakResultatPeriodeEntitet> perioder = new ArrayList<>();

    public void leggTilPeriode(UttakResultatPeriodeEntitet periode) {
        validerIkkeOverlapp(periode);
        perioder.add(periode);
        periode.setPerioder(this);
    }

    private void validerIkkeOverlapp(UttakResultatPeriodeEntitet p2) {
        for (UttakResultatPeriodeEntitet p1 : perioder) {
            if (p1.getTidsperiode().overlapper(p2.getTidsperiode())) {
                throw new IllegalArgumentException("UttakResultatPerioder kan ikke overlappe " + p2 + p1);
            }
        }
    }

    public List<UttakResultatPeriodeEntitet> getPerioder() {
        return perioder.stream().sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom)).collect(Collectors.toList());
    }
}
