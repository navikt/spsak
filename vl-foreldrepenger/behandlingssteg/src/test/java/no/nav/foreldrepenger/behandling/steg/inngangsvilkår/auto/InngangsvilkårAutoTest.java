package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto;


import static org.hamcrest.CoreMatchers.equalTo;

import java.time.Period;
import java.util.Objects;
import java.util.function.BiFunction;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsvilkårResultat;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class InngangsvilkårAutoTest {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void vurderMedlemskap() {
        new VilkårVurdering().vurderVilkår(collector, VilkårType.MEDLEMSKAPSVILKÅRET);
    }

    @Test
    public void vurderOpptjening() {
        final BiFunction<VilkårResultat, Object, Boolean> extraDataSjekk = (resultat, extra) ->
            {
                collector.checkThat("Avvik i opptjeningstid for " + resultat,
                    ((OpptjeningsvilkårResultat) extra).getResultatOpptjent(), equalTo(Period.parse(resultat.getOpptjentTid())));
                return ((OpptjeningsvilkårResultat) extra).getResultatOpptjent().equals(Period.parse(resultat.getOpptjentTid()));
            };
        new VilkårVurdering().vurderVilkår(collector, VilkårType.OPPTJENINGSVILKÅRET, extraDataSjekk);
    }

    @Test
    public void fastsettOpptjeningsPeriode() {
        final BiFunction<VilkårResultat, Object, Boolean> extraDataSjekk = (resultat, extra) -> {
            collector.checkThat("Avvik i opptjeningsperiode (fom) for " + resultat,
                ((OpptjeningsPeriode) extra).getOpptjeningsperiodeFom(), equalTo(resultat.getOpptjeningFom()));
            collector.checkThat("Avvik i opptjeningsperiode (tom) for " + resultat,
                ((OpptjeningsPeriode) extra).getOpptjeningsperiodeTom(), equalTo(resultat.getOpptjeningTom()));
            
            return Objects.equals(((OpptjeningsPeriode) extra).getOpptjeningsperiodeFom(), resultat.getOpptjeningFom())
                    && Objects.equals(((OpptjeningsPeriode) extra).getOpptjeningsperiodeTom(), resultat.getOpptjeningTom());
        };
        new VilkårVurdering().vurderVilkår(collector, VilkårType.OPPTJENINGSPERIODEVILKÅR, extraDataSjekk);
    }

    @Test
    @Ignore("Kan brukes for å debugge et enkelt case")
    public void run_one_test() {
        new VilkårVurdering().vurderVilkår("1007351-5c69e8b1-b19e-4ad7-8a60-7d4bb8885944",
            collector,
            VilkårType.FØDSELSVILKÅRET_MOR,
            VilkårVurdering.DO_NOTHING);
    }
}
