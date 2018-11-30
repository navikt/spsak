package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.vedtak.util.Tuple;

public class ArbeidsforholdInformasjonBuilder {

    private final ArbeidsforholdInformasjonEntitet kladd;
    private final List<Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>>> erstattArbeidsforhold = new ArrayList<>();
    private final List<Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>>> reverserteErstattninger = new ArrayList<>();

    private ArbeidsforholdInformasjonBuilder(ArbeidsforholdInformasjonEntitet kladd) {
        this.kladd = kladd;
    }

    public static ArbeidsforholdInformasjonBuilder oppdatere(ArbeidsforholdInformasjon oppdatere) {
        return new ArbeidsforholdInformasjonBuilder(new ArbeidsforholdInformasjonEntitet(oppdatere));
    }

    public ArbeidsforholdOverstyringBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref) {
        return kladd.getOverstyringBuilderFor(arbeidsgiver, ref);
    }

    public ArbeidsforholdInformasjonBuilder tilbakestillOverstyringer() {
        final List<ArbeidsforholdReferanseEntitet> collect = kladd.getReferanser().stream().filter(it -> kladd.getOverstyringer().stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getNyArbeidsforholdRef().gjelderFor(it.getInternReferanse()))).collect(Collectors.toList());
        collect.forEach(it -> {
            final ArbeidsforholdRef arbeidsforholdRef = kladd.finnForEksternBeholdHistoriskReferanse(it.getArbeidsgiver(), it.getEksternReferanse());
            if (!Objects.equals(arbeidsforholdRef, it.getInternReferanse())) {
                reverserteErstattninger.add(new Tuple<>(it.getArbeidsgiver(), new Tuple<>(it.getInternReferanse(),
                    arbeidsforholdRef)));
            }
        });
        kladd.tilbakestillOverstyringer();
        return this;
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over Arbeidsgiver / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>>> getErstattArbeidsforhold() {
        return Collections.unmodifiableList(erstattArbeidsforhold);
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over Arbeidsgiver / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<ArbeidsforholdRef, ArbeidsforholdRef>>> getReverserteErstattArbeidsforhold() {
        return Collections.unmodifiableList(reverserteErstattninger);
    }

    public ArbeidsforholdInformasjonBuilder erstattArbedsforhold(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef gammelRef, ArbeidsforholdRef ref) {
        // TODO: Sjekke om revertert allerede
        // Hvis eksisterer så reverter revertering og ikke legg inn erstattning og kall på erstatt
        erstattArbeidsforhold.add(new Tuple<>(arbeidsgiver, new Tuple<>(gammelRef, ref)));
        kladd.erstattArbeidsforhold(arbeidsgiver, gammelRef, ref);
        return this;
    }

    public ArbeidsforholdInformasjonBuilder leggTil(ArbeidsforholdOverstyringBuilder overstyringBuilder) {
        if (!overstyringBuilder.isOppdatering()) {
            kladd.leggTilOverstyring(overstyringBuilder.build());
        }
        return this;
    }

    public ArbeidsforholdInformasjonEntitet build() {
        return kladd;
    }

    public ArbeidsforholdInformasjonBuilder fjernOverstyringVedrørende(Virksomhet virksomhet,
                                                                       ArbeidsforholdRef arbeidsforholdRef) {
        kladd.fjernOverstyringVedrørende(virksomhet, arbeidsforholdRef);
        return this;
    }
}
