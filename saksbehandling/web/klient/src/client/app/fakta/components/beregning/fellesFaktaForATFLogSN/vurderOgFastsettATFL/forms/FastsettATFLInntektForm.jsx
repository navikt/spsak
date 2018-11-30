import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { FormattedMessage } from 'react-intl';
import moment from 'moment';
import { Element, Normaltekst } from 'nav-frontend-typografi';
import { required } from 'utils/validation/validators';
import createVisningsnavnForAktivitet from 'utils/arbeidsforholdUtil';
import VerticalSpacer from 'sharedComponents/VerticalSpacer';
import { InputField } from 'form/Fields';
import { getFaktaOmBeregning } from 'behandling/behandlingSelectors';
import Table from 'sharedComponents/Table';
import TableRow from 'sharedComponents/TableRow';
import TableColumn from 'sharedComponents/TableColumn';
import { DDMMYYYY_DATE_FORMAT } from 'utils/formats';
import aktivitetStatus from 'kodeverk/aktivitetStatus';
import { formatCurrencyNoKr, parseCurrencyInput, removeSpacesFromNumber } from 'utils/currencyUtils';
import faktaOmBeregningTilfelle, { erATFLSpesialtilfelle, harKunATFLISammeOrgUtenBestebergning } from 'kodeverk/faktaOmBeregningTilfelle';

import styles from './fastsettATFLInntektForm.less';

const inntektInputFieldName = 'fastsattInntekt';

export const createInputfieldKeyAT = (arbeidsforhold) => {
  const key = `${inntektInputFieldName}_${arbeidsforhold.arbeidsgiverNavn}_${arbeidsforhold.startdato}_${arbeidsforhold.arbeidsforholdId}`;
  return key;
};

export const createInputfieldKeyFL = () => `${inntektInputFieldName}_FL`;

const skalFastsetteATForholdInntekt = forhold => forhold !== undefined
  && (forhold.inntektPrMnd === null || forhold.inntektPrMnd === undefined);

const createFLTableRow = (frilansAndel, readOnly, isAksjonspunktClosed) => (
  <TableRow key="FLRow">
    <TableColumn>
      <Normaltekst>{frilansAndel.inntektskategori.navn}</Normaltekst>
    </TableColumn>
    <TableColumn>
      {frilansAndel.arbeidsforhold && frilansAndel.arbeidsforhold.startdato
      && (
      <Normaltekst>
        <FormattedMessage
          id="BeregningInfoPanel.VurderOgFastsettATFL.Periode"
          values={{
            fom: moment(frilansAndel.arbeidsforhold.startdato).format(DDMMYYYY_DATE_FORMAT),
            tom: frilansAndel && frilansAndel.arbeidsforhold && frilansAndel.arbeidsforhold.opphoersdato
              ? moment(frilansAndel.arbeidsforhold.opphoersdato).format(DDMMYYYY_DATE_FORMAT) : '',
          }}
        />
      </Normaltekst>
      )
      }
    </TableColumn>
    <TableColumn>
      <div className={readOnly ? styles.adjustedFieldInput : styles.rightAlignInput}>
        <InputField
          name={createInputfieldKeyFL()}
          bredde="S"
          validate={[required]}
          parse={parseCurrencyInput}
          isEdited={isAksjonspunktClosed}
          readOnly={readOnly}
        />
      </div>
    </TableColumn>
    <TableColumn>
      <Normaltekst>{frilansAndel.inntektskategori ? frilansAndel.inntektskategori.navn : ''}</Normaltekst>
    </TableColumn>
  </TableRow>
);

