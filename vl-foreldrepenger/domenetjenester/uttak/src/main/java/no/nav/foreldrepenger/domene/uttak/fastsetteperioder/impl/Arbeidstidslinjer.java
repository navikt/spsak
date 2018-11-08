package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;

class Arbeidstidslinjer {
    private final Map<AktivitetIdentifikator, ArbeidTidslinje> arbeidsprosenter;

    Arbeidstidslinjer(Map<AktivitetIdentifikator, ArbeidTidslinje> arbeidsprosenter) {
        this.arbeidsprosenter = arbeidsprosenter;
    }

    public List<AktivitetIdentifikator> graderteAktiviteter(LocalDate fom, LocalDate tom, String orgnr) {
        List<AktivitetIdentifikator> aktivitetIdentifikatorer = new ArrayList<>();
        for (Map.Entry<AktivitetIdentifikator, ArbeidTidslinje> entry : arbeidsprosenter.entrySet()) {
            if(orgnr.equals(entry.getKey().getOrgNr())) {
                if(gradertIPeriode(fom, tom, entry)) {
                    aktivitetIdentifikatorer.add(entry.getKey());
                }
            }
        }
        return aktivitetIdentifikatorer;
    }

    public Map<AktivitetIdentifikator, ArbeidTidslinje> getArbeidsprosenter() {
        return arbeidsprosenter;
    }

    public AktivitetIdentifikator gradertFrilansSelvstendigNæringsdrivendeAktivitet(LocalDate fom, LocalDate tom) {
        return arbeidsprosenter.entrySet()
            .stream()
            .filter(entry -> frilansEllerNæringsdrivende(entry.getKey().getAktivitetType()))
            .filter(entry -> gradertIPeriode(fom, tom, entry)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Arbeidstidslinje har ikke gradert frilans/selvstendig næringsdrivende i perioden " + fom + " - " + tom))
            .getKey();
    }

    private boolean gradertIPeriode(LocalDate fom, LocalDate tom, Map.Entry<AktivitetIdentifikator, ArbeidTidslinje> entry) {
        Optional<Arbeid> arbeid = entry.getValue().getArbeid(fom, tom);
        return arbeid.isPresent() && arbeid.get().isGradert();
    }

    private boolean frilansEllerNæringsdrivende(AktivitetType aktivitetType) {
        return Objects.equals(AktivitetType.FRILANS, aktivitetType) || Objects.equals(AktivitetType.SELVSTENDIG_NÆRINGSDRIVENDE, aktivitetType);
    }
}
