package no.nav.foreldrepenger.dokumentbestiller.api.mal.mapper;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.InnvilgelseForeldrepengerMapper.ENDRING_BEREGNING_OG_UTTAK;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.SatsType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.DokumentMapperTjenesteProvider;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.uttak.OpphørFPTjeneste;

@ApplicationScoped
public class DokumentBehandlingsresultatMapper {
    private BeregningRepository beregningsRepository;
    private VilkårKodeverkRepository vilkårKodeverkRepository;
    private UttakRepository uttakRepository;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private FamilieHendelseTjeneste familiehendelseTjeneste;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private OpphørFPTjeneste opphørTjeneste;

    @Inject
    public DokumentBehandlingsresultatMapper(BehandlingRepositoryProvider repositoryProvider,
                                             DokumentMapperTjenesteProvider tjenesteProvider) {
        this.beregningsRepository = repositoryProvider.getBeregningRepository();
        this.vilkårKodeverkRepository = repositoryProvider.getVilkårKodeverkRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.personopplysningTjeneste = tjenesteProvider.getBasisPersonopplysningTjeneste();
        this.familiehendelseTjeneste = tjenesteProvider.getFamiliehendelseTjeneste();
        this.skjæringstidspunktTjeneste = tjenesteProvider.getSkjæringstidspunktTjeneste();
        this.opphørTjeneste = tjenesteProvider.getOpphørFPTjeneste();
    }

    public DokumentBehandlingsresultatMapper() {
        //For CDI
    }

    void mapDataRelatertTilBehandlingsResultat(final Behandling behandling, final DokumentTypeDto dto) {

        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        // Fritekstbrev
        dto.getDokumentBehandlingsresultatDto().setBrødtekst(behandlingsresultat.getFritekstbrev());
        dto.getDokumentBehandlingsresultatDto().setOverskrift(behandlingsresultat.getOverskrift());

        // Alle andre brevene.
        mapDataRelatertTilAvslag(behandling, dto);
        dto.getDokumentBehandlingsresultatDto().setFritekst(behandlingsresultat.getAvslagarsakFritekst());

        List<KonsekvensForYtelsen> konsekvenserForYtelsen = behandlingsresultat.getKonsekvenserForYtelsen();
        dto.getDokumentBehandlingsresultatDto().setKonsekvensForYtelse(kodeFra(konsekvenserForYtelsen));

        if (behandlingsresultat.getBehandlingResultatType() != null) {
            dto.getDokumentBehandlingsresultatDto().setBehandlingsResultat(behandlingsresultat.getBehandlingResultatType().getKode());
        }
        if (behandlingsresultat.getBeregningResultat() != null) {
            final Optional<Beregning> sisteBeregning = behandlingsresultat.getBeregningResultat().getSisteBeregning();
            if (sisteBeregning.isPresent()) {
                Beregning beregning = sisteBeregning.get();
                dto.getDokumentBehandlingsresultatDto().setBeløp(beregning.getBeregnetTilkjentYtelse());
            }
        }
    }

    private String kodeFra(List<KonsekvensForYtelsen> konsekvenserForYtelsen) {
        if (konsekvenserForYtelsen.contains(KonsekvensForYtelsen.ENDRING_I_BEREGNING)) { // viktigst å få med endring i beregning
            return konsekvenserForYtelsen.contains(KonsekvensForYtelsen.ENDRING_I_UTTAK) ?
                ENDRING_BEREGNING_OG_UTTAK : KonsekvensForYtelsen.ENDRING_I_BEREGNING.getKode();
        } else {
            return konsekvenserForYtelsen.isEmpty() ?
                KonsekvensForYtelsen.UDEFINERT.getKode() : konsekvenserForYtelsen.get(0).getKode(); // velger bare den første i listen (finnes ikke koder for andre ev. kombinasjoner)
        }
    }

    private void mapDataRelatertTilAvslag(final Behandling behandling, final DokumentTypeDto dto) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        FagsakYtelseType ytelseType = behandlingsresultat.getBehandling().getFagsakYtelseType();
        final Avslagsårsak avslagsårsak = behandlingsresultat.getAvslagsårsak();

