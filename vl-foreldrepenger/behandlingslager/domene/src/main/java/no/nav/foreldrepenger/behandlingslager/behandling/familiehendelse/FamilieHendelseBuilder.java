package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;

public class FamilieHendelseBuilder {
    private final FamilieHendelseEntitet hendelse;
    private final HendelseVersjonType type;
    private final boolean oppdaterer;
    private HendelseVersjonType overgrippendeType;
    private AdopsjonBuilder adopsjonBuilder;
    private TerminbekreftelseBuilder terminbekreftelseBuilder;

    private FamilieHendelseBuilder(FamilieHendelseEntitet hendelse, HendelseVersjonType type, boolean oppdaterer) {
        this.hendelse = hendelse;
        this.type = type;
        this.oppdaterer = oppdaterer;
    }

    static FamilieHendelseBuilder ny(HendelseVersjonType type) {
        return new FamilieHendelseBuilder(new FamilieHendelseEntitet(FamilieHendelseType.UDEFINERT), type, false);
    }

    static FamilieHendelseBuilder oppdatere(FamilieHendelse oppdatere, HendelseVersjonType type) {
        return new FamilieHendelseBuilder(new FamilieHendelseEntitet(oppdatere), type, true);
    }

    /**
     * Kommer til å endre scope til package private
     *
     * @param oppdatere entiteten som skal oppdateres
     * @param type      HendelseVersjonType
     * @return buildern
     */
    public static FamilieHendelseBuilder oppdatere(Optional<FamilieHendelse> oppdatere, HendelseVersjonType type) {
        return oppdatere.map(oppdatere1 -> oppdatere(oppdatere1, type)).orElseGet(() -> ny(type));
    }

    public FamilieHendelseBuilder leggTilBarn(LocalDate fødselsDato) {
        hendelse.leggTilBarn(new UidentifisertBarnEntitet(fødselsDato, hendelse.getBarna().size() + 1));
        return this;
    }

    public FamilieHendelseBuilder leggTilBarn(LocalDate fødselsDato, LocalDate dødsdato) {
        hendelse.leggTilBarn(new UidentifisertBarnEntitet(hendelse.getBarna().size() + 1, fødselsDato, dødsdato));
        return this;
    }

    public FamilieHendelseBuilder leggTilBarn(UidentifisertBarn barn) {
        final UidentifisertBarnEntitet barnEntitet = new UidentifisertBarnEntitet(barn);
        hendelse.leggTilBarn(barnEntitet);
        return this;
    }

    public FamilieHendelseBuilder medFødselsDato(LocalDate fødselsDato) {
        leggTilBarn(fødselsDato);
        return this;
    }

    public FamilieHendelseBuilder tilbakestillBarn() {
        hendelse.clearBarn();
        return this;
    }

    public FamilieHendelseBuilder medAntallBarn(Integer antallBarn) {
        hendelse.setAntallBarn(antallBarn);
        return this;
    }

    public FamilieHendelseBuilder medErMorForSykVedFødsel(Boolean erMorForSykVedFødsel) {
        hendelse.setMorForSykVedFødsel(erMorForSykVedFødsel);
        return this;
    }

    public FamilieHendelseBuilder medTerminbekreftelse(TerminbekreftelseBuilder terminbekreftelse) {
        if (hendelse.getTerminbekreftelse().isPresent() == terminbekreftelse.getErOppdatering()) {
            hendelse.setTerminbekreftelse((TerminbekreftelseEntitet) terminbekreftelse.build());
            terminbekreftelseBuilder = null;
            return this;
        }
        throw FamilieHendelseFeil.FACTORY.måBasereSegPåEksisterendeVersjon().toException();
    }

