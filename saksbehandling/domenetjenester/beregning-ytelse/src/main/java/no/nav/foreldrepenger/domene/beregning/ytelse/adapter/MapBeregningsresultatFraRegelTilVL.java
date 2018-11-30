package no.nav.foreldrepenger.domene.beregning.ytelse.adapter;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatFP;
import no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatPeriode;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningsgrunnlagUtil;

@ApplicationScoped
public class MapBeregningsresultatFraRegelTilVL {

    private MapBeregningsresultatFraRegelTilVL() {
        // default constructor
    }

    public static no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP mapFra(BeregningsresultatFP resultat, no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP eksisterendeResultat, VirksomhetRepository virksomhetRepository) {
        if (eksisterendeResultat.getBeregningsresultatPerioder().isEmpty()) {
            resultat.getBeregningsresultatPerioder().forEach(p -> mapFraPeriode(p, eksisterendeResultat, virksomhetRepository));
        } else {
            throw new IllegalArgumentException("Forventer ingen beregningsresultatPerioder");
        }
        return eksisterendeResultat;
    }

    private static no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode mapFraPeriode(BeregningsresultatPeriode resultatPeriode, no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP eksisterendeResultat, VirksomhetRepository virksomhetRepository) {
        no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode nyPeriode = no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode.builder()
            .medBeregningsresultatPeriodeFomOgTom(resultatPeriode.getFom(), resultatPeriode.getTom())
            .build(eksisterendeResultat);
        resultatPeriode.getBeregningsresultatAndelList().forEach(bra -> mapFraAndel(bra, nyPeriode, virksomhetRepository));
        return nyPeriode;
    }

    private static BeregningsresultatAndel mapFraAndel(no.nav.foreldrepenger.domene.beregning.regelmodell.BeregningsresultatAndel bra, no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode brp, VirksomhetRepository virksomhetRepository) {
        int dagsats = BeregningsgrunnlagUtil.nullSafeLong(bra.getDagsats()).intValue();
        int dagsatsFraBg = BeregningsgrunnlagUtil.nullSafeLong(bra.getDagsatsFraBg()).intValue();
        return BeregningsresultatAndel.builder()
            .medVirksomhet(bra.getArbeidsforhold() == null ? null :
                    (VirksomhetEntitet) virksomhetRepository.hent(bra.getArbeidsforhold().getOrgnr()).orElse(null))
            .medBrukerErMottaker(bra.erBrukerMottaker())
            .medDagsats(dagsats)
            .medStillingsprosent(bra.getStillingsprosent())
            .medUtbetalingsgrad(bra.getUtbetalingsgrad())
            .medDagsatsFraBg(dagsatsFraBg)
            .medAktivitetstatus(AktivitetStatusMapper.fraRegelTilVl(bra))
            .medArbforholdId(bra.getArbeidsforhold() ==null
                ? null: bra.getArbeidsforhold().getArbeidsforholdId())
            .medInntektskategori(InntektskategoriMapper.fraRegelTilVL(bra.getInntektskategori()))
            .build(brp);
    }
}
