package no.nav.foreldrepenger.domene.person.impl;

import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TpsFamilieTjenesteImpl implements TpsFamilieTjeneste {

    private TpsTjeneste tpsTjeneste;
    private SøknadRepository søknadRepository;
    private Period etterkontrollTidsromFørSøknadsdato;
    private Period etterkontrollTidsromEtterTermindato;

    TpsFamilieTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public TpsFamilieTjenesteImpl(TpsTjeneste tpsTjeneste,
                                  BehandlingRepositoryProvider repositoryProvider,
                                  @KonfigVerdi("etterkontroll.førsøknad.periode") Instance<Period> etterkontrollTidsromFørSøknadsdato,
                                  @KonfigVerdi("etterkontroll.ettertermin.periode") Instance<Period> etterkontrollTidsromEtterTermindato) {
        this.tpsTjeneste = tpsTjeneste;
        this.etterkontrollTidsromEtterTermindato = etterkontrollTidsromEtterTermindato.get();
        this.etterkontrollTidsromFørSøknadsdato = etterkontrollTidsromFørSøknadsdato.get();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
    }

    @Override
    public List<FødtBarnInfo> getFødslerRelatertTilBehandling(Behandling behandling, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        final Søknad søknad = finnOrginalSøknad(behandling);
        DatoIntervallEntitet forventetFødselIntervall = TpsFødselUtil.forventetFødselIntervall(familieHendelseGrunnlag,
            etterkontrollTidsromFørSøknadsdato, etterkontrollTidsromEtterTermindato, søknad);

        List<FødtBarnInfo> barneListe = tpsTjeneste.hentFødteBarn(behandling.getAktørId());
        return barneListe.stream().filter(p -> forventetFødselIntervall.inkluderer(p.getFødselsdato())).collect(Collectors.toList());
    }

    private Søknad finnOrginalSøknad(Behandling behandling) {
        return søknadRepository.hentFørstegangsSøknad(behandling);
    }
}