const createTableRows = (frilansAndel, aktiviteter, readOnly, isAksjonspunktClosed, skalFastsetteFL, skalFastsetteAT) => {
  const rows = [];
  if (frilansAndel && skalFastsetteFL) {
    rows.push(createFLTableRow(frilansAndel, readOnly, isAksjonspunktClosed));
  }
  if (aktiviteter && skalFastsetteAT) {
    aktiviteter.forEach((aktivitet) => {
      rows.push(
        <TableRow key={createInputfieldKeyAT(aktivitet.arbeidsforhold)}>
          <TableColumn>
            <Normaltekst>{createVisningsnavnForAktivitet(aktivitet.arbeidsforhold)}</Normaltekst>
          </TableColumn>
          <TableColumn>
            {aktivitet.arbeidsforhold && aktivitet.arbeidsforhold.startdato
          && (
          <Normaltekst>
            <FormattedMessage
              id="BeregningInfoPanel.VurderOgFastsettATFL.Periode"
              values={{
                fom: moment(aktivitet.arbeidsforhold.startdato).format(DDMMYYYY_DATE_FORMAT),
                tom: aktivitet.arbeidsforhold.opphoersdato ? moment(aktivitet.arbeidsforhold.opphoersdato).format(DDMMYYYY_DATE_FORMAT) : '',
              }}
            />
          </Normaltekst>
          )
          }
          </TableColumn>
          {skalFastsetteATForholdInntekt(aktivitet)
        && (
        <TableColumn className={readOnly ? styles.adjustedFieldInput : styles.rightAlignInput}>
          <InputField
            name={createInputfieldKeyAT(aktivitet.arbeidsforhold)}
            bredde="S"
            validate={[required]}
            parse={parseCurrencyInput}
            isEdited={isAksjonspunktClosed}
            readOnly={readOnly}
          />
        </TableColumn>
        )
        }
          {!skalFastsetteATForholdInntekt(aktivitet)
        && (
        <TableColumn>
          <div className={styles.rightAlignText}>
            <Normaltekst>{formatCurrencyNoKr(aktivitet.inntektPrMnd)}</Normaltekst>
          </div>
        </TableColumn>
        )
        }
          <TableColumn>
            <Normaltekst>{aktivitet.inntektskategori ? aktivitet.inntektskategori.navn : ''}</Normaltekst>
          </TableColumn>
        </TableRow>,
      );
    });
  }
  return rows;
};


const findInstruksjonForBruker = (tilfellerSomSkalFastsettes, manglerInntektsmelding, skalFastsetteFL, skalFastsetteAT) => {
  if (erATFLSpesialtilfelle(tilfellerSomSkalFastsettes)) {
    return 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettATFLAlleOppdrag';
  }
  if (tilfellerSomSkalFastsettes.includes(faktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON)) {
    return manglerInntektsmelding === true
      ? 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettATFLAlleOppdrag'
      : 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettFrilansAlleOppdrag';
  }
  if (skalFastsetteFL) {
    return tilfellerSomSkalFastsettes.length === 1
      ? 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettFrilans'
      : 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettFrilansAlleOppdrag';
  }
  if (skalFastsetteAT) {
    return 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettArbeidsinntekt';
  }
  return 'BeregningInfoPanel.VurderOgFastsettATFL.FastsettATFLAlleOppdrag';
};

const lagInstruksjonForBrukerUtenVurdering = (manglerInntektsmelding) => {
  if (manglerInntektsmelding) {
    return (
      <div>
        <Normaltekst>
          <FormattedMessage id="BeregningInfoPanel.VurderOgFastsettATFL.ATFLSammeOrgUtenIM" />
        </Normaltekst>
        <Normaltekst>
          <FormattedMessage id="BeregningInfoPanel.VurderOgFastsettATFL.FastsettATFLAlleOppdrag" />
        </Normaltekst>
      </div>
    );
  }
  return (
    <div>
      <Normaltekst>
        <FormattedMessage id="BeregningInfoPanel.VurderOgFastsettATFL.ATFLSammeOrg" />
      </Normaltekst>
      <Normaltekst>
        <FormattedMessage id="BeregningInfoPanel.VurderOgFastsettATFL.FastsettFrilansAlleOppdrag" />
      </Normaltekst>
    </div>
  );
};


/**
 * FastsettATFLInntektForm
 *
 * Presentasjonskomponent. Inneholder inntektstabellen som brukes til å fastsette innteker for frilansere og arbeidstakere.
 * Kan vises av komponentene LonnsendringForm, NyoppstartetFLForm eller
 * direkte av VurderOgFastsettATFL dersom ingen forhåndsvurdering er nødvendig.
 */
const FastsettATFLInntektForm = ({
  readOnly,
  isAksjonspunktClosed,
  tilfellerSomSkalFastsettes,
  arbeidsforholdSomSkalFastsettes,
  frilansAndel,
  manglerInntektsmelding,
  tabellVisesUtenVurdering,
  skalFastsetteFL,
  skalFastsetteAT,
}) => {
  const headerTextCodes = [
    'BeregningInfoPanel.FastsettInntektTabell.Aktivitet',
    'BeregningInfoPanel.FastsettInntektTabell.Arbeidsperiode',
    'BeregningInfoPanel.FastsettInntektTabell.InntektPrMnd',
    'BeregningInfoPanel.FastsettInntektTabell.Inntektskategori',
  ];
  return (
    <div className={!tabellVisesUtenVurdering ? styles.paddingRundtTabell : undefined}>
      {tabellVisesUtenVurdering
      && (
      <div>
        {lagInstruksjonForBrukerUtenVurdering(manglerInntektsmelding)}
      </div>
      )
      }
      {!tabellVisesUtenVurdering
      && (
      <Element>
        <FormattedMessage id={findInstruksjonForBruker(tilfellerSomSkalFastsettes, manglerInntektsmelding, skalFastsetteFL, skalFastsetteAT)} />
      </Element>
      )
      }
      <VerticalSpacer space={2} />
      <Table headerTextCodes={headerTextCodes} noHover classNameTable={styles.inntektTable}>
        {createTableRows(frilansAndel, arbeidsforholdSomSkalFastsettes, readOnly, isAksjonspunktClosed, skalFastsetteFL, skalFastsetteAT)}
      </Table>
    </div>
  );
};

