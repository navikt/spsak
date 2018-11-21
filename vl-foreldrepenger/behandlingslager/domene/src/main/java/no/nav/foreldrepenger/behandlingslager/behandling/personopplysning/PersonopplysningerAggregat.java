package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class PersonopplysningerAggregat {

    private final AktørId søkerAktørId;
    private final List<Personopplysning> allePersonopplysninger;
    private final List<PersonRelasjon> alleRelasjoner;
    private final List<PersonAdresse> aktuelleAdresser;
    private final List<Personstatus> aktuellePersonstatus;
    private final List<Personstatus> overstyrtPersonstatus;
    private final List<Personstatus> orginalPersonstatus;
    private final List<Statsborgerskap> aktuelleStatsborgerskap;
    private final DatoIntervallEntitet forPeriode;

    public PersonopplysningerAggregat(PersonopplysningGrunnlag grunnlag, AktørId aktørId, DatoIntervallEntitet forPeriode, Map<Landkoder, Region> landkoderRegionMap) {
        this.søkerAktørId = aktørId;
        this.forPeriode = forPeriode;
        if (grunnlag.getRegisterVersjon() != null) {
            this.alleRelasjoner = grunnlag.getRegisterVersjon().getRelasjoner();
            this.allePersonopplysninger = grunnlag.getRegisterVersjon().getPersonopplysninger();
            this.aktuelleAdresser = grunnlag.getRegisterVersjon().getAdresser()
                .stream()
                .filter(adr -> erIkkeSøker(aktørId, adr.getAktørId()) ||
                    erGyldigPåTidspunkt(forPeriode, adr.getPeriode()))
                .collect(Collectors.toList());
            overstyrtPersonstatus = grunnlag.getOverstyrtVersjon().map(PersonInformasjon::getPersonstatus)
                .orElse(Collections.emptyList());
            final List<Personstatus> registerPersonstatus = grunnlag.getRegisterVersjon().getPersonstatus()
                .stream()
                .filter(it -> finnesIkkeIOverstyrt(it, overstyrtPersonstatus))
                .collect(Collectors.toList());
            this.orginalPersonstatus = grunnlag.getRegisterVersjon().getPersonstatus()
                .stream()
                .filter(it -> !finnesIkkeIOverstyrt(it, overstyrtPersonstatus))
                .collect(Collectors.toList());
            this.aktuellePersonstatus = Stream.concat(
                registerPersonstatus.stream(),
                overstyrtPersonstatus.stream())
                .filter(st -> erIkkeSøker(aktørId, st.getAktørId()) ||
                    erGyldigPåTidspunkt(forPeriode, st.getPeriode()))
                .collect(Collectors.toList());
            this.aktuelleStatsborgerskap = grunnlag.getRegisterVersjon().getStatsborgerskap()
                .stream()
                .filter(adr -> erIkkeSøker(aktørId, adr.getAktørId()) ||
                    erGyldigPåTidspunkt(forPeriode, adr.getPeriode()))
                .peek(sb -> ((StatsborgerskapEntitet) sb).setRegion(landkoderRegionMap.get(sb.getStatsborgerskap())))
                .collect(Collectors.toList());
        } else {
            this.alleRelasjoner = Collections.emptyList();
            this.allePersonopplysninger = Collections.emptyList();
            this.aktuelleAdresser = Collections.emptyList();
            this.aktuellePersonstatus = Collections.emptyList();
            this.aktuelleStatsborgerskap = Collections.emptyList();
            this.orginalPersonstatus = Collections.emptyList();
            this.overstyrtPersonstatus = Collections.emptyList();
        }
    }

    private boolean finnesIkkeIOverstyrt(Personstatus status, List<Personstatus> overstyrt) {
        return overstyrt.stream().noneMatch(it -> it.getAktørId().equals(status.getAktørId()) && it.getPeriode().equals(status.getPeriode()));
    }

    private boolean erGyldigPåTidspunkt(DatoIntervallEntitet forPeriode, DatoIntervallEntitet periode) {
        return periode.tilIntervall().overlaps(forPeriode.tilIntervall());
    }

    private boolean erIkkeSøker(AktørId aktørId, AktørId aktuellAktør) {
        return !aktuellAktør.equals(aktørId);
    }

    public List<Personopplysning> getPersonopplysninger() {
        return Collections.unmodifiableList(allePersonopplysninger);
    }

    public List<PersonRelasjon> getRelasjoner() {
        return Collections.unmodifiableList(alleRelasjoner);
    }

    public List<Personstatus> getPersonstatuserFor(AktørId aktørId) {
        return aktuellePersonstatus.stream()
            .filter(ss -> ss.getAktørId().equals(aktørId))
            .collect(Collectors.toList());
    }

    public Personopplysning getSøker() {
        return allePersonopplysninger.stream()
            .filter(po -> po.getAktørId().equals(søkerAktørId))
            .findFirst()
            .orElse(null);
    }

    public List<Statsborgerskap> getStatsborgerskapFor(AktørId aktørId) {
        return aktuelleStatsborgerskap.stream()
            .filter(ss -> ss.getAktørId().equals(aktørId))
            .sorted(Comparator.comparing(this::rangerRegion))
            .collect(Collectors.toList());
    }

    // Det finnes ingen definert rangering for regioner. Men venter med å generalisere til det finnes use-caser som
    // krever en annen rangering enn nedenfor.
    private Integer rangerRegion(Statsborgerskap region) {
        if (region.getRegion().equals(Region.NORDEN)) {
            return 1;
        }
        if (region.getRegion().equals(Region.EOS)) {
            return 2;
        }
        return 3;
    }

    public Personstatus getPersonstatusFor(AktørId aktørId) {
        return aktuellePersonstatus.stream()
            .filter(ss -> ss.getAktørId().equals(aktørId))
            .sorted(Comparator.comparing(Personstatus::getPeriode).reversed())
            .findFirst().orElse(null);
    }

    /**
     * SKal kun benyttes av GUI til å vise verdien i aksjonspunktet {@value AksjonspunktDefinisjon#AVKLAR_FAKTA_FOR_PERSONSTATUS}
     *
     * @param søkerAktørId
     * @param aktørId
     * @return
     */
    public Personstatus getOverstyrtPersonstatusFor(AktørId søkerAktørId) {
        return aktuellePersonstatus.stream()
            .filter(ss -> ss.getAktørId().equals(søkerAktørId))
            .sorted(Comparator.comparing(Personstatus::getPeriode).reversed())
            .findFirst().orElse(null);
    }

    /**
     * Returnerer opprinnelig personstatus der hvor personstatus har blitt overstyrt
     *
     * @param søkerAktørId
     * @return personstatus
     */
    public Optional<Personstatus> getOrginalPersonstatusFor(AktørId søkerAktørId) {
        return orginalPersonstatus.stream()
            .filter(ss -> ss.getAktørId().equals(søkerAktørId))
            .sorted(Comparator.comparing(Personstatus::getPeriode).reversed())
            .findFirst();
    }

    public String getNavn() {
        return getSøker().getNavn();
    }

    public List<PersonAdresse> getAdresserFor(AktørId aktørId) {
        return aktuelleAdresser.stream()
            .filter(ss -> ss.getAktørId().equals(aktørId))
            .sorted(Comparator.comparing(PersonAdresse::getPeriode).reversed())
            .collect(Collectors.toList());
    }

    public Optional<Personopplysning> getEktefelle() {
        List<Personopplysning> personer = getTilPersonerFor(søkerAktørId, RelasjonsRolleType.EKTE);
        return personer.isEmpty() ? Optional.empty() : Optional.of(personer.get(0));
    }

    public List<PersonRelasjon> getSøkersRelasjoner() {
        return finnRelasjon(søkerAktørId);
    }

    public Map<AktørId, Personopplysning> getAktørPersonopplysningMap() {
        return getPersonopplysninger().stream().collect(Collectors.toMap(Personopplysning::getAktørId, Function.identity()));
    }

    public List<Personopplysning> getTilPersonerFor(AktørId fraAktørId, RelasjonsRolleType relasjonsRolleType) {
        List<AktørId> tilAktører = alleRelasjoner.stream()
            .filter(e -> e.getRelasjonsrolle().equals(relasjonsRolleType) && e.getAktørId().equals(fraAktørId))
            .map(PersonRelasjon::getTilAktørId)
            .collect(Collectors.toList());

        List<Personopplysning> tilPersoner = new ArrayList<>();
        tilAktører.forEach(e -> {
            allePersonopplysninger.stream()
                .filter(po -> po.getAktørId().equals(e))
                .forEach(p -> tilPersoner.add(p));
        });
        return Collections.unmodifiableList(tilPersoner);
    }

    private List<PersonRelasjon> finnRelasjon(AktørId fraAktørId) {
        return getRelasjoner().stream()
            .filter(e -> e.getAktørId().equals(fraAktørId))
            .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PersonopplysningerAggregat that = (PersonopplysningerAggregat) o;
        return Objects.equals(søkerAktørId, that.søkerAktørId) &&
            Objects.equals(allePersonopplysninger, that.allePersonopplysninger) &&
            Objects.equals(alleRelasjoner, that.alleRelasjoner) &&
            Objects.equals(aktuelleAdresser, that.aktuelleAdresser) &&
            Objects.equals(aktuellePersonstatus, that.aktuellePersonstatus) &&
            Objects.equals(aktuelleStatsborgerskap, that.aktuelleStatsborgerskap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(søkerAktørId, allePersonopplysninger, alleRelasjoner, aktuelleAdresser, aktuellePersonstatus, aktuelleStatsborgerskap);
    }

    @Override
    public String toString() {
        return "PersonopplysningerAggregat{" +
            "søkerAktørId=" + søkerAktørId +
            ", allePersonopplysninger=" + allePersonopplysninger +
            ", forPeriode=" + forPeriode +
            '}';
    }
}
