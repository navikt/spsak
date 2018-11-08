package no.nav.foreldrepenger.domene.vedtakinnsyn.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.vedtakinnsyn.VedtakInnsynTjeneste;
import no.nav.foreldrepenger.domene.vedtakinnsyn.VedtakXMLTilHTMLTransformator;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;

@ApplicationScoped
public class VedtakInnsynTjenesteImpl implements VedtakInnsynTjeneste {

    private LagretVedtakRepository lagretVedtakRepository;

    public VedtakInnsynTjenesteImpl() {
        //CDI
    }

    @Inject
    public VedtakInnsynTjenesteImpl(LagretVedtakRepository lagretVedtakRepository) {
        this.lagretVedtakRepository = lagretVedtakRepository;
    }

    @Override
    public String hentVedtaksdokument(Long behadnlingId) {
        return VedtakXMLTilHTMLTransformator.transformer(lagretVedtakRepository.hentLagretVedtakForBehandling(behadnlingId).getXmlClob(), behadnlingId);
    }

}
