package no.nav.foreldrepenger.domene.uttak.beregnkontoer.impl;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittRettighet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.domene.uttak.UttakOmsorgUtil;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.BeregnKontoerGrunnlag;
import no.nav.foreldrepenger.regler.uttak.beregnkontoer.grunnlag.Dekningsgrad;

public class StønadskontoRegelOversetter {

    public BeregnKontoerGrunnlag tilRegelmodell(RelasjonsRolleType relasjonsRolleType, FamilieHendelseGrunnlag familieHendelseGrunnlag, YtelseFordelingAggregat ytelseFordelingAggregat,FagsakRelasjon fagsakRelasjon, boolean harSøkerRett) {
        requireNonNull(familieHendelseGrunnlag);
        requireNonNull(fagsakRelasjon);

        FamilieHendelse familieHendelse = familieHendelseGrunnlag.getGjeldendeVersjon();

        OppgittRettighet oppgittRettighet = ytelseFordelingAggregat.getOppgittRettighet();

        boolean annenForeldreHarRett = UttakOmsorgUtil.harAnnenForelderRett(oppgittRettighet);

        boolean erFødsel = FamilieHendelseType.FØDSEL.equals(familieHendelse.getType()) || FamilieHendelseType.TERMIN.equals(familieHendelse.getType());

        BeregnKontoerGrunnlag.Builder grunnlagBuilder = BeregnKontoerGrunnlag.builder()
            .medAntallBarn(familieHendelse.getAntallBarn())
            .medDekningsgrad(oversett(fagsakRelasjon.getDekningsgrad().getVerdi()))
            .erFødsel(erFødsel)
            .medFamiliehendelsesdato(finnFamiliehendelsesdato(familieHendelseGrunnlag, erFødsel));

        boolean aleneomsorg = UttakOmsorgUtil.harAleneomsorg(ytelseFordelingAggregat);
        if (relasjonsRolleType.equals(RelasjonsRolleType.MORA)) {
            return grunnlagBuilder
                .morRett(harSøkerRett)
                .farRett(annenForeldreHarRett)
                .morAleneomsorg(aleneomsorg)
                .build();
        } else {
            return grunnlagBuilder
                .morRett(annenForeldreHarRett)
                .farRett(harSøkerRett)
                .farAleneomsorg(aleneomsorg)
                .build();
        }
    }

    private Dekningsgrad oversett(int dekningsgrad) {
        if (Objects.equals(dekningsgrad, OppgittDekningsgradEntitet.HUNDRE_PROSENT)) {
            return Dekningsgrad.DEKNINGSGRAD_100;
        }
        return Dekningsgrad.DEKNINGSGRAD_80;
    }

    private LocalDate finnFamiliehendelsesdato(FamilieHendelseGrunnlag familieHendelseGrunnlag, boolean erFødsel) {
        if (erFødsel) {
            return familieHendelseGrunnlag.finnGjeldendeFødselsdato();
        }
        Optional<Adopsjon> gjeldendeAdopsjon = familieHendelseGrunnlag.getGjeldendeAdopsjon();
        if (gjeldendeAdopsjon.isPresent()) {
            return gjeldendeAdopsjon.get().getOmsorgsovertakelseDato();
        }
        throw new IllegalStateException("Finner ikke familiehendelsesdato.");
    }

}
