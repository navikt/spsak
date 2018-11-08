package no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.engangsstønad.v1;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Innsendingsvalg;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOpphold;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittLandOppholdEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPartBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadAnnenPartType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedleggEntitet;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentFeil;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.MottattDokumentOversetter;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.NamespaceRef;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.AktoerId;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.GrunnlagForAnsvarsovertakelse;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.KanIkkeOppgiFar;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.KanIkkeOppgiMor;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Kodeverdi;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmFar;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmMor;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Utenlandsopphold;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Vedlegg;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;

@NamespaceRef(SoeknadsskjemaEngangsstoenadContants.NAMESPACE)
@ApplicationScoped
public class MottattDokumentOversetterEngangsstønad implements MottattDokumentOversetter<MottattDokumentWrapperEngangsstønad> {

    private PersonopplysningRepository personopplysningRepository;
    private MedlemskapRepository medlemskapRepository;
    private TpsAdapter tpsAdapter;
    private KodeverkRepository kodeverkRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private SøknadRepository søknadRepository;

    public MottattDokumentOversetterEngangsstønad() {
        // for CDI proxy
    }

    @Inject
    public MottattDokumentOversetterEngangsstønad(TpsAdapter tpsAdapter, BehandlingRepositoryProvider repositoryProvider) {
        this.tpsAdapter = tpsAdapter;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    }

    @Override
    public void trekkUtDataOgPersister(MottattDokumentWrapperEngangsstønad wrapper,
                                       MottattDokument mottattDokument, Behandling behandling, Optional<LocalDate> gjelderFra) {
        LocalDate mottattDato = mottattDokument.getMottattDato();
        boolean elektroniskSøknad = mottattDokument.getElektroniskRegistrert();

        final FamilieHendelseBuilder hendelseBuilder = familieHendelseRepository.opprettBuilderFor(behandling);

        final SøknadEntitet.Builder builder = byggSøknad(wrapper, behandling, elektroniskSøknad, mottattDato);

        if (søknadErFor(FoedselEllerAdopsjon.FOEDSEL, wrapper)) {
            Objects.requireNonNull(wrapper.getOpplysningerOmBarn(), "OpplysningerOmBarn må være oppgitt");
            if (wrapper.getOpplysningerOmBarn().getFoedselsdato() != null && !wrapper.getOpplysningerOmBarn().getFoedselsdato().isEmpty()) {
                byggFødselsrelaterteFelter(wrapper, hendelseBuilder);
            } else {
                byggTerminrelaterteFelter(wrapper, hendelseBuilder);
            }
        } else if (søknadErFor(FoedselEllerAdopsjon.ADOPSJON, wrapper)) {
            byggAdopsjonsrelaterteFelter(wrapper, hendelseBuilder, gjettetRolle(behandling));
        }
        familieHendelseRepository.lagre(behandling, hendelseBuilder);
        builder.medErEndringssøknad(false);
        final Søknad søknad = builder
            .medFamilieHendelse(familieHendelseRepository.hentAggregat(behandling).getSøknadVersjon())
            .medRelasjonsRolleType(gjettetRolle(behandling))
            .build();
        søknadRepository.lagreOgFlush(behandling, søknad);
    }

