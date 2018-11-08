package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class PersonInformasjonBuilder {

    private final PersonInformasjonEntitet kladd;
    private final PersonopplysningVersjonType type;
    private final boolean gjelderOppdatering;

    private AktørId søkerAktørId;
    private Optional<AktørId> annenpartAktørId;

    private PersonInformasjonBuilder(PersonInformasjonEntitet personInfoAggregatEntitet, PersonopplysningVersjonType type, boolean gjelderOppdatering) {
        this.kladd = personInfoAggregatEntitet;
        this.type = type;
        this.gjelderOppdatering = gjelderOppdatering;
    }

    private static PersonInformasjonBuilder nytt(PersonopplysningVersjonType type) {
        return new PersonInformasjonBuilder(new PersonInformasjonEntitet(), type, false);
    }

    private static PersonInformasjonBuilder oppdater(PersonInformasjon personInformasjon, PersonopplysningVersjonType type) {
        return new PersonInformasjonBuilder(new PersonInformasjonEntitet(personInformasjon), type, true);
    }

    public static PersonInformasjonBuilder oppdater(Optional<PersonInformasjon> aggregat, PersonopplysningVersjonType type) {
        return aggregat.map(e -> oppdater(e, type)).orElseGet(() -> nytt(type));
    }

    public PersonInformasjonBuilder leggTil(AdresseBuilder builder) {
        if (!builder.getErOppdatering()) {
            kladd.leggTilAdresse(builder.build());
        }
        return this;
    }

    public PersonInformasjonBuilder leggTil(PersonstatusBuilder builder) {
        if (!builder.getErOppdatering()) {
            kladd.leggTilPersonstatus(builder.build());
        }
        return this;
    }

    public PersonInformasjonBuilder leggTil(StatsborgerskapBuilder builder) {
        if (!builder.getErOppdatering()) {
            kladd.leggTilStatsborgerskap(builder.build());
        }
        return this;
    }

    public PersonInformasjonBuilder leggTil(PersonopplysningBuilder builder) {
        if (!builder.getErOppdatering()) {
            kladd.leggTilPersonopplysning(builder.build());
        }
        return this;
    }

    public PersonInformasjonBuilder leggTil(RelasjonBuilder builder) {
        if (!builder.getErOppdatering()) {
            kladd.leggTilPersonrelasjon(builder.build());
        }
        return this;
    }

    public PersonInformasjon build() {
        ryddBortGamlePersonopplysninger();
        return kladd;
    }

    private void ryddBortGamlePersonopplysninger() {
        if (gjelderOppdatering() && søkerAktørId != null) {
            Set<AktørId> aktørerIRelasjoner = kladd.getRelasjoner().stream().map(e -> e.getAktørId()).collect(Collectors.toSet());
            aktørerIRelasjoner.addAll(kladd.getRelasjoner().stream().map(e -> e.getTilAktørId()).collect(Collectors.toSet()));

            Set<AktørId> personer = kladd.getPersonopplysninger()
                .stream()
                .filter(e -> !søkerAktørId.equals(e.getAktørId()))
                .filter(e -> annenpartAktørId.isPresent() ? !annenpartAktørId.get().equals(e.getAktørId()) : true)
                .map(e -> e.getAktørId()).collect(Collectors.toSet());
            personer.forEach(e -> {
                if (!aktørerIRelasjoner.contains(e)) {
                    kladd.fjernPersonopplysning(e);
                }
            });
        }
    }

    public void tilbakestill(AktørId søkerAktørId, Optional<AktørId> annenpartAktørId) {
        if (gjelderOppdatering()) {
            this.søkerAktørId = søkerAktørId;
            this.annenpartAktørId = annenpartAktørId;
            kladd.tilbakestill();
        }
    }

    boolean gjelderOppdatering() {
        return gjelderOppdatering;
    }

    public boolean harIkkeFåttPersonstatusHistorikk(AktørId aktørId) {
        return kladd.getPersonstatus().stream().noneMatch(it -> it.getAktørId().equals(aktørId));
    }

    public boolean harIkkeFåttStatsborgerskapHistorikk(AktørId aktørId) {
        return kladd.getStatsborgerskap().stream().noneMatch(it -> it.getAktørId().equals(aktørId));
    }

    public boolean harIkkeFåttAdresseHistorikk(AktørId aktørId) {
        return kladd.getAdresser().stream().noneMatch(it -> it.getAktørId().equals(aktørId));
    }

    public PersonopplysningVersjonType getType() {
        return type;
    }

    public PersonopplysningBuilder getPersonopplysningBuilder(AktørId aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        return kladd.getPersonBuilderForAktørId(aktørId);
    }

    public AdresseBuilder getAdresseBuilder(AktørId aktørId, DatoIntervallEntitet periode, AdresseType type) {
        return kladd.getAdresseBuilderForAktørId(aktørId, type, periode);
    }

    public PersonstatusBuilder getPersonstatusBuilder(AktørId aktørId, DatoIntervallEntitet periode) {
        return kladd.getPersonstatusBuilderForAktørId(aktørId, periode);
    }

    public RelasjonBuilder getRelasjonBuilder(AktørId fraAktør, AktørId tilAktør, RelasjonsRolleType rolle) {
        Objects.requireNonNull(fraAktør, "fraAktør");
        Objects.requireNonNull(tilAktør, "tilAktør");
        Objects.requireNonNull(rolle, "rolle");
        return kladd.getRelasjonBuilderForAktørId(fraAktør, tilAktør, rolle);
    }

    public StatsborgerskapBuilder getStatsborgerskapBuilder(AktørId aktørId, DatoIntervallEntitet periode, Landkoder landkode, Region region) {
        return kladd.getStatsborgerskapBuilderForAktørId(aktørId, landkode, periode,region);
    }

    public static final class PersonopplysningBuilder {
        private final PersonopplysningEntitet kladd;
        private final boolean oppdatering;

        private PersonopplysningBuilder(PersonopplysningEntitet kladd, boolean oppdatering) {
            this.kladd = kladd;
            this.oppdatering = oppdatering;
        }

        private static PersonopplysningBuilder oppdatere(PersonopplysningEntitet kladd) {
            return new PersonopplysningBuilder(kladd, true);
        }

        private static PersonopplysningBuilder ny() {
            return new PersonopplysningBuilder(new PersonopplysningEntitet(), false);
        }

        static PersonopplysningBuilder oppdater(Optional<PersonopplysningEntitet> aggregat) {
            return aggregat.map(PersonopplysningBuilder::oppdatere).orElseGet(PersonopplysningBuilder::ny);
        }

        public PersonopplysningBuilder medAktørId(AktørId aktørId) {
            kladd.setAktørId(aktørId);
            return this;
        }

        public PersonopplysningBuilder medKjønn(NavBrukerKjønn brukerKjønn) {
            kladd.setBrukerKjønn(brukerKjønn);
            return this;
        }

        public PersonopplysningBuilder medSivilstand(SivilstandType sivilstand) {
            kladd.setSivilstand(sivilstand);
            return this;
        }

        public PersonopplysningBuilder medNavn(String navn) {
            kladd.setNavn(navn);
            return this;
        }

        public PersonopplysningBuilder medFødselsdato(LocalDate fødselsdato) {
            kladd.setFødselsdato(fødselsdato);
            return this;
        }

        public PersonopplysningBuilder medDødsdato(LocalDate dødsdato) {
            kladd.setDødsdato(dødsdato);
            return this;
        }

        public PersonopplysningBuilder medRegion(Region region) {
            kladd.setRegion(region);
            return this;
        }

        public Personopplysning build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static final class AdresseBuilder {

        private final PersonAdresseEntitet kladd;
        private final boolean oppdatering;

        private AdresseBuilder(PersonAdresseEntitet kladd, boolean oppdatering) {
            this.kladd = kladd;
            this.oppdatering = oppdatering;
        }

        private static AdresseBuilder ny() {
            return new AdresseBuilder(new PersonAdresseEntitet(), false);
        }

        private static AdresseBuilder oppdatere(PersonAdresseEntitet entitet) {
            return new AdresseBuilder(entitet, true);
        }

        static AdresseBuilder oppdater(Optional<PersonAdresseEntitet> entitet) {
            return entitet.map(AdresseBuilder::oppdatere).orElseGet(AdresseBuilder::ny);
        }

        AdresseBuilder medAktørId(AktørId aktørId) {
            kladd.setAktørId(aktørId);
            return this;
        }

        public AdresseBuilder medPeriode(DatoIntervallEntitet periode) {
            kladd.setPeriode(periode);
            return this;
        }

        public AdresseBuilder medAdresseType(AdresseType adresseType) {
            kladd.setAdresseType(adresseType);
            return this;
        }

        public AdresseBuilder medAdresselinje1(String adresselinje1) {
            kladd.setAdresselinje1(adresselinje1);
            return this;
        }

        public AdresseBuilder medAdresselinje2(String adresselinje2) {
            kladd.setAdresselinje2(adresselinje2);
            return this;
        }

        public AdresseBuilder medAdresselinje3(String adresselinje3) {
            kladd.setAdresselinje3(adresselinje3);
            return this;
        }

        public AdresseBuilder medAdresselinje4(String adresselinje4) {
            kladd.setAdresselinje4(adresselinje4);
            return this;
        }

        public AdresseBuilder medPostnummer(String postnummer) {
            kladd.setPostnummer(postnummer);
            return this;
        }

        public AdresseBuilder medPoststed(String poststed) {
            kladd.setPoststed(poststed);
            return this;
        }

        public AdresseBuilder medLand(String land) {
            kladd.setLand(land);
            return this;
        }

        public PersonAdresse build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static final class PersonstatusBuilder {

        private final PersonstatusEntitet kladd;
        private final boolean oppdatering;

        private PersonstatusBuilder(PersonstatusEntitet kladd, boolean oppdatering) {
            this.kladd = kladd;
            this.oppdatering = oppdatering;
        }

        private static PersonstatusBuilder ny() {
            return new PersonstatusBuilder(new PersonstatusEntitet(), false);
        }

        private static PersonstatusBuilder oppdatere(PersonstatusEntitet entitet) {
            return new PersonstatusBuilder(entitet, true);
        }

        static PersonstatusBuilder oppdater(Optional<PersonstatusEntitet> entitet) {
            return entitet.map(PersonstatusBuilder::oppdatere).orElseGet(PersonstatusBuilder::ny);
        }

        public PersonstatusBuilder medAktørId(AktørId aktørId) {
            kladd.setAktørId(aktørId);
            return this;
        }

        public PersonstatusBuilder medPeriode(DatoIntervallEntitet periode) {
            kladd.setPeriode(periode);
            return this;
        }

        public PersonstatusBuilder medPersonstatus(PersonstatusType personstatus) {
            kladd.setPersonstatus(personstatus);
            return this;
        }

        public Personstatus build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static final class RelasjonBuilder {

        private final PersonRelasjonEntitet kladd;
        private final boolean oppdatering;

        private RelasjonBuilder(PersonRelasjonEntitet kladd, boolean oppdatering) {
            this.kladd = kladd;
            this.oppdatering = oppdatering;
        }

        private static RelasjonBuilder ny() {
            return new RelasjonBuilder(new PersonRelasjonEntitet(), false);
        }

        private static RelasjonBuilder oppdatere(PersonRelasjonEntitet entitet) {
            return new RelasjonBuilder(entitet, true);
        }

        static RelasjonBuilder oppdater(Optional<PersonRelasjonEntitet> aggregat) {
            return aggregat.map(RelasjonBuilder::oppdatere).orElseGet(RelasjonBuilder::ny);
        }

        public RelasjonBuilder fraAktør(AktørId fraAktørId) {
            kladd.setFraAktørId(fraAktørId);
            return this;
        }

        public RelasjonBuilder tilAktør(AktørId tilAktørId) {
            kladd.setTilAktørId(tilAktørId);
            return this;
        }

        public RelasjonBuilder medRolle(RelasjonsRolleType relasjonsrolle) {
            kladd.setRelasjonsrolle(relasjonsrolle);
            return this;
        }

        public RelasjonBuilder harSammeBosted(Boolean harSammeBosted) {
            kladd.setHarSammeBosted(harSammeBosted);
            return this;
        }

        public PersonRelasjon build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static final class StatsborgerskapBuilder {
        private final StatsborgerskapEntitet kladd;
        private final boolean oppdatering;

        private StatsborgerskapBuilder(StatsborgerskapEntitet kladd, boolean oppdatering) {
            this.kladd = kladd;
            this.oppdatering = oppdatering;
        }

        private static StatsborgerskapBuilder ny() {
            return new StatsborgerskapBuilder(new StatsborgerskapEntitet(), false);
        }

        private static StatsborgerskapBuilder oppdatere(StatsborgerskapEntitet entitet) {
            return new StatsborgerskapBuilder(entitet, true);
        }

        static StatsborgerskapBuilder oppdater(Optional<StatsborgerskapEntitet> entitet) {
            return entitet.map(StatsborgerskapBuilder::oppdatere).orElseGet(StatsborgerskapBuilder::ny);
        }

        public StatsborgerskapBuilder medAktørId(AktørId aktørId) {
            kladd.setAktørId(aktørId);
            return this;
        }

        public StatsborgerskapBuilder medPeriode(DatoIntervallEntitet gyldighetsperiode) {
            kladd.setPeriode(gyldighetsperiode);
            return this;
        }

        public StatsborgerskapBuilder medStatsborgerskap(Landkoder statsborgerskap) {
            kladd.setStatsborgerskap(statsborgerskap);
            return this;
        }

        public StatsborgerskapBuilder medRegion(Region region){
            kladd.setRegion(region);
            return this;
        }

        public Statsborgerskap build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }
}
