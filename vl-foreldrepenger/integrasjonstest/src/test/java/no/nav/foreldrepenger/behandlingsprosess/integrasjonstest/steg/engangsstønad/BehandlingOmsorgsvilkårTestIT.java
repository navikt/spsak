package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_OMSORGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OMSORGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.STD_BARN_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.STD_MANN_AKTØR_ID;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.STD_MANN_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad.søknad.SoeknadsskjemaEngangsstoenadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.GrunnlagForAnsvarsovertakelse;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OmsorgsvilkårAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklartDataBarnDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingOmsorgsvilkårTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private DokumentmottakTestUtil hjelper;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    private BehandlingskontrollKontekst kontekst;

    @Test
    public void omsorgsovertakelse_mann_happy_case() throws Exception {
        // Arrange steg 1: Behandle søknad -> aksjonspunkt for å avklare vilkårtype for omsorgsovertakelse
        Fagsak fagsak = byggFagsakForSøknadYtelsestypeAdopsjon(STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(STD_BARN_FNR);
        LocalDate søknadsdato = LocalDate.now().plusDays(7L);
        LocalDate omsorgsovertakelseDato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFarOmsorgsovertakelse(fødselsdato, søknadsdato, omsorgsovertakelseDato));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertThat(repository.hentAlle(PersonopplysningEntitet.class)).hasSize(2);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange Steg 2: Avklar fakta fra GUI -> Vilkår for omsorgsovertakelse settes til Omsorgsvilkåret
        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto aksjonspunktDto = byggOmsorgovertakelseAksjonspunktDto(behandlingId);
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(aksjonspunktDto), behandlingId);

        // Act
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(OMSORGSVILKÅRET, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
            resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
            resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Steg 3: Sett omsorgsvilkåret til oppfylt GUI -> Omsorgsvilkåret oppfylles, totrinnsbehandling er trigget.
        OmsorgsvilkårAksjonspunktDto omsorgDto = byggOmsorgsvilkårAksjonspunktDto(behandlingId, true);

        // Act
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(omsorgDto), behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
            resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, UTFØRT),
            resultat(FORESLÅ_VEDTAK, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    @Test
    public void omsorgsovertakelse_mann_happy_case_gammelt_søknadsformat() throws Exception {
        // Arrange steg 1: Behandle søknad -> aksjonspunkt for å avklare vilkårtype for omsorgsovertakelse
        Fagsak fagsak = byggFagsakForSøknadYtelsestypeAdopsjon(STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(STD_BARN_FNR);
        LocalDate omsorgsovertakelseDato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandlingGammeltSøknadsformat(fagsak, søknadFarOmsorgGammeltFormat(
            GrunnlagForAnsvarsovertakelse.OVERTATT_OMSORG_INNEN_53_UKER_ADOPSJON, fødselsdato,
            omsorgsovertakelseDato));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertThat(repository.hentAlle(PersonopplysningEntitet.class)).hasSize(2);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange Steg 2: Avklar fakta fra GUI -> Vilkår for omsorgsovertakelse settes til Omsorgsvilkåret
        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto aksjonspunktDto = byggOmsorgovertakelseAksjonspunktDto(behandlingId);
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(aksjonspunktDto), behandlingId);

        // Act
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(OMSORGSVILKÅRET, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
            resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
            resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Steg 3: Sett omsorgsvilkåret til oppfylt GUI -> Omsorgsvilkåret oppfylles, totrinnsbehandling er trigget.
        OmsorgsvilkårAksjonspunktDto omsorgDto = byggOmsorgsvilkårAksjonspunktDto(behandlingId, true);

        // Act
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(omsorgDto), behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
            resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, UTFØRT),
            resultat(FORESLÅ_VEDTAK, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    @Test
    public void omsorgsovertakelse_når_vilkår_blir_satt_til_ikke_oppfylt() throws Exception {
        // Arrange steg 1: Behandle søknad -> aksjonspunkt for å avklare vilkårtype for omsorgsovertakelse
        Fagsak fagsak = byggFagsakForSøknadYtelsestypeAdopsjon(STD_MANN_AKTØR_ID, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(STD_BARN_FNR);
        LocalDate søknadsdato = LocalDate.now().plusDays(7L);
        LocalDate omsorgsovertakelseDato = LocalDate.now();

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadFarOmsorgsovertakelse(fødselsdato, søknadsdato, omsorgsovertakelseDato));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertThat(repository.hentAlle(PersonopplysningEntitet.class)).hasSize(2);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange steg 2: Avklar fakta fra GUI -> Vilkår for omsorgsovertakelse settes til Omsorgsvilkåret
        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto aksjonspunktDto = byggOmsorgovertakelseAksjonspunktDto(behandlingId);

        // Act
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(aksjonspunktDto), behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(OMSORGSVILKÅRET, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
            resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange steg 3: Sett omsorgsvilkåret til ikke oppfylt GUI ->
        //    omsorgsvilkåret blir satt til IKKE_OPPFYLT og det lagres ned en avslagskode på vilkåret
        OmsorgsvilkårAksjonspunktDto omsorgDto = byggOmsorgsvilkårAksjonspunktDto(behandlingId, false);

        // Act
        applikasjonstjeneste.bekreftAksjonspunkter(singletonList(omsorgDto), behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(OMSORGSVILKÅRET, IKKE_OPPFYLT, Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O));
        assertUtil.assertAksjonspunkter(
            asList(resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, UTFØRT),
                resultat(MANUELL_VURDERING_AV_OMSORGSVILKÅRET, UTFØRT),
                resultat(FORESLÅ_VEDTAK, OPPRETTET),
                resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    private SoeknadsskjemaEngangsstoenad søknadFarFødselGammeltFormat(GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse, LocalDate fødselsdato) {
        return new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .fødsel()
                .engangsstønadFar()
                .medGrunnlagForAnsvarsovertakelse(grunnlagForAnsvarsovertakelse)
                .medPersonidentifikator(STD_MANN_FNR)
                .medVedleggsliste(emptyList()) // Kan denne defaultes?
                .medFødselsdatoer(Collections.singletonList(fødselsdato))
                .medTidligereOppholdNorge(true)
                .medOppholdNorgeNå(true)
                .medFremtidigOppholdNorge(true)
                .build();
    }

    private AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto byggOmsorgovertakelseAksjonspunktDto(Long behandlingId) {

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Stream<Aksjonspunkt> streammAp = behandling.getAksjonspunkter().stream();
        Long aksjonspunktId = streammAp
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon()
                .equals(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE))
            .map(Aksjonspunkt::getId)
            .findFirst()
            .orElse(null);
        assertThat(aksjonspunktId).isNotNull();
        Map<Long, LocalDate> map = new HashMap<>();
        map.put(1L, LocalDate.now());

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(LocalDate.now());
        dto.setVilkårType(OMSORGSVILKÅRET);
        dto.setForeldre(emptyList());

        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        UidentifisertBarn adopsjonBarn = familieHendelseGrunnlag.getGjeldendeBarna().iterator().next();
        AvklartDataBarnDto barn = new AvklartDataBarnDto();
        barn.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);
        barn.setFodselsdato(adopsjonBarn.getFødselsdato());
        dto.setBarn(singletonList(barn));
        return dto;
    }

    private OmsorgsvilkårAksjonspunktDto byggOmsorgsvilkårAksjonspunktDto(Long behandlingId, boolean vilkårOk) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Stream<Aksjonspunkt> streammAp = behandling.getAksjonspunkter().stream();
        Long aksjonspunktId = streammAp
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(MANUELL_VURDERING_AV_OMSORGSVILKÅRET))
            .map(Aksjonspunkt::getId)
            .findFirst()
            .orElse(null);
        assertThat(aksjonspunktId).isNotNull();

        return new OmsorgsvilkårAksjonspunktDto("Grunn", vilkårOk, "1008");
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        repository.flush();
    }

    private Soeknad søknadFarOmsorgsovertakelse(LocalDate fødselsdato, LocalDate søknadsdato,
                                                LocalDate omsorgsovertakelseDato) {
        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(søknadsdato)
            .medOmsorgsovertakelse(new SøknadTestdataBuilder.OmsorgsovertakelseBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelseDato)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato))
                .medAntallBarn(1)
                .medOmsorgsovertakelseaarsaker(FarSøkerType.OVERTATT_OMSORG))
            .build();
    }

    private SoeknadsskjemaEngangsstoenad søknadFarOmsorgGammeltFormat(GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse, LocalDate fødselsdato, LocalDate omsorgsovertakelseDato) {
        return new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .adopsjon()
                .engangsstønadFar()
                .medGrunnlagForAnsvarsovertakelse(grunnlagForAnsvarsovertakelse)
                .medPersonidentifikator(STD_MANN_FNR)
                .medVedleggsliste(emptyList()) // Kan denne defaultes?
                .medOmsorgsovertakelsesdato(omsorgsovertakelseDato)
                .medFødselsdatoer(Collections.singletonList(fødselsdato))
                .medTidligereOppholdNorge(true)
                .medOppholdNorgeNå(true)
                .medFremtidigOppholdNorge(true)
                .build();
    }

    private Fagsak byggFagsakForSøknadYtelsestypeAdopsjon(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medSaksnummer(new Saksnummer("123"))
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);

        return fagsak;
    }

    private Fagsak byggFagsakForSøknadOmYtelsestypeFødsel(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medSaksnummer(new Saksnummer("123"))
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);

        return fagsak;
    }


}
