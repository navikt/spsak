package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class FamilieHendelseEntitetTest {

    @Test
    public void skal_rapportere_at_hendelse_omhandler_døde_barn_ved_forekomst_av_dødsdato() {
        final FamilieHendelseBuilder builder = FamilieHendelseBuilder.ny(HendelseVersjonType.BEKREFTET);

        builder.leggTilBarn(LocalDate.now(), LocalDate.now().plusDays(1));

        final FamilieHendelse hendelse = builder.build();

        assertThat(hendelse.getBarna()).isNotEmpty();
        assertThat(hendelse.getInnholderDødtBarn()).isTrue();
        assertThat(hendelse.getInnholderDøfødtBarn()).isFalse();
    }

    @Test
    public void skal_rapportere_at_hendelse_omhandler_døfødt_barn_ved_dødsdato_er_lik_fødselsdato() {
        final FamilieHendelseBuilder builder = FamilieHendelseBuilder.ny(HendelseVersjonType.BEKREFTET);

        builder.leggTilBarn(LocalDate.now());
        builder.leggTilBarn(LocalDate.now(), LocalDate.now());

        final FamilieHendelse hendelse = builder.build();

        assertThat(hendelse.getBarna()).isNotEmpty();
        assertThat(hendelse.getInnholderDødtBarn()).isTrue();
        assertThat(hendelse.getInnholderDøfødtBarn()).isTrue();
    }
}