FastsettATFLInntektForm.propTypes = {
  readOnly: PropTypes.bool.isRequired,
  isAksjonspunktClosed: PropTypes.bool.isRequired,
  tilfellerSomSkalFastsettes: PropTypes.arrayOf(PropTypes.string).isRequired,
  tabellVisesUtenVurdering: PropTypes.bool.isRequired,
  manglerInntektsmelding: PropTypes.bool,
  frilansAndel: PropTypes.shape(),
  arbeidsforholdSomSkalFastsettes: PropTypes.arrayOf(PropTypes.shape()),
  skalFastsetteFL: PropTypes.bool,
  skalFastsetteAT: PropTypes.bool,

};

FastsettATFLInntektForm.defaultProps = {
  arbeidsforholdSomSkalFastsettes: PropTypes.arrayOf(PropTypes.shape()),
  manglerInntektsmelding: undefined,
  frilansAndel: undefined,
  skalFastsetteAT: true,
  skalFastsetteFL: true,
};

const slaSammenATListerSomSkalVurderes = (faktaOmBeregning) => {
  const andelsNrLagtIListen = [];
  const listeMedArbeidsforholdSomSkalFastsettes = [];
  if (faktaOmBeregning.arbeidsforholdMedLønnsendringUtenIM) {
    faktaOmBeregning.arbeidsforholdMedLønnsendringUtenIM.forEach((forhold) => {
      if (!andelsNrLagtIListen.includes(forhold.andelsnr)) {
        andelsNrLagtIListen.push(forhold.andelsnr);
        listeMedArbeidsforholdSomSkalFastsettes.push(forhold);
      }
    });
  }
  if (faktaOmBeregning.atogFLISammeOrganisasjonListe) {
    faktaOmBeregning.atogFLISammeOrganisasjonListe.forEach((forhold) => {
      if (!andelsNrLagtIListen.includes(forhold.andelsnr)) {
        andelsNrLagtIListen.push(forhold.andelsnr);
        listeMedArbeidsforholdSomSkalFastsettes.push(forhold);
      }
    });
  }
  return listeMedArbeidsforholdSomSkalFastsettes.length === 0 ? null : listeMedArbeidsforholdSomSkalFastsettes;
};

const harFrilansinntektBlittFastsattTidligere = frilansAndel => frilansAndel
&& (frilansAndel.erNyoppstartetEllerSammeOrganisasjon === true || frilansAndel.beregnetPrAar);

const finnKorrektBGAndelFraFaktaOmBeregningAndel = (faktaOmBeregningAndel, beregningsgrunnlag) => {
  const forstePeriode = beregningsgrunnlag.beregningsgrunnlagPeriode
    ? beregningsgrunnlag.beregningsgrunnlagPeriode[0] : undefined;
  return forstePeriode
    ? forstePeriode.beregningsgrunnlagPrStatusOgAndel.find(andel => andel.andelsnr === faktaOmBeregningAndel.andelsnr) : undefined;
};