        if (avslagsårsak != null && ytelseType.gjelderEngangsstønad()) {
            dto.getDokumentBehandlingsresultatDto().leggTilAvslagsårsak(avslagsårsak.getKode());
            dto.getDokumentBehandlingsresultatDto().setVilkårTypeKode(finnVilkårKode(behandlingsresultat.getBehandling(), avslagsårsak));
        } else if (ytelseType.gjelderForeldrepenger() && (avslagsårsak != null
            || behandlingsresultat.isBehandlingsresultatAvslåttOrOpphørt())) {

            mapDataFraUttak(behandlingsresultat.getBehandling(), dto);
            mapDataRelatertTilSkjæringstidspunkt(dto, behandling);
            if (avslagsårsak != null) {
                dto.getDokumentBehandlingsresultatDto().leggTilAvslagsårsak(avslagsårsak.getKode());
                String lovReferanse = avslagsårsak.getLovReferanse(ytelseType);
                if (lovReferanse == null) {
                    throw BrevFeil.FACTORY.manglerInfoOmLovhjemmelForAvslagsårsak(avslagsårsak.getKode()).toException();
                }
                dto.getDokumentBehandlingsresultatDto().leggTilLovhjemmelForAvslag(lovReferanse);
            }
            if (behandlingsresultat.isBehandlingsresultatOpphørt()) {
                mapDataRelatertTilOpphør(behandlingsresultat, dto);
            }
        }
    }

    private void mapDataRelatertTilOpphør(Behandlingsresultat behandlingsresultat, DokumentTypeDto dto) {
        Behandling behandling = behandlingsresultat.getBehandling();
        opphørTjeneste.getFørsteStønadsDato(behandling).ifPresent(dto.getDokumentBeregningsresultatDto()::setFørsteStønadsDato);
        opphørTjeneste.getOpphørsdato(behandling).ifPresent(dto.getDokumentBeregningsresultatDto()::setOpphorDato);
        if (dto.getDokumentBeregningsresultatDto().getFørsteStønadsDato().isPresent() &&
            dto.getDokumentBeregningsresultatDto().getOpphorDato().isPresent()) {
            if (dto.getDokumentBeregningsresultatDto().getFørsteStønadsDato().get().isEqual(
                dto.getDokumentBeregningsresultatDto().getOpphorDato().get())) {
                dto.getDokumentBeregningsresultatDto().setSisteStønadsDato(dto.getDokumentBeregningsresultatDto().getOpphorDato().get());
            } else {
                dto.getDokumentBeregningsresultatDto().setSisteStønadsDato(dto.getDokumentBeregningsresultatDto().getOpphorDato().get().minusDays(1));
            }
        }

        finnDødsdato(behandling, dto.getDokumentBehandlingsresultatDto().getAvslagsårsakListe()).ifPresent(dto.getDokumentBehandlingsresultatDto()::setDodsdato);
    }

    private void mapDataRelatertTilSkjæringstidspunkt(final DokumentTypeDto dto, Behandling behandling) {
        Sats sats = beregningsRepository.finnEksaktSats(SatsType.GRUNNBELØP, skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        if (sats != null) {
            dto.setHalvG(Math.round(sats.getVerdi() / 2.0));
        }
    }

    private void mapDataFraUttak(final Behandling behandling, final DokumentTypeDto dto) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        if (uttakResultat.isPresent()) {
            UttakResultatPerioderEntitet uttakResultatPeriodeEntitet = uttakResultat.get().getGjeldendePerioder();
            List<UttakResultatPeriodeEntitet> perioder = uttakResultatPeriodeEntitet.getPerioder();

            setÅrsaksListeOgSisteDagIfellesPeriodeHvisFinnes(behandling, dto, perioder);

            if ((behandling.getRelasjonsRolleType().equals(RelasjonsRolleType.FARA)
                || behandling.getRelasjonsRolleType().equals(RelasjonsRolleType.MEDMOR))
                && dto.getSisteDagIFellesPeriode() != null) {

                setUkerEtterFellesPeriode(dto, perioder);
            }
        }
    }

    private void setÅrsaksListeOgSisteDagIfellesPeriodeHvisFinnes(final Behandling behandling, final DokumentTypeDto dto, final List<UttakResultatPeriodeEntitet> perioder) {
        for (UttakResultatPeriodeEntitet periode : perioder) {
            if (periode.getPeriodeResultatType().equals(PeriodeResultatType.AVSLÅTT)) {
                setAvslagsårsakOgLovhjemmel(behandling, dto, periode);
            }
            setSisteDagIFellesPeriode(dto, periode);
        }
    }

    private void setSisteDagIFellesPeriode(DokumentTypeDto dto, UttakResultatPeriodeEntitet periode) {
        periode.getAktiviteter().stream().filter(aktivitet -> StønadskontoType.FELLESPERIODE.equals(aktivitet.getTrekkonto()))
            .forEach(aktivitet -> {
                if (dto.getSisteDagIFellesPeriode() == null || dto.getSisteDagIFellesPeriode().isBefore(aktivitet.getTom())) {
                    dto.setSisteDagIFellesPeriode(aktivitet.getTom());
                }
            });
    }

    private void setAvslagsårsakOgLovhjemmel(final Behandling behandling, final DokumentTypeDto dto, final UttakResultatPeriodeEntitet periode) {
        Optional<String> avslagsÅrsak = Optional.ofNullable(periode.getPeriodeResultatÅrsak().getKode());
        avslagsÅrsak.ifPresent(årsak -> {
            dto.getDokumentBehandlingsresultatDto().leggTilAvslagsårsak(årsak);
            String lovhjemler = periode.getPeriodeResultatÅrsak().getLovReferanse(behandling.getFagsakYtelseType()).orElse("");
            dto.getDokumentBehandlingsresultatDto().leggTilLovhjemmelForAvslag(lovhjemler);
        });
    }

    private void setUkerEtterFellesPeriode(final DokumentTypeDto dto, final List<UttakResultatPeriodeEntitet> perioder) {
        perioder.stream().filter(periode -> IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER.equals(periode.getPeriodeResultatÅrsak()))
            .forEach(p -> {
                int ukerEtterFellesPeriode = p.getAktiviteter().stream()
                    .filter(a -> a.getFom().equals(dto.getSisteDagIFellesPeriode().plusDays(1)))
                    .mapToInt(a -> a.getTrekkdager() / 5).sum();
                dto.setUkerEtterFellesPeriode(ukerEtterFellesPeriode);
            });
    }

    private String finnVilkårKode(final Behandling behandling, Avslagsårsak avslagsårsak) {
        List<VilkårType> vilkårTyper = vilkårKodeverkRepository.finnVilkårTypeListe(avslagsårsak.getKode());
        List<Vilkår> vilkårene = behandling.getBehandlingsresultat().getVilkårResultat().getVilkårene();
        Vilkår vilkår = vilkårene.stream()
            .filter(v -> vilkårTyper.contains(v.getVilkårType()))
            .findFirst().orElseThrow(() -> new IllegalStateException("Fant ingen vilkår"));

        return vilkår.getVilkårType().getKode();
    }

    private Optional<LocalDate> finnDødsdato(final Behandling behandling, final Set<String> avslagsårsakListe) {
        Optional<LocalDate> dødsdato = Optional.empty();
        if (avslagsårsakListe.contains(IkkeOppfyltÅrsak.SØKER_ER_DØD.getKode())) {
            dødsdato = personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling)
                .map(PersonopplysningerAggregat::getSøker)
                .map(Personopplysning::getDødsdato);
        }
        if (avslagsårsakListe.contains(IkkeOppfyltÅrsak.BARNET_ER_DØD.getKode())) {
            dødsdato = familiehendelseTjeneste.finnBarnSøktStønadFor(behandling).stream()
                .filter(personopplysning -> personopplysning.getDødsdato() != null)
                .map(Personopplysning::getDødsdato)
                .findFirst();
        }
        return dødsdato;
    }
}
