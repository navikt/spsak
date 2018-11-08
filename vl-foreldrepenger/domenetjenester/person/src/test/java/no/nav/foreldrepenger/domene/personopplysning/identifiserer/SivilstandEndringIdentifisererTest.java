package no.nav.foreldrepenger.domene.personopplysning.identifiserer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class SivilstandEndringIdentifisererTest {

    private AktørId AKTØRID = new AktørId("1");


    private SivilstandEndringIdentifiserer sivilstandEndringIdentifiserer;

    @Before
    public void setup() {
        sivilstandEndringIdentifiserer = new SivilstandEndringIdentifiserer();
    }


    @Test
    public void testSivilstandUendret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(SivilstandType.GIFT);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(SivilstandType.GIFT);

        boolean erEndret = sivilstandEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at sivilstand er uendret").isFalse();
    }

    @Test
    public void testSivilstandEndret() {
        PersonopplysningGrunnlag personopplysningGrunnlag1 = opprettPersonopplysningGrunnlag(SivilstandType.GIFT);
        PersonopplysningGrunnlag personopplysningGrunnlag2 = opprettPersonopplysningGrunnlag(SivilstandType.UGIFT);

        boolean erEndret = sivilstandEndringIdentifiserer.erEndret(AKTØRID, personopplysningGrunnlag1, personopplysningGrunnlag2);
        assertThat(erEndret).as("Forventer at endring i sivilstand blir detektert.").isTrue();
    }

    private PersonopplysningGrunnlag opprettPersonopplysningGrunnlag(SivilstandType sivilstand) {
        final PersonInformasjonBuilder builder1 = PersonInformasjonBuilder.oppdater(Optional.empty(), PersonopplysningVersjonType.REGISTRERT);
        builder1.leggTil(builder1.getPersonopplysningBuilder(AKTØRID).medSivilstand(sivilstand));
        return PersonopplysningGrunnlagBuilder.oppdatere(Optional.empty()).medRegistrertVersjon(builder1).build();
    }
}