FastsettATFLInntektForm.buildInitialValues = (beregningsgrunnlag) => {
  const initialValues = {};
  const faktaOmBeregning = beregningsgrunnlag ? beregningsgrunnlag.faktaOmBeregning : undefined;
  if (!beregningsgrunnlag || !faktaOmBeregning || !beregningsgrunnlag.beregningsgrunnlagPeriode
    || beregningsgrunnlag.beregningsgrunnlagPeriode.length < 1) {
    return initialValues;
  }

  const frilansAndel = beregningsgrunnlag.beregningsgrunnlagPeriode[0].beregningsgrunnlagPrStatusOgAndel
    .find(andel => andel.aktivitetStatus.kode === aktivitetStatus.FRILANSER);

  if (faktaOmBeregning.atogFLISammeOrganisasjonListe !== null) {
    faktaOmBeregning.atogFLISammeOrganisasjonListe.forEach((aktivitet) => {
      const korrektAndel = finnKorrektBGAndelFraFaktaOmBeregningAndel(aktivitet, beregningsgrunnlag);
      if (korrektAndel && korrektAndel.beregnetPrAar !== null && korrektAndel.beregnetPrAar !== undefined) {
        const key = createInputfieldKeyAT(aktivitet.arbeidsforhold);
        initialValues[key] = formatCurrencyNoKr(korrektAndel.beregnetPrAar / 12);
      }
    });
  }
  if (faktaOmBeregning.arbeidsforholdMedLønnsendringUtenIM !== null) {
    faktaOmBeregning.arbeidsforholdMedLønnsendringUtenIM.forEach((aktivitet) => {
      const korrektAndel = finnKorrektBGAndelFraFaktaOmBeregningAndel(aktivitet, beregningsgrunnlag);
      if (korrektAndel !== undefined && korrektAndel.lonnsendringIBeregningsperioden === true) {
        const key = createInputfieldKeyAT(aktivitet.arbeidsforhold);
        initialValues[key] = formatCurrencyNoKr(korrektAndel.beregnetPrAar / 12);
      }
    });
  }

  if (harFrilansinntektBlittFastsattTidligere(frilansAndel)) {
    const key = createInputfieldKeyFL();
    initialValues[key] = formatCurrencyNoKr(frilansAndel.beregnetPrAar / 12);
  }
  return initialValues;
};

const transformValuesFL = (values) => {
  const key = createInputfieldKeyFL();
  return removeSpacesFromNumber(values[key]);
};

const transformValuesAT = (values, faktaOmBeregning) => {
  const arbeidsforholdSomSkalSubmittes = slaSammenATListerSomSkalVurderes(faktaOmBeregning);
  const listeMedFastsatteMaanedsinntekter = [];
  arbeidsforholdSomSkalSubmittes.forEach((aktivitet) => {
    if (!aktivitet.inntektPrMnd) {
      const inputField = createInputfieldKeyAT(aktivitet.arbeidsforhold);
      const inntektUtenFormat = values[inputField];
      listeMedFastsatteMaanedsinntekter.push({
        andelsnr: aktivitet.andelsnr,
        arbeidsinntekt: inntektUtenFormat ? removeSpacesFromNumber(inntektUtenFormat) : undefined,
      });
    }
  });
  return listeMedFastsatteMaanedsinntekter;
};

FastsettATFLInntektForm.eraseValuesFL = () => ({
  fastsettMaanedsinntektFL: null,
});

FastsettATFLInntektForm.eraseValuesAT = () => ({
  fastsettMaanedsinntektFL: null,
});

FastsettATFLInntektForm.transformValues = (values, faktaOmBeregning, aktueltTilfelle) => {
  if (aktueltTilfelle === faktaOmBeregningTilfelle.FASTSETT_MAANEDSINNTEKT_FL) {
    return {
      fastsettMaanedsinntektFL: { maanedsinntekt: transformValuesFL(values) },
    };
  }
  if (aktueltTilfelle === faktaOmBeregningTilfelle.FASTSETT_MAANEDSLONN_VED_LONNSENDRING) {
    return {
      fastsatteLonnsendringer: { vurderLønnsendringAndelListe: transformValuesAT(values, faktaOmBeregning) },
    };
  }
  if (aktueltTilfelle === faktaOmBeregningTilfelle.VURDER_AT_OG_FL_I_SAMME_ORGANISASJON) {
    const andelsliste = transformValuesAT(values, faktaOmBeregning);
    andelsliste.push({
      andelsnr: faktaOmBeregning.frilansAndel.andelsnr,
      arbeidsinntekt: transformValuesFL(values),
    });
    return {
      vurderATogFLiSammeOrganisasjon: { vurderATogFLiSammeOrganisasjonAndelListe: andelsliste },
    };
  }
  return {};
};

const mapStateToProps = (state, ownProps) => {
  const faktaOmBeregning = getFaktaOmBeregning(state);
  const { tilfellerSomSkalFastsettes } = ownProps;
  if (!faktaOmBeregning || tilfellerSomSkalFastsettes === undefined || tilfellerSomSkalFastsettes.length < 1) {
    return {};
  }
  const arbeidsforholdSomSkalFastsettes = slaSammenATListerSomSkalVurderes(faktaOmBeregning);
  const tabellVisesUtenVurdering = harKunATFLISammeOrgUtenBestebergning(tilfellerSomSkalFastsettes);
  return {
    tabellVisesUtenVurdering,
    arbeidsforholdSomSkalFastsettes,
    frilansAndel: faktaOmBeregning.frilansAndel,
  };
};

export default connect(mapStateToProps)(FastsettATFLInntektForm);