    private SøknadEntitet.Builder byggSøknad(MottattDokumentWrapperEngangsstønad skjema,
                                             Behandling behandling, Boolean elektroniskSøknad, LocalDate forsendelseMottatt) {
        SøknadEntitet.Builder søknadBuilder = new SøknadEntitet.Builder();
        søknadBuilder.medElektroniskRegistrert(elektroniskSøknad)
            // I første omgang vil disse to settes til det samme, men det er mulig de slås sammen senere:
            .medSøknadsdato(forsendelseMottatt)
            .medMottattDato(forsendelseMottatt)
            .medBegrunnelseForSenInnsending(skjema.getOpplysningerOmBarn().getBegrunnelse())
            .medTilleggsopplysninger(skjema.getTilleggsopplysninger());

        if (skjema.getRettigheter() != null && skjema.getRettigheter().getGrunnlagForAnsvarsovertakelse() != null && RelasjonsRolleType.FARA.equals(gjettetRolle(behandling))) {
            søknadBuilder.medFarSøkerType(tolkFarSøkerType(skjema.getRettigheter().getGrunnlagForAnsvarsovertakelse()));
        }
        for (Vedlegg vedlegg : skjema.getVedleggListe()) {
            byggSøknadVedlegg(søknadBuilder, vedlegg);
        }
        if (skjema.getTilknytningNorge() != null) {
            byggSøknadTilknytningNorge(skjema, søknadBuilder, behandling, forsendelseMottatt);
        }
        if (skalByggeSøknadAnnenPartForFar(skjema)) {
            byggSøknadAnnenPartForFar(skjema, søknadBuilder, behandling);
        }
        if (skalByggeSøknadAnnenPartForMor(skjema)) {
            byggSøknadAnnenPartForMor(skjema, søknadBuilder, behandling);
        }
        return søknadBuilder;
    }

    private boolean skalByggeSøknadAnnenPartForFar(MottattDokumentWrapperEngangsstønad skjema) {
        if (skjema.getOpplysningerOmFar() == null) {
            return false;
        }
        Optional<PersonIdent> personident = finnPersonidentForBruker(skjema);
        return personident.map(s -> !s.getIdent().equals(skjema.getOpplysningerOmFar().getPersonidentifikator())).orElse(true);
    }

    private boolean skalByggeSøknadAnnenPartForMor(MottattDokumentWrapperEngangsstønad skjema) {
        if (skjema.getOpplysningerOmMor() == null) {
            return false;
        }
        Optional<PersonIdent> personident = finnPersonidentForBruker(skjema);
        return personident.map(s -> !s.getIdent().equals(skjema.getOpplysningerOmMor().getPersonidentifikator())).orElse(true);
    }

    private Optional<PersonIdent> finnPersonidentForBruker(MottattDokumentWrapperEngangsstønad skjema) {
        if (skjema.getBruker() instanceof AktoerId) {
            AktoerId aktørId = (AktoerId) skjema.getBruker();
            return tpsAdapter.hentIdentForAktørId(new AktørId(aktørId.getAktoerId()));
        } else if (skjema.getBruker() instanceof Bruker) {
            Bruker bruker = (Bruker) skjema.getBruker();
            return Optional.of(new PersonIdent(bruker.getPersonidentifikator()));
        } else {
            return Optional.empty();
        }
    }

    private FarSøkerType tolkFarSøkerType(GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse) {
        switch (grunnlagForAnsvarsovertakelse) {
            case ADOPTERER_ALENE:
                return FarSøkerType.ADOPTERER_ALENE;
            case OVERTATT_PA_GRUNN_AV_DOD:
                return FarSøkerType.ANDRE_FORELDER_DØD;
            case OVERTATT_OMSORG_INNEN_53_UKER_ADOPSJON:
                return FarSøkerType.OVERTATT_OMSORG;
            case OVERTATT_OMSORG_INNEN_53_UKER_FODSEL:
                return FarSøkerType.OVERTATT_OMSORG_F;
            default:
                return FarSøkerType.UDEFINERT;
        }
    }

    private boolean søknadErFor(FoedselEllerAdopsjon foedselEllerAdopsjon, MottattDokumentWrapperEngangsstønad skjema) {
        Objects.requireNonNull(skjema.getSoknadsvalg(), "Soeknadsvalg må være oppgitt");
        Objects.requireNonNull(skjema.getSoknadsvalg().getFoedselEllerAdopsjon(), "FoedselEllerAdopsjon må være oppgitt");
        return skjema.getSoknadsvalg().getFoedselEllerAdopsjon().equals(foedselEllerAdopsjon);
    }