    public FamilieHendelseBuilder medAdopsjon(AdopsjonBuilder adopsjon) {
        if (hendelse.getAdopsjon().isPresent() == adopsjon.getErOppdatering()) {
            hendelse.setAdopsjon((AdopsjonEntitet) adopsjon.build());
            adopsjonBuilder = null;
            return this;
        }
        throw FamilieHendelseFeil.FACTORY.måBasereSegPåEksisterendeVersjon().toException();
    }

    /**
     * Gjør det mulig å sette type til omsorgovertagelse.
     *
     * @return builder
     */
    public FamilieHendelseBuilder erOmsorgovertagelse() {
        if (hendelse.getType().equals(FamilieHendelseType.UDEFINERT)) {
            hendelse.setType(FamilieHendelseType.OMSORG);
        } else {
            throw FamilieHendelseFeil.FACTORY.kanIkkeEndreTypePåHendelseFraTil(hendelse.getType(), FamilieHendelseType.OMSORG).toException();
        }
        return this;
    }

    /**
     * Gjør det mulig å sette type til omsorgovertagelse.
     *
     * @return builder
     */
    public FamilieHendelseBuilder erFødsel() {
        if (hendelse.getType().equals(FamilieHendelseType.UDEFINERT)
            || hendelse.getType().equals(FamilieHendelseType.FØDSEL)
            || hendelse.getType().equals(FamilieHendelseType.TERMIN)) {
            hendelse.setType(FamilieHendelseType.FØDSEL);
        } else {
            throw FamilieHendelseFeil.FACTORY.kanIkkeEndreTypePåHendelseFraTil(hendelse.getType(), FamilieHendelseType.FØDSEL).toException();
        }
        return this;
    }

    public TerminbekreftelseBuilder getTerminbekreftelseBuilder() {
        if (terminbekreftelseBuilder == null) {
            terminbekreftelseBuilder = TerminbekreftelseBuilder.oppdatere(hendelse.getTerminbekreftelse());
        }
        return terminbekreftelseBuilder;
    }

