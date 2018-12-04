package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.IKKE_BRUK;
import static no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "Inntektsmeldinger")
@Table(name = "IAY_INNTEKTSMELDINGER")
public class InntektsmeldingAggregatEntitet extends BaseEntitet implements InntektsmeldingAggregat {

    private static final Logger logger = LoggerFactory.getLogger(InntektsmeldingAggregatEntitet.class);

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDINGER")
    private Long id;

    @OneToMany(mappedBy = "inntektsmeldinger")
    @ChangeTracked
    private List<InntektsmeldingEntitet> inntektsmeldinger = new ArrayList<>();

    @Transient
    private ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjon;

    InntektsmeldingAggregatEntitet() {
    }

    InntektsmeldingAggregatEntitet(InntektsmeldingAggregat inntektsmeldingAggregat) {
        final InntektsmeldingAggregatEntitet inntektsmeldingAggregat1 = (InntektsmeldingAggregatEntitet) inntektsmeldingAggregat; // NOSONAR
        this.inntektsmeldinger = inntektsmeldingAggregat1.inntektsmeldinger.stream().map(i -> {
            final InntektsmeldingEntitet inntektsmeldingEntitet = new InntektsmeldingEntitet(i);
            inntektsmeldingEntitet.setInntektsmeldinger(this);
            return inntektsmeldingEntitet;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Inntektsmelding> getInntektsmeldinger() {
        return Collections.unmodifiableList(inntektsmeldinger.stream().filter(this::skalBrukes).collect(Collectors.toList()));
    }

    public List<Inntektsmelding> getAlleInntektsmeldinger() {
        return Collections.unmodifiableList(inntektsmeldinger);
    }

    private boolean skalBrukes(InntektsmeldingEntitet im) {
        return arbeidsforholdInformasjon == null || arbeidsforholdInformasjon.getOverstyringer()
            .stream()
            .noneMatch(ov -> erFjernet(im, ov));
    }

    private boolean erFjernet(InntektsmeldingEntitet im, ArbeidsforholdOverstyringEntitet ov) {
        return (ov.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
            && ov.getArbeidsgiver().equals(Arbeidsgiver.virksomhet(im.getVirksomhet()))
            && (Objects.equals(IKKE_BRUK, ov.getHandling())
            || Objects.equals(BRUK_UTEN_INNTEKTSMELDING, ov.getHandling())
            || Objects.equals(SLÅTT_SAMMEN_MED_ANNET, ov.getHandling()));
    }

    @Override
    public List<Inntektsmelding> getInntektsmeldingerFor(Virksomhet virksomhet) {
        return getInntektsmeldinger().stream().filter(i -> i.getVirksomhet().equals(virksomhet)).collect(Collectors.toList());
    }

    /**
     * Den persisterte inntektsmeldingen kan være av nyere dato, bestemmes av
     * innsendingstidspunkt på inntektsmeldingen.
     */
    void leggTil(Inntektsmelding inntektsmelding) {

        boolean fjernet = inntektsmeldinger.removeIf(it -> it.gjelderSammeArbeidsforhold(inntektsmelding)
            && it.getInnsendingstidspunkt().isBefore(inntektsmelding.getInnsendingstidspunkt())
        );

        if (fjernet || inntektsmeldinger.stream().noneMatch(it -> it.gjelderSammeArbeidsforhold(inntektsmelding))) {
            final InntektsmeldingEntitet entitet = (InntektsmeldingEntitet) inntektsmelding;
            entitet.setInntektsmeldinger(this);
            inntektsmeldinger.add(entitet);
        }

        inntektsmeldinger.stream().filter(it -> it.gjelderSammeArbeidsforhold(inntektsmelding) && !fjernet).findFirst().ifPresent(e -> {
            logger.info("Persistert inntektsmelding med journalpostid {} er nyere enn den mottatte med journalpostid {}. Ignoreres", e.getJournalpostId(), inntektsmelding.getJournalpostId());
        });
        
        inntektsmeldinger.stream().filter(it -> it.getJournalpostId().equals(inntektsmelding.getJournalpostId())).findFirst().ifPresent(
            e -> {
                logger.info("Persistert inntektsmelding med samme journalpostid {}", e.getJournalpostId());
            });
    }

    void taHensynTilBetraktninger(ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjon) {
        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InntektsmeldingAggregatEntitet that = (InntektsmeldingAggregatEntitet) o;
        return Objects.equals(inntektsmeldinger, that.inntektsmeldinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektsmeldinger);
    }
}
