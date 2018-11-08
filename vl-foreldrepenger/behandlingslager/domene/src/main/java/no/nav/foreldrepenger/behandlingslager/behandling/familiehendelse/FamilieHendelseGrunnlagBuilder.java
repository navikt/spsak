package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import java.util.Optional;

public class FamilieHendelseGrunnlagBuilder {
    private final FamilieHendelseGrunnlagEntitet kladd;

    private FamilieHendelseGrunnlagBuilder(FamilieHendelseGrunnlagEntitet kladd) {
        this.kladd = kladd;
    }

    private static FamilieHendelseGrunnlagBuilder nytt() {
        return new FamilieHendelseGrunnlagBuilder(new FamilieHendelseGrunnlagEntitet());
    }

    private static FamilieHendelseGrunnlagBuilder oppdatere(FamilieHendelseGrunnlag kladd) {
        return new FamilieHendelseGrunnlagBuilder(new FamilieHendelseGrunnlagEntitet(kladd));
    }

    /**
     * Kommer til å endre scope
     * Benytt repository metode.
     *
     * @param kladd eksisterende versjon
     * @return Builder
     */
    public static FamilieHendelseGrunnlagBuilder oppdatere(Optional<FamilieHendelseGrunnlag> kladd) {
        return kladd.map(FamilieHendelseGrunnlagBuilder::oppdatere).orElseGet(FamilieHendelseGrunnlagBuilder::nytt);
    }

    public FamilieHendelseGrunnlagBuilder medSøknadVersjon(FamilieHendelseBuilder hendelseBuilder) {
        if (Optional.ofNullable(kladd.getSøknadVersjon()).isPresent()) {
            throw FamilieHendelseFeil.FACTORY.kanIkkeOppdatereSøknadVersjon().toException();
        }
        kladd.setSøknadHendelse((FamilieHendelseEntitet) hendelseBuilder.build());
        return this;
    }

    public FamilieHendelseGrunnlagBuilder medBekreftetVersjon(FamilieHendelseBuilder hendelse) {
        if (hendelse == null) {
            kladd.setBekreftetHendelse(null);
        } else if (!hendelse.getOpprinneligType().equals(HendelseVersjonType.BEKREFTET) || kladd.getBekreftetVersjon().isPresent() == hendelse.getErOppdatering()) {
            kladd.setBekreftetHendelse((FamilieHendelseEntitet) hendelse.build());
        } else {
            throw FamilieHendelseFeil.FACTORY.måBasereSegPåEksisterendeVersjon().toException();
        }
        return this;
    }

    public FamilieHendelseGrunnlagBuilder medOverstyrtVersjon(FamilieHendelseBuilder hendelse) {
        if (hendelse == null) {
            kladd.setOverstyrtHendelse(null);
        } else if (!hendelse.getOpprinneligType().equals(HendelseVersjonType.OVERSTYRT) || kladd.getOverstyrtVersjon().isPresent() == hendelse.getErOppdatering()) {
            kladd.setOverstyrtHendelse((FamilieHendelseEntitet) hendelse.build());
        } else {
            throw FamilieHendelseFeil.FACTORY.måBasereSegPåEksisterendeVersjon().toException();
        }
        return this;
    }

    FamilieHendelseGrunnlag getKladd() {
        return kladd;
    }

    public FamilieHendelseGrunnlag build() {
        return kladd;
    }
}
