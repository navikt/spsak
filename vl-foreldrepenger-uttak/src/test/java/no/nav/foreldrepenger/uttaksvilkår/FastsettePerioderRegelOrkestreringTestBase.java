package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public abstract class FastsettePerioderRegelOrkestreringTestBase {
    protected static final AktivitetIdentifikator AREBEIDSFORHOLD = AktivitetIdentifikator.forArbeid("000000000", "1");

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();

    protected FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
            .medSaldo(MØDREKVOTE, 50)
            .medSaldo(FEDREKVOTE, 50)
            .medSaldo(FELLESPERIODE, 130);

    protected LocalDate førsteLovligeUttaksdag(LocalDate fødselsdag) {
        return fødselsdag.withDayOfMonth(1).minusMonths(3);
    }

    protected void verifiserPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Perioderesultattype forventetResultat, Stønadskontotype stønadskontotype) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(forventetResultat);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
    }

    protected void verifiserAvslåttPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Stønadskontotype stønadskontotype, IkkeOppfyltÅrsak ikkeOppfyltÅrsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


    protected void verifiserManuellBehandlingPeriode(UttakPeriode periode, LocalDate forventetFom, LocalDate forventetTom, Stønadskontotype stønadskontotype, IkkeOppfyltÅrsak ikkeOppfyltÅrsak, Manuellbehandlingårsak manuellbehandlingårsak) {
        assertThat(periode.getFom()).isEqualTo(forventetFom);
        assertThat(periode.getTom()).isEqualTo(forventetTom);
        assertThat(periode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(periode.getStønadskontotype()).isEqualTo(stønadskontotype);
        assertThat(periode.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
        assertThat(periode.getÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
    }


}
