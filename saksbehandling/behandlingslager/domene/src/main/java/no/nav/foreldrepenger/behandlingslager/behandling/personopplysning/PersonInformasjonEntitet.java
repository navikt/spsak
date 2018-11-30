package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

@Entity(name = "PersonopplysningInformasjon")
@Table(name = "PO_INFORMASJON")
public class PersonInformasjonEntitet extends BaseEntitet implements PersonInformasjon {

    private static final String REF_NAME = "personopplysningInformasjon";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PO_INFORMASJON")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = REF_NAME)
    private List<PersonstatusEntitet> personstatuser = new ArrayList<>();

    @ChangeTracked
    @OneToMany(mappedBy = REF_NAME)
    private List<StatsborgerskapEntitet> statsborgerskap = new ArrayList<>();

    @ChangeTracked
    @OneToMany(mappedBy = REF_NAME)
    private List<PersonAdresseEntitet> adresser = new ArrayList<>();

    @ChangeTracked
    @OneToMany(mappedBy = REF_NAME)
    private List<PersonopplysningEntitet> personopplysninger = new ArrayList<>();

    PersonInformasjonEntitet() {
    }

    PersonInformasjonEntitet(PersonInformasjon aggregat) {
        if (Optional.ofNullable(aggregat.getAdresser()).isPresent()) {
            aggregat.getAdresser()
                .forEach(e -> {
                    PersonAdresseEntitet entitet = new PersonAdresseEntitet(e);
                    adresser.add(entitet);
                    entitet.setPersonopplysningInformasjon(this);
                });
        }
        if (Optional.ofNullable(aggregat.getPersonstatus()).isPresent()) {
            aggregat.getPersonstatus()
                .forEach(e -> {
                    PersonstatusEntitet entitet = new PersonstatusEntitet(e);
                    personstatuser.add(entitet);
                    entitet.setPersonInformasjon(this);
                });
        }
        if (Optional.ofNullable(aggregat.getStatsborgerskap()).isPresent()) {
            aggregat.getStatsborgerskap()
                .forEach(e -> {
                    StatsborgerskapEntitet entitet = new StatsborgerskapEntitet(e);
                    statsborgerskap.add(entitet);
                    entitet.setPersonopplysningInformasjon(this);
                });
        }
        if (Optional.ofNullable(aggregat.getPersonopplysninger()).isPresent()) {
            aggregat.getPersonopplysninger()
                .forEach(e -> {
                    PersonopplysningEntitet entitet = new PersonopplysningEntitet(e);
                    personopplysninger.add(entitet);
                    entitet.setPersonopplysningInformasjon(this);
                });
        }
    }

    void leggTilAdresse(PersonAdresse adresse) {
        final PersonAdresseEntitet adresse1 = (PersonAdresseEntitet) adresse;
        adresse1.setPersonopplysningInformasjon(this);
        adresser.add(adresse1);
    }

    void leggTilStatsborgerskap(Statsborgerskap statsborgerskap) {
        final StatsborgerskapEntitet statsborgerskap1 = (StatsborgerskapEntitet) statsborgerskap;
        statsborgerskap1.setPersonopplysningInformasjon(this);
        this.statsborgerskap.add(statsborgerskap1);
    }

    void leggTilPersonstatus(Personstatus personstatus) {
        final PersonstatusEntitet personstatus1 = (PersonstatusEntitet) personstatus;
        personstatus1.setPersonInformasjon(this);
        this.personstatuser.add(personstatus1);
    }

    void leggTilPersonopplysning(Personopplysning personopplysning) {
        final PersonopplysningEntitet personopplysning1 = (PersonopplysningEntitet) personopplysning;
        personopplysning1.setPersonopplysningInformasjon(this);
        this.personopplysninger.add(personopplysning1);
    }

    void fjernPersonopplysning(AktørId aktørId) {
        this.personopplysninger.removeIf(e -> e.getAktørId().equals(aktørId));
    }

    /**
     * Rydder bort alt unntatt personopplysninger
     */
    void tilbakestill() {
        this.adresser.clear();
        this.personstatuser.clear();
        this.statsborgerskap.clear();
    }

