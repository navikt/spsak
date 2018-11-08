package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;

public class FastsettePerioderRegelOrkestreringSporingTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void fastsette_perioder_regel_skal_produsere_sporing_i_json_format() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> resultatListe = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultatListe).hasSize(3);
        resultatListe
                .forEach(resultat -> {
                    try {
                        assertThat(new ObjectMapper().readValue(resultat.getInnsendtGrunnlag(), HashMap.class)).isNotNull().isNotEmpty();
                        assertThat(new ObjectMapper().readValue(resultat.getEvalueringResultat(), HashMap.class)).isNotNull().isNotEmpty();
                    } catch (IOException e) {
                        fail("JSON mapping feiler.");
                    }
                });
    }

}
