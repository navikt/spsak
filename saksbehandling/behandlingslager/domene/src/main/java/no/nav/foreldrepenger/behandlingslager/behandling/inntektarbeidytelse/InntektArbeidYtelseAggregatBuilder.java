package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Builder for å håndtere en gitt versjon {@link VersjonType} av grunnlaget.
 * <p>
 * Holder styr på om det er en oppdatering av eksisterende informasjon, om det gjelder før eller etter skjæringstidspunktet
 * og om det er registerdata eller saksbehandlers beslutninger.
 * <p>
 * NB! Viktig at denne builderen hentes fra repository for å sikre at den er rett tilstand ved oppdatering. Hvis ikke kan data gå tapt.
 */
public class InntektArbeidYtelseAggregatBuilder {

    private final InntektArbeidYtelseAggregatEntitet kladd;
    private final VersjonType versjon;
    private boolean oppdaterer;

    private InntektArbeidYtelseAggregatBuilder(InntektArbeidYtelseAggregatEntitet kladd, boolean oppdaterer, VersjonType versjon) {
        this.kladd = kladd;
        this.oppdaterer = oppdaterer;
        this.versjon = versjon;
    }

    private static InntektArbeidYtelseAggregatBuilder ny(VersjonType versjon) {
        return new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatEntitet(), false, versjon);
    }

    private static InntektArbeidYtelseAggregatBuilder oppdatere(InntektArbeidYtelseAggregat oppdatere, VersjonType versjon) {
        return new InntektArbeidYtelseAggregatBuilder(new InntektArbeidYtelseAggregatEntitet(oppdatere), true, versjon);
    }

    public static InntektArbeidYtelseAggregatBuilder oppdatere(Optional<InntektArbeidYtelseAggregat> oppdatere, VersjonType versjon) {
        return oppdatere.map(aggregat -> oppdatere(aggregat, versjon)).orElseGet(() -> ny(versjon));
    }

    /**
     * Legger til inntekter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørInntekt {@link AktørInntektBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørInntekt(AktørInntektBuilder aktørInntekt) {
        if (!aktørInntekt.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørInntekt(aktørInntekt.build());
        }
        return this;
    }

    /**
     * Legger til aktiviteter for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørArbeid {@link AktørArbeidBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørArbeid(AktørArbeidBuilder aktørArbeid) {
        if (!aktørArbeid.getErOppdatering()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørArbeid(aktørArbeid.build());
        }
        return this;
    }

    /**
     * Legger til tilstøtende ytelser for en gitt aktør hvis det ikke er en oppdatering av eksisterende.
     * Ved oppdatering eksisterer koblingen for denne aktøren allerede så en kopi av forrige innslag manipuleres før lagring.
     *
     * @param aktørYtelse {@link AktørYtelseBuilder}
     * @return this
     */
    public InntektArbeidYtelseAggregatBuilder leggTilAktørYtelse(AktørYtelseBuilder aktørYtelse) {
        if (!aktørYtelse.getErOppdatering() && aktørYtelse.harVerdi()) {
            // Hvis ny så skal den legges til, hvis ikke ligger den allerede der og blir manipulert.
            this.kladd.leggTilAktørYtelse(aktørYtelse.build());
        }
        return this;
    }

    /**
     * Oppretter builder for aktiviteter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørArbeidBuilder}
     */
    public AktørArbeidBuilder getAktørArbeidBuilder(AktørId aktørId) {
        Optional<AktørArbeid> aktørArbeid = kladd.getAktørArbeid().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        return AktørArbeidBuilder.oppdatere(aktørArbeid).medAktørId(aktørId);
    }

    /**
     * Oppretter builder for inntekter for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørInntektBuilder}
     */
    public AktørInntektBuilder getAktørInntektBuilder(AktørId aktørId) {
        Optional<AktørInntekt> aktørInntekt = kladd.getAktørInntekt().stream().filter(aa -> aktørId.equals(aa.getAktørId())).findFirst();
        final AktørInntektBuilder oppdatere = AktørInntektBuilder.oppdatere(aktørInntekt);
        oppdatere.medAktørId(aktørId);
        return oppdatere;
    }

    /**
     * Oppretter builder for tilstøtende ytelser for en gitt aktør. Baserer seg på en kopi av forrige innslag for aktøren hvis det eksisterer.
     *
     * @param aktørId aktøren
     * @return builder {@link AktørYtelseBuilder}
     */
    public AktørYtelseBuilder getAktørYtelseBuilder(AktørId aktørId) {
        Optional<AktørYtelse> aktørYtelse = kladd.getAktørYtelse().stream().filter(ay -> aktørId.equals(ay.getAktørId())).findFirst();
        return AktørYtelseBuilder.oppdatere(aktørYtelse).medAktørId(aktørId);
    }

    public AktørYtelseBuilder getAktørYtelseBuilderForKilde(AktørId aktørId, Fagsystem kilde) { // NOSONAR
        Optional<AktørYtelse> aktørYtelse = kladd.getAktørYtelse().stream().filter(ay -> aktørId.equals(ay.getAktørId())).findFirst();
        return AktørYtelseBuilder.oppdatere(aktørYtelse).medAktørId(aktørId).medKilde(kilde);
    }

    public InntektArbeidYtelseAggregat build() {
        return this.kladd;
    }

    public boolean isOppdaterer() {
        return oppdaterer;
    }

    VersjonType getVersjon() {
        return versjon;
    }

    void oppdaterArbeidsforholdReferanseEtterErstatting(AktørId søker, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef gammelRef, ArbeidsforholdRef nyRef) {
        final AktørArbeidBuilder builder = getAktørArbeidBuilder(søker);
        if (builder.getErOppdatering()) {
            if (eksistererIkkeFraFør(arbeidsgiver, gammelRef, builder)) {
                final YrkesaktivitetBuilder yrkesaktivitetBuilder = builder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
                    ArbeidType.AA_REGISTER_TYPER);
                if (yrkesaktivitetBuilder.getErOppdatering()) {
                    yrkesaktivitetBuilder.medArbeidsforholdId(nyRef);
                    builder.leggTilYrkesaktivitet(yrkesaktivitetBuilder);
                    leggTilAktørArbeid(builder);
                }
            }
        }
    }

    private boolean eksistererIkkeFraFør(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef gammelRef, AktørArbeidBuilder builder) {
        return !builder.getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel.forArbeidsforholdIdMedArbeidgiver(gammelRef, arbeidsgiver),
            ArbeidType.AA_REGISTER_TYPER).getErOppdatering();
    }

    public static class AktørArbeidBuilder {
        private final AktørArbeidEntitet kladd;
        private final boolean oppdatering;

        private AktørArbeidBuilder(AktørArbeidEntitet aktørArbeid, boolean oppdatering) {
            this.kladd = aktørArbeid;
            this.oppdatering = oppdatering;
        }

        static AktørArbeidBuilder ny() {
            return new AktørArbeidBuilder(new AktørArbeidEntitet(), false);
        }

        static AktørArbeidBuilder oppdatere(AktørArbeid oppdatere) {
            return new AktørArbeidBuilder((AktørArbeidEntitet) oppdatere, true);
        }

        public static AktørArbeidBuilder oppdatere(Optional<AktørArbeid> oppdatere) {
            return oppdatere.map(AktørArbeidBuilder::oppdatere).orElseGet(AktørArbeidBuilder::ny);
        }

        public AktørArbeidBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel nøkkel, ArbeidType arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForNøkkelAvType(Opptjeningsnøkkel nøkkel, Set<ArbeidType> arbeidType) {
            return kladd.getYrkesaktivitetBuilderForNøkkel(nøkkel, arbeidType);
        }

        public YrkesaktivitetBuilder getYrkesaktivitetBuilderForType(ArbeidType type) {
            return kladd.getYrkesaktivitetBuilderForType(type);
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(YrkesaktivitetBuilder yrkesaktivitet) {
            YrkesaktivitetEntitet yrkesaktivitetEntitet = (YrkesaktivitetEntitet) yrkesaktivitet.build();
            if (!yrkesaktivitet.getErOppdatering()) {
                kladd.leggTilYrkesaktivitet(yrkesaktivitetEntitet);
            }
            return this;
        }

        public AktørArbeidBuilder leggTilYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
            kladd.leggTilYrkesaktivitet(yrkesaktivitet);
            return this;
        }

        public AktørArbeid build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        public boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørArbeid getKladd() {
            return kladd;
        }

        public void fjernYrkesaktivitetHvisFinnes(YrkesaktivitetBuilder builder) {
            kladd.fjernYrkesaktivitetForBuilder(builder);
        }
    }

    public static class AktørInntektBuilder {
        private final AktørInntektEntitet aktørInntektEntitet;
        private final boolean oppdatering;

        private AktørInntektBuilder(AktørInntektEntitet aktørInntektEntitet, boolean oppdatering) {
            this.aktørInntektEntitet = aktørInntektEntitet;
            this.oppdatering = oppdatering;
        }

        static AktørInntektBuilder ny() {
            return new AktørInntektBuilder(new AktørInntektEntitet(), false);
        }

        static AktørInntektBuilder oppdatere(AktørInntekt oppdatere) {
            return new AktørInntektBuilder((AktørInntektEntitet) oppdatere, true);
        }

        public static AktørInntektBuilder oppdatere(Optional<AktørInntekt> oppdatere) {
            return oppdatere.map(AktørInntektBuilder::oppdatere).orElseGet(AktørInntektBuilder::ny);
        }


        private void medAktørId(AktørId aktørId) {
            this.aktørInntektEntitet.setAktørId(aktørId);
        }

        public AktørInntektEntitet.InntektBuilder getInntektBuilder(InntektsKilde inntektsKilde, Opptjeningsnøkkel opptjeningsnøkkel) {
            return aktørInntektEntitet.getInntektBuilder(inntektsKilde, opptjeningsnøkkel);
        }

        public AktørInntektEntitet.InntektBuilder getInntektBuilderForYtelser(InntektsKilde inntektsKilde) {
            return aktørInntektEntitet.getInntektBuilderForYtelser(inntektsKilde);
        }

        public AktørInntektBuilder leggTilInntekt(AktørInntektEntitet.InntektBuilder inntektBuilder) {
            if (!inntektBuilder.getErOppdatering()) {
                InntektEntitet inntektTmpEntitet = (InntektEntitet) inntektBuilder.build();
                aktørInntektEntitet.leggTilInntekt(inntektTmpEntitet);
            }
            return this;
        }

        public AktørInntekt build() {
            if (aktørInntektEntitet.hasValues()) {
                return aktørInntektEntitet;
            }
            throw new IllegalStateException();
        }

        AktørInntekt getKladd() {
            return aktørInntektEntitet;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørInntektBuilder fjernInntekterFraKilde(InntektsKilde inntektsKilde) {
            aktørInntektEntitet.fjernInntekterFraKilde(inntektsKilde);
            return this;
        }
    }

    public static class AktørYtelseBuilder {
        private final AktørYtelseEntitet kladd;
        private final boolean oppdatering;
        private Set<YtelseEntitet> ytelser;
        private Fagsystem kilde;

        private AktørYtelseBuilder(AktørYtelseEntitet aktørYtelseEntitet, boolean oppdatering) {
            this.kladd = aktørYtelseEntitet;
            this.oppdatering = oppdatering;
            this.ytelser = new LinkedHashSet<>();
            this.kilde = Fagsystem.UDEFINERT;
        }

        static AktørYtelseBuilder ny() {
            return new AktørYtelseBuilder(new AktørYtelseEntitet(), false);
        }

        static AktørYtelseBuilder oppdatere(AktørYtelse oppdatere) {
            return new AktørYtelseBuilder((AktørYtelseEntitet) oppdatere, true);
        }

        public static AktørYtelseBuilder oppdatere(Optional<AktørYtelse> oppdatere) {
            return oppdatere.map(AktørYtelseBuilder::oppdatere).orElseGet(AktørYtelseBuilder::ny);
        }

        boolean getErOppdatering() {
            return oppdatering;
        }

        public AktørYtelseBuilder medAktørId(AktørId aktørId) {
            this.kladd.setAktørId(aktørId);
            return this;
        }

        public AktørYtelseBuilder medKilde(Fagsystem fagsystem) {
            this.kilde = fagsystem;
            return this;
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, Saksnummer sakId) {
            return kladd.getYtelseBuilderForType(fagsystem, type, sakId);
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, Saksnummer sakId, LocalDate fom) {
            return kladd.getYtelseBuilderForType(fagsystem, type, sakId, fom);
        }

        public YtelseBuilder getYtelselseBuilderForType(Fagsystem fagsystem, RelatertYtelseType type, TemaUnderkategori typeKategori, DatoIntervallEntitet periode) {
            return kladd.getYtelseBuilderForType(fagsystem, type, typeKategori, periode);
        }

        public AktørYtelseBuilder leggTilYtelse(YtelseBuilder ytelse) {
            YtelseEntitet ytelseEntitet = (YtelseEntitet) ytelse.build();
            ytelser.add(ytelseEntitet);
            if (!ytelse.getErOppdatering()) {
                this.kladd.leggTilYtelse(ytelseEntitet);
            }
            return this;
        }

        boolean harVerdi() {
            return kladd.hasValues();
        }

        public AktørYtelse build() {
            if (this.kladd.hasValues()) {
                this.kladd.getYtelser().forEach(ytelse -> {
                    if ((Fagsystem.UDEFINERT.equals(this.kilde) || ytelse.getKilde().equals(this.kilde)) && !ytelser.contains(ytelse)) {
                        this.kladd.fjernYtelse(ytelse);
                    }
                });
                return kladd;
            }
            throw new IllegalStateException("Har ikke innhold");
        }
    }

}