    PersonInformasjonBuilder.PersonopplysningBuilder getPersonBuilderForAktørId(AktørId aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        final Optional<PersonopplysningEntitet> eksisterendeAktør = personopplysninger.stream().filter(it -> it.getAktørId().equals(aktørId)).findFirst();
        return PersonInformasjonBuilder.PersonopplysningBuilder.oppdater(eksisterendeAktør).medAktørId(aktørId);
    }

    @Override
    public List<Personopplysning> getPersonopplysninger() {
        return Collections.unmodifiableList(personopplysninger);
    }

    @Override
    public List<Personstatus> getPersonstatus() {
        return Collections.unmodifiableList(personstatuser);
    }

    @Override
    public List<Statsborgerskap> getStatsborgerskap() {
        return Collections.unmodifiableList(statsborgerskap);
    }

    @Override
    public List<PersonAdresse> getAdresser() {
        return Collections.unmodifiableList(adresser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonInformasjonEntitet that = (PersonInformasjonEntitet) o;
        return Objects.equals(personstatuser, that.personstatuser) &&
            Objects.equals(statsborgerskap, that.statsborgerskap) &&
            Objects.equals(adresser, that.adresser) &&
            Objects.equals(personopplysninger, that.personopplysninger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personstatuser, statsborgerskap, adresser, personopplysninger);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PersonInformasjonEntitet{");
        sb.append("id=").append(id);
        sb.append(", personstatuser=").append(personstatuser);
        sb.append(", statsborgerskap=").append(statsborgerskap);
        sb.append(", adresser=").append(adresser);
        sb.append(", personopplysninger=").append(personopplysninger);
        sb.append('}');
        return sb.toString();
    }

    PersonInformasjonBuilder.AdresseBuilder getAdresseBuilderForAktørId(AktørId aktørId, AdresseType type, DatoIntervallEntitet periode) {
        final Optional<PersonAdresseEntitet> eksisterende = adresser.stream()
            .filter(it -> it.getAktørId().equals(aktørId) && it.getAdresseType().equals(type) && erSannsynligvisSammePeriode(it.getPeriode(), periode))
            .findAny();
        return PersonInformasjonBuilder.AdresseBuilder.oppdater(eksisterende).medAktørId(aktørId).medAdresseType(type).medPeriode(periode);
    }

    private boolean erSannsynligvisSammePeriode(DatoIntervallEntitet eksiterendePeriode, DatoIntervallEntitet nyPeriode) {
        return eksiterendePeriode.equals(nyPeriode) || eksiterendePeriode.getFomDato().equals(nyPeriode.getFomDato())
            && eksiterendePeriode.getTomDato().equals(Tid.TIDENES_ENDE) && !nyPeriode.getTomDato().equals(Tid.TIDENES_ENDE);
    }

    PersonInformasjonBuilder.StatsborgerskapBuilder getStatsborgerskapBuilderForAktørId(AktørId aktørId, Landkoder landkode, DatoIntervallEntitet periode, Region region) {
        final Optional<StatsborgerskapEntitet> eksisterende = statsborgerskap.stream()
            .filter(it -> it.getAktørId().equals(aktørId) && it.getStatsborgerskap().equals(landkode) && erSannsynligvisSammePeriode(it.getPeriode(), periode))
            .findAny();
        return PersonInformasjonBuilder.StatsborgerskapBuilder.oppdater(eksisterende).medAktørId(aktørId).medStatsborgerskap(landkode).medPeriode(periode).medRegion(region);
    }

    PersonInformasjonBuilder.PersonstatusBuilder getPersonstatusBuilderForAktørId(AktørId aktørId, DatoIntervallEntitet periode) {
        final Optional<PersonstatusEntitet> eksisterende = personstatuser.stream()
            .filter(it -> it.getAktørId().equals(aktørId) && erSannsynligvisSammePeriode(it.getPeriode(), periode))
            .findAny();
        return PersonInformasjonBuilder.PersonstatusBuilder.oppdater(eksisterende).medAktørId(aktørId).medPeriode(periode);
    }
}
