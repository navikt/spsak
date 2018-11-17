package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.HarAktørId;

public class PersonInformasjonBuilderTest {

    @Test
    public void skal_tilbakestille_kladden_ved_oppdatering_men_ikke_slette_hovedsøker_og_annenpart_når_annenpart_har_ingen_relasjoner() {

        AktørId søker = new AktørId("1");
        AktørId anpa = new AktørId("2");
        AktørId brn1 = new AktørId("3");
        AktørId brn2 = new AktørId("4");

        PersonInformasjonBuilder førsteInnhenting = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        førsteInnhenting.leggTil(lagPersonopplysning(søker, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(anpa, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(brn1, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(brn2, førsteInnhenting));

        førsteInnhenting.leggTil(lagRelasjon(søker, brn1, RelasjonsRolleType.MORA, førsteInnhenting));
        førsteInnhenting.leggTil(lagRelasjon(søker, brn2, RelasjonsRolleType.MORA, førsteInnhenting));

        PersonInformasjon informasjon = førsteInnhenting.build();

        assertThat(informasjon.getPersonopplysninger()).hasSize(4);
        assertThat(informasjon.getRelasjoner()).hasSize(2);

        PersonInformasjonBuilder oppdater = PersonInformasjonBuilder.oppdater(Optional.of(informasjon), PersonopplysningVersjonType.REGISTRERT);

        assertThat(oppdater.build().getPersonopplysninger()).hasSize(4);
        assertThat(oppdater.build().getRelasjoner()).hasSize(2);

        oppdater.tilbakestill(søker);

        assertThat(oppdater.build().getPersonopplysninger().stream().map(HarAktørId::getAktørId)).containsExactly(søker);
        assertThat(oppdater.build().getRelasjoner()).hasSize(0);

    }

    @Test
    public void skal_tilbakestille_kladden_ved_oppdatering_men_ikke_slette_hovedsøker_og_annenpart_når_annenpart_har_relasjoner() {

        AktørId søker = new AktørId("1");
        AktørId anpa = new AktørId("2");
        AktørId brn1 = new AktørId("3");
        AktørId brn2 = new AktørId("4");

        PersonInformasjonBuilder førsteInnhenting = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        førsteInnhenting.leggTil(lagPersonopplysning(søker, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(anpa, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(brn1, førsteInnhenting));
        førsteInnhenting.leggTil(lagPersonopplysning(brn2, førsteInnhenting));

        førsteInnhenting.leggTil(lagRelasjon(søker, anpa, RelasjonsRolleType.EKTE, førsteInnhenting));
        førsteInnhenting.leggTil(lagRelasjon(søker, brn1, RelasjonsRolleType.MORA, førsteInnhenting));
        førsteInnhenting.leggTil(lagRelasjon(søker, brn2, RelasjonsRolleType.MORA, førsteInnhenting));
        førsteInnhenting.leggTil(lagRelasjon(anpa, brn1, RelasjonsRolleType.FARA, førsteInnhenting));

        PersonInformasjon informasjon = førsteInnhenting.build();

        assertThat(informasjon.getPersonopplysninger()).hasSize(4);
        assertThat(informasjon.getRelasjoner()).hasSize(4);

        PersonInformasjonBuilder oppdater = PersonInformasjonBuilder.oppdater(Optional.of(informasjon), PersonopplysningVersjonType.REGISTRERT);

        assertThat(oppdater.build().getPersonopplysninger()).hasSize(4);
        assertThat(oppdater.build().getRelasjoner()).hasSize(4);

        oppdater.tilbakestill(søker);

        assertThat(oppdater.build().getPersonopplysninger().stream().map(HarAktørId::getAktørId)).containsExactly(søker);
        assertThat(oppdater.build().getRelasjoner()).hasSize(0);

    }

    private PersonInformasjonBuilder.RelasjonBuilder lagRelasjon(AktørId fra, AktørId til, RelasjonsRolleType type, PersonInformasjonBuilder informasjonBuilder) {
        return informasjonBuilder.getRelasjonBuilder(fra, til, type);
    }


    private PersonInformasjonBuilder.PersonopplysningBuilder lagPersonopplysning(AktørId aktørId, PersonInformasjonBuilder informasjonBuilder) {
        return informasjonBuilder
            .getPersonopplysningBuilder(aktørId)
            .medSivilstand(SivilstandType.GIFT)
            .medRegion(Region.NORDEN)
            .medNavn("Richard Feynman")
            .medFødselsdato(LocalDate.now())
            .medKjønn(NavBrukerKjønn.KVINNE);
    }
}
