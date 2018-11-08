package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class IntegrasjonstestAssertUtils {

    private Repository repository;

    public IntegrasjonstestAssertUtils(Repository repository) {
        this.repository = repository;
    }

    public void assertVilkårresultatOgRegelmerknad(VilkårResultatType vilkårResultatType, VilkårTestutfall testutfall) {
        assertVilkårresultatOgRegelmerknad(vilkårResultatType, singletonList(testutfall));
    }

    public void assertAksjonspunkter(AksjonspunktTestutfall... aksjonspunktTestutfall) {
        assertAksjonspunkter(Arrays.asList(aksjonspunktTestutfall));
    }

    public void assertVilkårresultatOgRegelmerknad(VilkårResultatType vilkårResultatType,
                                                   List<VilkårTestutfall> forventetResultat) {

        // VilkårResultat
        List<VilkårResultat> vilkårResultater = repository.hentAlle(VilkårResultat.class);
        assertThat(vilkårResultater)
            .as("Sjekker 1:1 mellom Behandling og VilkårResultat")
            .hasSize(1);

        VilkårResultat vilkårResultat = vilkårResultater.get(0);
        assertThat(vilkårResultat.getVilkårResultatType().getKode())
            .as("Sjekker totalt vilkårResultat")
            .isEqualTo(vilkårResultatType.getKode());

        // Vilkår
        List<Vilkår> vilkårene = vilkårResultat.getVilkårene();
        if (forventetResultat.isEmpty()) {
            assertThat(vilkårene)
                .as("Forventer at det ikke har blitt opprettet noen vilkår")
                .isEmpty();
        }
        assertThat(vilkårene.stream().map(Vilkår::getVilkårType).sorted().collect(toList()))
            .as("Sjekker at opprettede vilkår er av riktig type")
            .containsAll(forventetResultat.stream().map(VilkårTestutfall::getVilkårType).sorted().collect(toList()));

        forventetResultat.forEach(forventet -> {
            Vilkår vilkår = vilkårene.stream()
                .filter(v -> v.getVilkårType().equals(forventet.getVilkårType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Skal ikke være mulig å havne her."));
            String vilkårNavn = forventet.getVilkårType().getNavn();
            // sjekker utfall
            assertThat(vilkår.getGjeldendeVilkårUtfall())
                .as("Sjekker at vilkår '" + vilkårNavn + "' har forventet vilkårsutfall")
                .isEqualTo(forventet.getUtfallType());
            // sjekker merknad
            assertThat(vilkår.getVilkårUtfallMerknad())
                .as("Sjekker at vilkår '" + vilkårNavn + "' har forventet vilkårsutfallmerknad")
                .isEqualTo(forventet.getUtfallMerknad());
            // sjekker avslagsårsak
            assertThat(vilkår.getAvslagsårsak())
                .as("Sjekker at vilkår '" + vilkårNavn + "' har forventet avslagsårsak")
                .isEqualTo(forventet.getAvslagsårsak());
            // sjekker overstyring
            assertThat(vilkår.erOverstyrt())
                .as("Sjekker at vilkår '" + vilkårNavn + "' har forventet overstyringsflagg")
                .isEqualTo(forventet.erOverstyrt());
        });
    }

    public void assertAksjonspunkter(List<AksjonspunktTestutfall> aksjonspunktTestutfall) {
        List<Aksjonspunkt> aksjonspunkter = repository.hentAlle(Aksjonspunkt.class);

        if (aksjonspunktTestutfall.isEmpty()) {
            assertThat(aksjonspunkter)
                .as("Sjekker at det ikke har blitt opprettet noen aksjonspunkter")
                .isEmpty();
        } else {
            Set<AksjonspunktDefinisjon> forventet = aksjonspunktTestutfall.stream().map(a -> a.getAksjonspunktDefinisjon()).collect(Collectors.toSet());
            Set<AksjonspunktDefinisjon> actual = aksjonspunkter.stream().map(a -> a.getAksjonspunktDefinisjon()).collect(Collectors.toSet());
            // Må sortere settet av for at containsExactlyElementsOf skal fungere, ellers feil: "not in the same order"
            assertThat(sorterApDef(actual)).containsExactlyElementsOf(sorterApDef(forventet));

            aksjonspunktTestutfall.forEach(a -> {
                Aksjonspunkt punkt = aksjonspunkter.stream()
                    .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(a.getAksjonspunktDefinisjon()))
                    .findFirst()
                    .orElse(null);
                assertThat(punkt).as("Fant ikke " + a.getAksjonspunktDefinisjon() + ", har: " + aksjonspunkter).isNotNull();
                assertThat(punkt.getStatus())
                    .as("Sjekker at aksjonspunkt '" + punkt.getAksjonspunktDefinisjon() + "' har forventet status")
                    .isEqualTo(a.getStatus());
            });
        }
    }

    private List<AksjonspunktDefinisjon> sorterApDef(Set<AksjonspunktDefinisjon> actual) {
        return actual.stream()
            .sorted((apDef1, apDef2) -> (apDef1.getKode().compareTo(apDef2.getKode())))
            .collect(toList());
    }

    public void assertBehandlingÅrsak(BehandlingÅrsakTestutfall behandlingÅrsakTestutfall) {
        List<BehandlingÅrsak> faktiskForAngittBehandling = repository.hentAlle(BehandlingÅrsak.class).stream()
            .filter(b -> b.getBehandling().getId().equals(behandlingÅrsakTestutfall.getBehandlingId()))
            .collect(toList());

        final List<BehandlingÅrsakType> forventet = behandlingÅrsakTestutfall.getForventet();

        if (forventet.isEmpty()) {
            assertThat(faktiskForAngittBehandling)
                .as("Sjekker at det ikke har blitt opprettet noen BehandlingÅrsak")
                .isEmpty();
        } else {
            forventet.forEach(båt -> {
                BehandlingÅrsak innslag = faktiskForAngittBehandling.stream()
                    .filter(bå -> Objects.equals(båt, bå.getBehandlingÅrsakType()))
                    .findFirst()
                    .orElse(null);
                assertThat(innslag).as("Fant ikke " + båt.getKode()).isNotNull();
            });
            assertThat(faktiskForAngittBehandling.stream().map(BehandlingÅrsak::getBehandlingÅrsakType).collect(toList()))
                .containsExactlyInAnyOrder(forventet.toArray(new BehandlingÅrsakType[forventet.size()]));
        }
    }

    public void assertSisteBeregning(BeregningTestutfall beregningTestutfall) {
        List<BeregningResultat> alleBeregningResultater = repository.hentAlle(BeregningResultat.class);

        List<BeregningResultat> beregningResultat = alleBeregningResultater.stream()
            .filter(b -> b.getOriginalBehandling().getId().equals(beregningTestutfall.getBehandlingId()))
            .collect(toList());

        if (beregningTestutfall == null) {
            assertThat(beregningResultat)
                .as("Sjekker at det ikke har blitt opprettet noen beregninger")
                .isEmpty();
        } else {
            assertThat(beregningResultat.size())
                .as("Sjekker at det bare er ett BeregningResultat for behandlingen")
                .isEqualTo(1);
            assertThat(beregningResultat.get(0).isOverstyrt())
                .as("Sjekker at beregningsresultatet har forventet overstyringsflagg")
                .isEqualTo(beregningTestutfall.erOverstyrt());
            if (beregningTestutfall.getBeregnetTilkjentYtelse() != null) {
                assertThat(beregningResultat.get(0).getSisteBeregning().isPresent())
                    .as("Sjekker at beregningsresultatet har en beregning")
                    .isEqualTo(true);
                assertThat(beregningResultat.get(0).getSisteBeregning().get().isOverstyrt())
                    .as("Sjekker at beregningen har forventet overstyringsflagg")
                    .isEqualTo(beregningTestutfall.erOverstyrt());
                assertThat(beregningResultat.get(0).getSisteBeregning().get().getBeregnetTilkjentYtelse())
                    .as("Sjekker at beregningen har forventet tilkjent ytelse")
                    .isEqualTo(beregningTestutfall.getBeregnetTilkjentYtelse());
                assertThat(beregningResultat.get(0).getSisteBeregning().get().getOpprinneligBeregnetTilkjentYtelse())
                    .as("Sjekker at beregningen har forventet opprinnelig tilkjent ytelse")
                    .isEqualTo(beregningTestutfall.getOpprinneligBeregnetTilkjentYtelse());
            } else {
                assertThat(beregningResultat.get(0).getSisteBeregning().isPresent())
                    .as("Sjekker at beregningsresultatet ikke har en beregning")
                    .isEqualTo(false);
            }
        }
    }

    public void assertHistorikkinnslag(List<HistorikkinnslagType> historikkinnslagForventet) {
        List<Historikkinnslag> historikkinnslagFaktisk = repository.hentAlle(Historikkinnslag.class);

        if (historikkinnslagForventet.isEmpty()) {
            assertThat(historikkinnslagFaktisk)
                .as("Sjekker at det ikke har blitt opprettet noen historikkinnslag")
                .isEmpty();
        } else {
            historikkinnslagForventet.forEach(h -> {
                Historikkinnslag innslag = historikkinnslagFaktisk.stream()
                    .filter(historikkinnslag -> historikkinnslag.getType().getKode().equals(h.getKode()))
                    .findFirst()
                    .orElse(null);
                assertThat(innslag).as("Fant ikke " + h.getKode()).isNotNull();
            });
            assertThat(historikkinnslagFaktisk.stream().map(Historikkinnslag::getType).collect(toList()))
                .containsExactlyInAnyOrder(historikkinnslagForventet.toArray(new HistorikkinnslagType[historikkinnslagForventet.size()]));
        }
    }

    public void assertOppgaveBehandlingKobling(OppgaveÅrsak... åpneOppgaverForventet) {
        assertOppgaveBehandlingKobling(asList(åpneOppgaverForventet));
    }

    public void assertOppgaveBehandlingKobling(List<OppgaveÅrsak> åpneOppgaverForventet) {
        List<OppgaveBehandlingKobling> oppgaverFaktisk = repository.hentAlle(OppgaveBehandlingKobling.class);

        OppgaveÅrsak[] oppgaveArray = åpneOppgaverForventet.toArray(new OppgaveÅrsak[åpneOppgaverForventet.size()]);
        assertThat(oppgaverFaktisk.stream()
            .filter(oppgave -> !oppgave.isFerdigstilt())
            .map(OppgaveBehandlingKobling::getOppgaveÅrsak).collect(toList()))
            .containsExactlyInAnyOrder(oppgaveArray);
    }
}
