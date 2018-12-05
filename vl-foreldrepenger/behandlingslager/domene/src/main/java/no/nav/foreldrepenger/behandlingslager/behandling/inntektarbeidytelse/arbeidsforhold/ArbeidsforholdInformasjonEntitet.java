package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Table(name = "IAY_INFORMASJON")
@Entity(name = "ArbeidsforholdInformasjon")
public class ArbeidsforholdInformasjonEntitet extends BaseEntitet implements ArbeidsforholdInformasjon {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_INFORMASJON")
    private Long id;

    @ChangeTracked
    @OneToMany(mappedBy = "informasjon")
    private Set<ArbeidsforholdReferanseEntitet> referanser = new LinkedHashSet<>();

    @ChangeTracked
    @OneToMany(mappedBy = "informasjon")
    private List<ArbeidsforholdOverstyringEntitet> overstyringer = new ArrayList<>();

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    public ArbeidsforholdInformasjonEntitet() {
    }

    public ArbeidsforholdInformasjonEntitet(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet = (ArbeidsforholdInformasjonEntitet) arbeidsforholdInformasjon; // NOSONAR
        for (ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet : arbeidsforholdInformasjonEntitet.referanser) {
            final ArbeidsforholdReferanseEntitet referanseEntitet = new ArbeidsforholdReferanseEntitet(arbeidsforholdReferanseEntitet);
            referanseEntitet.setInformasjon(this);
            this.referanser.add(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringEntitet arbeidsforholdOverstyringEntitet : arbeidsforholdInformasjonEntitet.overstyringer) {
            final ArbeidsforholdOverstyringEntitet overstyringEntitet = new ArbeidsforholdOverstyringEntitet(arbeidsforholdOverstyringEntitet);
            overstyringEntitet.setInformasjon(this);
            this.overstyringer.add(overstyringEntitet);
        }
    }

    public Collection<ArbeidsforholdReferanseEntitet> getReferanser() {
        return Collections.unmodifiableSet(this.referanser);
    }

    @Override
    public List<ArbeidsforholdOverstyringEntitet> getOverstyringer() {
        return Collections.unmodifiableList(this.overstyringer);
    }

    @Override
    public ArbeidsforholdRef finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef) {
        // For å sike at det ikke mistes data ved sammenslåing av og innhenting av registerdata
        final Optional<ArbeidsforholdReferanseEntitet> referanseEntitet = referanser.stream().filter(re -> overstyringer.stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && re.getEksternReferanse().equals(arbeidsforholdRef)
                && re.getInternReferanse().equals(ov.getArbeidsforholdRef())))
            .findAny();
        if (referanseEntitet.isPresent()) {
            return referanseEntitet.get().getInternReferanse();
        }
        return finnForEkstern(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public ArbeidsforholdRef finnForEkstern(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        final List<ArbeidsforholdReferanseEntitet> arbeidsforholdReferanseEntitetStream = this.referanser.stream()
            .filter(this::erIkkeMerget)
            .collect(Collectors.toList());
        return arbeidsforholdReferanseEntitetStream.stream()
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getEksternReferanse().equals(ref))
            .findAny().map(ArbeidsforholdReferanseEntitet::getInternReferanse).orElse(ref);
    }

    private boolean erIkkeMerget(ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet) {
        return overstyringer.stream().noneMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().equals(arbeidsforholdReferanseEntitet.getArbeidsgiver())
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdReferanseEntitet.getInternReferanse()));
    }

    @Override
    public ArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, final ArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdOverstyringEntitet> erstattning = overstyringer.stream()
            .filter(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getArbeidsgiver().equals(arbeidsgiver)
                && (ov.getArbeidsforholdRef().gjelderFor(ref)
                || ov.getArbeidsforholdRef().gjelderFor(finnForEksternBeholdHistoriskReferanse(arbeidsgiver, ref))))
            .findAny();
        if (erstattning.isPresent() && !erstattning.get().getNyArbeidsforholdRef().equals(ref)) {
            return finnEllerOpprett(arbeidsgiver, erstattning.get().getNyArbeidsforholdRef());
        } else {
            final ArbeidsforholdReferanseEntitet referanse = this.referanser.stream()
                .filter(this::erIkkeMerget)
                .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getInternReferanse().equals(ref))
                .findAny().orElseGet(() -> finnEksisterendeInternReferanseEllerOpprettNy(arbeidsgiver, ref));

            return referanse.getInternReferanse();
        }
    }

    private Optional<ArbeidsforholdReferanseEntitet> referanseEksistererIkke(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getInternReferanse().equals(ref))
            .findAny();
    }

    private ArbeidsforholdReferanseEntitet finnEksisterendeInternReferanseEllerOpprettNy(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        return finnEksisterendeReferanse(arbeidsgiver, ref)
            .orElseGet(() -> opprettNyReferanse(arbeidsgiver, ArbeidsforholdRef.ref(UUID.randomUUID().toString()), ref));
    }

    private Optional<ArbeidsforholdReferanseEntitet> finnEksisterendeReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        return this.referanser.stream()
            .filter(this::erIkkeMerget)
            .filter(it -> it.getArbeidsgiver().equals(arbeidsgiver) && it.getEksternReferanse().equals(ref))
            .findAny();
    }

    private ArbeidsforholdReferanseEntitet opprettNyReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef internReferanse, ArbeidsforholdRef eksternReferanse) {
        final ArbeidsforholdReferanseEntitet arbeidsforholdReferanseEntitet = new ArbeidsforholdReferanseEntitet(arbeidsgiver,
            internReferanse, eksternReferanse);
        arbeidsforholdReferanseEntitet.setInformasjon(this);
        referanser.add(arbeidsforholdReferanseEntitet);
        return arbeidsforholdReferanseEntitet;
    }

    ArbeidsforholdOverstyringBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        return ArbeidsforholdOverstyringBuilder.oppdatere(this.overstyringer
            .stream()
            .filter(ov -> ov.getArbeidsgiver().equals(arbeidsgiver)
                && ov.getArbeidsforholdRef().gjelderFor(ref))
            .findFirst())
            .medInformasjon(this)
            .medArbeidsforholdRef(ref)
            .medArbeidsgiver(arbeidsgiver);
    }

    void leggTilOverstyring(ArbeidsforholdOverstyringEntitet build) {
        build.setInformasjon(this);
        this.overstyringer.add(build);
    }

    void tilbakestillOverstyringer() {
        this.overstyringer.clear();
    }

    void erstattArbeidsforhold(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef gammelRef, ArbeidsforholdRef ref) {
        final Optional<ArbeidsforholdReferanseEntitet> referanseEntitet = referanseEksistererIkke(arbeidsgiver, gammelRef);
        referanseEntitet.ifPresent(it -> opprettNyReferanse(arbeidsgiver, ref, it.getEksternReferanse()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdInformasjonEntitet that = (ArbeidsforholdInformasjonEntitet) o;
        return Objects.equals(referanser, that.referanser) &&
            Objects.equals(overstyringer, that.overstyringer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanser, overstyringer);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
            "referanser=" + referanser +
            ", overstyringer=" + overstyringer +
            '}';
    }

    void fjernOverstyringVedrørende(Virksomhet virksomhet, ArbeidsforholdRef arbeidsforholdRef) {
        overstyringer.removeIf(ov -> !Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
            && ov.getArbeidsgiver().getErVirksomhet()
            && ov.getArbeidsgiver().getVirksomhet().equals(virksomhet)
            && ov.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef));
    }
}