    public AdopsjonBuilder getAdopsjonBuilder() {
        if (adopsjonBuilder == null) {
            adopsjonBuilder = AdopsjonBuilder.oppdatere(hendelse.getAdopsjon());
        }
        return adopsjonBuilder;
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public FamilieHendelse build() {
        if (hendelse.getTerminbekreftelse().isPresent() && hendelse.getAdopsjon().isPresent()) {
            throw new IllegalStateException("Utvikler feil: Kan ikke både ha terminbekreftelse og adopsjon");
        } else if (hendelse.getAdopsjon().isPresent()) {
            if (hendelse.getAdopsjon().get().getOmsorgovertakelseVilkår().equals(OmsorgsovertakelseVilkårType.UDEFINERT)
                && !erSøknadsversjonOgSattTilOmsorgsovertagelse()) {
                hendelse.setType(FamilieHendelseType.ADOPSJON);
            } else {
                hendelse.setType(FamilieHendelseType.OMSORG);
            }
        } else if (!hendelse.getBarna().isEmpty()
            || erHendelsenSattTil(FamilieHendelseType.FØDSEL)) {
            hendelse.setType(FamilieHendelseType.FØDSEL);
        } else if (hendelse.getTerminbekreftelse().isPresent()) {
            hendelse.setType(FamilieHendelseType.TERMIN);
        }
        if (hendelse.getAntallBarn() == null) {
            hendelse.setAntallBarn(hendelse.getBarna().size());
        }
        return hendelse;
    }

    private boolean erSøknadsversjonOgSattTilOmsorgsovertagelse() {
        return type.equals(HendelseVersjonType.SØKNAD) && hendelse.getType() != null
            && hendelse.getType().equals(FamilieHendelseType.OMSORG);
    }

    private boolean erHendelsenSattTil(FamilieHendelseType type) {
        return hendelse.getType() != null
            && hendelse.getType().equals(type);
    }

    HendelseVersjonType getType() {
        if (overgrippendeType != null && !overgrippendeType.equals(type)) {
            return overgrippendeType;
        }
        return type;
    }

    void setType(HendelseVersjonType overgrippendeType) {
        this.overgrippendeType = overgrippendeType;
    }

    HendelseVersjonType getOpprinneligType() {
        return type;
    }

    FamilieHendelse getKladd() {
        return hendelse;
    }

    public static class TerminbekreftelseBuilder {
        private final TerminbekreftelseEntitet kladd;
        private final boolean oppdatering;

        private TerminbekreftelseBuilder(TerminbekreftelseEntitet terminbekreftelse, boolean oppdatering) {
            this.kladd = terminbekreftelse;
            this.oppdatering = oppdatering;
        }

        static TerminbekreftelseBuilder ny() {
            return new TerminbekreftelseBuilder(new TerminbekreftelseEntitet(), false);
        }

        static TerminbekreftelseBuilder oppdatere(Terminbekreftelse oppdatere) {
            return new TerminbekreftelseBuilder(new TerminbekreftelseEntitet(oppdatere), true);
        }

        static TerminbekreftelseBuilder oppdatere(Optional<Terminbekreftelse> oppdatere) {
            return oppdatere.map(TerminbekreftelseBuilder::oppdatere).orElseGet(TerminbekreftelseBuilder::ny);
        }

        public TerminbekreftelseBuilder medTermindato(LocalDate termindato) {
            this.kladd.setTermindato(termindato);
            return this;
        }

        public TerminbekreftelseBuilder medUtstedtDato(LocalDate utstedtdato) {
            this.kladd.setUtstedtdato(utstedtdato);
            return this;
        }


        public TerminbekreftelseBuilder medNavnPå(String navn) {
            this.kladd.setNavn(navn);
            return this;
        }

        Terminbekreftelse build() {
            if (kladd.hasValues()) {
                return kladd;
            }
            throw new IllegalStateException();
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }

    public static class AdopsjonBuilder {
        private final AdopsjonEntitet kladd;
        private final boolean oppdatering;

        private AdopsjonBuilder(AdopsjonEntitet adopsjon, boolean oppdatering) {
            this.kladd = adopsjon;
            this.oppdatering = oppdatering;
        }

        static AdopsjonBuilder ny() {
            return new AdopsjonBuilder(new AdopsjonEntitet(), false);
        }

        static AdopsjonBuilder oppdatere(Adopsjon oppdatere) {
            return new AdopsjonBuilder(new AdopsjonEntitet(oppdatere), true);
        }

        static AdopsjonBuilder oppdatere(Optional<Adopsjon> oppdatere) {
            return oppdatere.map(AdopsjonBuilder::oppdatere).orElseGet(AdopsjonBuilder::ny);
        }

        public AdopsjonBuilder medAdoptererAlene(boolean adoptererAlene) {
            this.kladd.setAdoptererAlene(adoptererAlene);
            return this;
        }

        public AdopsjonBuilder medErEktefellesBarn(boolean erEktefellesBarn) {
            this.kladd.setErEktefellesBarn(erEktefellesBarn);
            return this;
        }

        public AdopsjonBuilder medOmsorgsovertakelseDato(LocalDate omsorgsovertakelseDato) {
            this.kladd.setOmsorgsovertakelseDato(omsorgsovertakelseDato);
            return this;
        }

        public AdopsjonBuilder medAnkomstDato(LocalDate ankomstDato) {
            this.kladd.setAnkomstNorgeDato(ankomstDato);
            return this;
        }

        public AdopsjonBuilder medForeldreansvarDato(LocalDate foreldreansvarDato) {
            this.kladd.setForeldreansvarDato(foreldreansvarDato);
            return this;
        }


        public AdopsjonBuilder medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType vilkårType) {
            this.kladd.setOmsorgsovertakelseVilkårType(vilkårType);
            return this;
        }

        Adopsjon build() {
            return kladd;
        }

        boolean getErOppdatering() {
            return oppdatering;
        }
    }
}