    private void byggFødselsrelaterteFelter(MottattDokumentWrapperEngangsstønad skjema, FamilieHendelseBuilder hendelseBuilder) {
        if (skjema.getOpplysningerOmBarn().getFoedselsdato() == null || skjema.getOpplysningerOmBarn()
            .getFoedselsdato().size() != 1) {
            throw new IllegalArgumentException("Utviklerfeil: Ved fødsel skal det være eksakt én fødselsdato");
        }

        LocalDate fødselsdato = DateUtil.convertToLocalDate(skjema.getOpplysningerOmBarn().getFoedselsdato().get(0));
        int antallBarn = skjema.getOpplysningerOmBarn().getAntallBarn();
        List<LocalDate> fødselsdatoene = new ArrayList<>();
        for (int i = 1; i <= antallBarn; i++) {
            fødselsdatoene.add(fødselsdato);
        }

        hendelseBuilder.medAntallBarn(antallBarn);
        for (LocalDate localDate : fødselsdatoene) {
            hendelseBuilder.leggTilBarn(localDate);
        }
    }

    private void byggTerminrelaterteFelter(MottattDokumentWrapperEngangsstønad skjema, FamilieHendelseBuilder hendelseBuilder) {
        Objects.requireNonNull(skjema.getOpplysningerOmBarn().getTermindato(), "Termindato må være oppgitt");
        Objects.requireNonNull(skjema.getSoknadsvalg().getStoenadstype(), "Stoenadstype må være oppgitt");

        hendelseBuilder.medAntallBarn(skjema.getOpplysningerOmBarn().getAntallBarn());
        hendelseBuilder.medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
            .medTermindato(DateUtil.convertToLocalDate(skjema.getOpplysningerOmBarn().getTermindato()))
            .medUtstedtDato(DateUtil.convertToLocalDate(skjema.getOpplysningerOmBarn().getTerminbekreftelsedato()))
            .medNavnPå(skjema.getOpplysningerOmBarn().getNavnPaaTerminbekreftelse()));
    }

    private void byggAdopsjonsrelaterteFelter(MottattDokumentWrapperEngangsstønad skjema, FamilieHendelseBuilder hendelseBuilder, RelasjonsRolleType rolle) {
        List<LocalDate> fødselsdatoene = Arrays.stream(skjema.getOpplysningerOmBarn().getFoedselsdato().toArray())
            .map(date -> DateUtil.convertToLocalDate((XMLGregorianCalendar) date))
            .collect(toList());

        hendelseBuilder.medAntallBarn(skjema.getOpplysningerOmBarn().getAntallBarn());
        final FamilieHendelseBuilder.AdopsjonBuilder adopsjon = hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(DateUtil.convertToLocalDate(skjema.getOpplysningerOmBarn().getOmsorgsovertakelsedato()));
        for (LocalDate localDate : fødselsdatoene) {
            hendelseBuilder.leggTilBarn(localDate);
        }
        if (skjema.getRettigheter() != null && skjema.getRettigheter().getGrunnlagForAnsvarsovertakelse() != null && RelasjonsRolleType.FARA.equals(rolle)) {
            final FarSøkerType farSøkerType = tolkFarSøkerType(skjema.getRettigheter().getGrunnlagForAnsvarsovertakelse());
            if (farSøkerType.equals(FarSøkerType.ADOPTERER_ALENE)) {
                adopsjon.medAdoptererAlene(true);
            } else if (!farSøkerType.equals(FarSøkerType.UDEFINERT)) {
                hendelseBuilder.erOmsorgovertagelse();
            }
        }
        hendelseBuilder.medAdopsjon(adopsjon);
    }

    private void byggSøknadVedlegg(SøknadEntitet.Builder søknadBuilder, Vedlegg vedlegg) {
        SøknadVedleggEntitet.Builder vedleggBuilder = new SøknadVedleggEntitet.Builder()
            .medErPåkrevdISøknadsdialog(vedlegg.isErPaakrevdISoeknadsdialog())
            .medInnsendingsvalg(tolkInnsendingsvalg(vedlegg.getInnsendingsvalg()))
            .medSkjemanummer(vedlegg.getSkjemanummer())
            .medTilleggsinfo(vedlegg.getTilleggsinfo());
        søknadBuilder.leggTilVedlegg(vedleggBuilder.build());
    }

    private Innsendingsvalg tolkInnsendingsvalg(no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Innsendingsvalg innsendingsvalg) {
        switch (innsendingsvalg) {
            case IKKE_VALGT:
                return Innsendingsvalg.IKKE_VALGT;
            case LASTET_OPP:
                return Innsendingsvalg.LASTET_OPP;
            case SEND_SENERE:
                return Innsendingsvalg.SEND_SENERE;
            case SENDES_IKKE:
                return Innsendingsvalg.SENDES_IKKE;
            case VEDLEGG_ALLEREDE_SENDT:
                return Innsendingsvalg.VEDLEGG_ALLEREDE_SENDT;
            case VEDLEGG_SENDES_AV_ANDRE:
                return Innsendingsvalg.VEDLEGG_SENDES_AV_ANDRE;
        }
        return null;
    }

    private void byggSøknadTilknytningNorge(MottattDokumentWrapperEngangsstønad skjema, SøknadEntitet.Builder søknadBuilder, Behandling behandling, LocalDate forsendelseMottatt) {
        OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder = new OppgittTilknytningEntitet.Builder()
            .medOppholdNå(skjema.getTilknytningNorge().isOppholdNorgeNaa()).medOppgittDato(forsendelseMottatt);

        settOppholdNorgePerioder(skjema, oppgittTilknytningBuilder);
        settTidligereOppholdUtland(skjema, oppgittTilknytningBuilder);
        settFremtidigOppholdUtland(skjema, oppgittTilknytningBuilder);
        medlemskapRepository.lagreOppgittTilkytning(behandling, oppgittTilknytningBuilder.build());
        final Optional<MedlemskapAggregat> medlemskapAggregat = medlemskapRepository.hentMedlemskap(behandling);
        final Optional<OppgittTilknytning> oppgittTilknytning = medlemskapAggregat.flatMap(MedlemskapAggregat::getOppgittTilknytning);
        oppgittTilknytning.ifPresent(søknadBuilder::medOppgittTilknytning);
    }

    private void settFremtidigOppholdUtland(MottattDokumentWrapperEngangsstønad skjema, OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder) {
        if (skjema.getTilknytningNorge().getFremtidigOppholdUtenlands() != null) {
            for (Utenlandsopphold utenlandsopphold : skjema.getTilknytningNorge().getFremtidigOppholdUtenlands().getUtenlandsopphold()) {
                // TODO (essv): Bør forvente at disse er fylt ut i skjemaet, slik at guard er unødvendig
                if (utenlandsopphold.getLand().getKode() == null || "".equals(utenlandsopphold.getLand().getKode())) {
                    continue;
                }
                oppgittTilknytningBuilder.leggTilOpphold(byggUtlandsopphold(utenlandsopphold, false));
            }
        }
    }

    private void settTidligereOppholdUtland(MottattDokumentWrapperEngangsstønad skjema, OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder) {
        if (skjema.getTilknytningNorge().getTidligereOppholdUtenlands() != null) {
            for (Utenlandsopphold utenlandsopphold : skjema.getTilknytningNorge().getTidligereOppholdUtenlands().getUtenlandsopphold()) {
                // TODO (essv): Bør forvente at disse er fylt ut i skjemaet, slik at guard er unødvendig
                if (utenlandsopphold.getLand().getKode() == null || "".equals(utenlandsopphold.getLand().getKode())) {
                    continue;
                }
                oppgittTilknytningBuilder.leggTilOpphold(byggUtlandsopphold(utenlandsopphold, true));
            }
        }
    }

    /**
     * Hvis angitt tidligere/fremtidig opphold så sett også periodetype. I søknadsskjema angis at opphold varighet er 12 mnd.
     */
    private void settOppholdNorgePerioder(MottattDokumentWrapperEngangsstønad skjema, OppgittTilknytningEntitet.Builder oppgittTilknytningBuilder) {
        Boolean tidligereOppholdNorge = skjema.getTilknytningNorge().isTidligereOppholdNorge();
        Boolean fremtidigOppholdNorge = skjema.getTilknytningNorge().isFremtidigOppholdNorge();
        if (Boolean.TRUE.equals(tidligereOppholdNorge)) {
            OppgittLandOpphold oppholdNorgeSistePeriode = new OppgittLandOppholdEntitet.Builder()
                .erTidligereOpphold(true)
                .medLand(Landkoder.NOR)
                .medPeriode(LocalDate.now(FPDateUtil.getOffset()).minusYears(1), LocalDate.now(FPDateUtil.getOffset()))
                .build();
            oppgittTilknytningBuilder.leggTilOpphold(oppholdNorgeSistePeriode);
        }
        if (Boolean.TRUE.equals(fremtidigOppholdNorge)) {
            OppgittLandOpphold oppholdNorgeNestePeriode = new OppgittLandOppholdEntitet.Builder()
                .erTidligereOpphold(false)
                .medLand(Landkoder.NOR)
                .medPeriode(LocalDate.now(FPDateUtil.getOffset()), LocalDate.now(FPDateUtil.getOffset()).plusYears(1))
                .build();
            oppgittTilknytningBuilder.leggTilOpphold(oppholdNorgeNestePeriode);
        }
    }

    private OppgittLandOpphold byggUtlandsopphold(Utenlandsopphold utenlandsopphold, boolean tidligereOpphold) {
        return new OppgittLandOppholdEntitet.Builder()
            .medLand(kodeverkRepository.finn(Landkoder.class, utenlandsopphold.getLand().getKode()))
            .medPeriode(
                DateUtil.convertToLocalDate(utenlandsopphold.getPeriode().getFom()),
                DateUtil.convertToLocalDate(utenlandsopphold.getPeriode().getTom())
            )
            .erTidligereOpphold(tidligereOpphold)
            .build();
    }

    private void byggSøknadAnnenPartForFar(MottattDokumentWrapperEngangsstønad skjema, SøknadEntitet.Builder søknadBuilder, Behandling behandling) {
        OpplysningerOmFar opplysningerOmFar = skjema.getOpplysningerOmFar();
        OppgittAnnenPartBuilder oppgittAnnenPartBuilder = new OppgittAnnenPartBuilder()
            .medType(SøknadAnnenPartType.FAR);

        // Fars aktørid
        Optional<AktørId> funnetAktørId = Optional.ofNullable(opplysningerOmFar.getPersonidentifikator())
            .flatMap(ident -> tpsAdapter.hentAktørIdForPersonIdent(new PersonIdent(ident)));
        funnetAktørId.ifPresent(oppgittAnnenPartBuilder::medAktørId);

        if (opplysningerOmFar.getKanIkkeOppgiFar() == null) {
            // Navn på oppgitt far
            Optional.ofNullable(skjema.getOpplysningerOmFar())
                .map(opp -> opp.getFornavn() + " " + opp.getEtternavn())
                .ifPresent(oppgittAnnenPartBuilder::medNavn);
        } else {
            // Informasjon om ikke-oppgitt far
            KanIkkeOppgiFar kanIkkeOppgiFar = opplysningerOmFar.getKanIkkeOppgiFar();
            oppgittAnnenPartBuilder.medUtenlandskFnr(kanIkkeOppgiFar.getUtenlandskfnrEllerForklaring())
                .medÅrsak(kanIkkeOppgiFar.getAarsak());

            Optional<String> funnetLandkode = Optional.ofNullable(kanIkkeOppgiFar.getUtenlandskfnrLand())
                .map(Kodeverdi::getKode);
            funnetLandkode.ifPresent(s -> oppgittAnnenPartBuilder
                .medUtenlandskFnrLand(kodeverkRepository.finn(Landkoder.class, s)));
        }

        personopplysningRepository.lagre(behandling, oppgittAnnenPartBuilder);
        final Optional<OppgittAnnenPart> annenPartFraSøknad = personopplysningRepository.hentPersonopplysninger(behandling).getOppgittAnnenPart();
        annenPartFraSøknad.ifPresent(søknadBuilder::medSøknadAnnenPart);
    }

    private void byggSøknadAnnenPartForMor(MottattDokumentWrapperEngangsstønad skjema, SøknadEntitet.Builder søknadBuilder, Behandling behandling) {
        OpplysningerOmMor opplysningerOmMor = skjema.getOpplysningerOmMor();
        OppgittAnnenPartBuilder oppgittAnnenPartBuilder = new OppgittAnnenPartBuilder()
            .medType(SøknadAnnenPartType.MOR);

        // Mors aktørid
        Optional<AktørId> funnetAktørId = Optional.ofNullable(opplysningerOmMor.getPersonidentifikator())
            .flatMap(ident -> tpsAdapter.hentAktørIdForPersonIdent(new PersonIdent(ident)));
        funnetAktørId.ifPresent(oppgittAnnenPartBuilder::medAktørId);

        if (opplysningerOmMor.getKanIkkeOppgiMor() == null) {
            // Navn på oppgitt mor
            Optional.ofNullable(skjema.getOpplysningerOmMor())
                .map(opp -> opp.getFornavn() + " " + opp.getEtternavn())
                .ifPresent(oppgittAnnenPartBuilder::medNavn);
        } else {
            // Informasjon om ikke-oppgitt mor
            KanIkkeOppgiMor kanIkkeOppgiMor = opplysningerOmMor.getKanIkkeOppgiMor();
            oppgittAnnenPartBuilder.medUtenlandskFnr(kanIkkeOppgiMor.getUtenlandskfnrEllerForklaring())
                .medÅrsak(kanIkkeOppgiMor.getAarsak())
                .medBegrunnelse(kanIkkeOppgiMor.getBegrunnelse());

            Optional<String> funnetLandkode = Optional.ofNullable(kanIkkeOppgiMor.getUtenlandskfnrLand())
                .map(Kodeverdi::getKode);
            funnetLandkode.ifPresent(s -> oppgittAnnenPartBuilder
                .medUtenlandskFnrLand(kodeverkRepository.finn(Landkoder.class, s)));
        }
        personopplysningRepository.lagre(behandling, oppgittAnnenPartBuilder);
        final Optional<OppgittAnnenPart> annenPartFraSøknad = personopplysningRepository.hentPersonopplysninger(behandling).getOppgittAnnenPart();
        annenPartFraSøknad.ifPresent(søknadBuilder::medSøknadAnnenPart);
    }

    // I {@link MottattDokumentType.ENGANGSSTOENAD} (gammelt format) angis ikke RelasjonsRolleType, så den må "gjettes"
    private RelasjonsRolleType gjettetRolle(Behandling behandling) {
        Optional<PersonIdent> personIdent = tpsAdapter.hentIdentForAktørId(behandling.getAktørId());
        if (personIdent.isPresent()) {
            return NavBrukerKjønn.MANN.equals(tpsAdapter.hentKjerneinformasjon(personIdent.get(), behandling.getAktørId()).getKjønn()) ? RelasjonsRolleType.FARA : RelasjonsRolleType.MORA;
        }
        throw MottattDokumentFeil.FACTORY.dokumentManglerRelasjonsRolleType(behandling.getId()).toException();
    }
}
