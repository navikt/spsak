import React from 'react';
import { connect } from 'react-redux';
import { getFormValues } from 'redux-form';
import PropTypes from 'prop-types';
import { FormattedMessage, injectIntl, intlShape } from 'react-intl';
import { Undertekst } from 'nav-frontend-typografi';
import { Row, Column } from 'nav-frontend-grid';
import AlertStripe from 'nav-frontend-alertstriper';

import VerticalSpacer from 'sharedComponents/VerticalSpacer';
import PeriodFieldArray from 'sharedComponents/PeriodFieldArray';
import { FlexContainer, FlexColumn, FlexRow } from 'sharedComponents/flexGrid';
import { maxValue, hasValidDecimal } from 'utils/validation/validators';
import {
  DatepickerField, SelectField, InputField, CheckboxField, DecimalField,
} from 'form/Fields';
import kodeverkPropType from 'kodeverk/kodeverkPropType';
import moment from 'moment/moment';
import { gyldigeUttakperioder } from './RenderPermisjonPeriodeFieldArray';

import styles from './renderGraderingPeriodeFieldArray.less';

const defaultGraderingPeriode = {
  periodeFom: '',
  periodeTom: '',
  periodeForGradering: '',
  prosentandelArbeid: '',
  orgNr: '',
  erArbeidstaker: '',
  skalGraderes: false,
  flerbarnsdager: '',
  harSamtidigUttak: '',
  samtidigUttaksprosent: '',
};

const maxValue200 = maxValue(200);
const maxValue100 = maxValue(100);

const mapKvoter = typer => typer
  .filter(({ kode }) => gyldigeUttakperioder.includes(kode))
  .map(({ kode, navn }) => <option value={kode} key={kode}>{navn}</option>);

/**
 *  RenderGraderingPeriodeFieldArray
 *
 * Presentasjonskomponent: Viser inputfelter for dato for bestemmelse av graderingperiode.
 * Komponenten må rendres som komponenten til et FieldArray.
 */
export const RenderGraderingPeriodeFieldArray = ({
  fields,
  meta,
  graderingKvoter,
  intl,
  readOnly,
  graderingValues,
}) => (
  <PeriodFieldArray
    fields={fields}
    meta={meta}
    emptyPeriodTemplate={defaultGraderingPeriode}
    textCode="Registrering.Permisjon.Gradering.LeggTilPeriode"
    readOnly={readOnly}
  >
    {(periodeElementFieldId, index, getRemoveButton) => {
      const { periodeFom } = fields.get(index);
      const { harSamtidigUttak } = graderingValues[index];
      const periodeFomForTidlig = periodeFom && moment(periodeFom).isBefore(moment('2019-01-01'));
      return (
        <Row key={periodeElementFieldId}>
          <Column xs="12" className={index !== (fields.length - 1) ? styles.notLastRow : ''}>
            <FlexContainer wrap>
              <FlexRow>
                <FlexColumn>
                  <SelectField
                    name={`${periodeElementFieldId}.periodeForGradering`}
                    bredde="s"
                    selectValues={mapKvoter(graderingKvoter)}
                    label={{ id: 'Registrering.Permisjon.Gradering.Periode' }}
                  />
                </FlexColumn>
                <FlexColumn>
                  <DatepickerField
                    label={{ id: 'Registrering.Permisjon.periodeFom' }}
                    name={`${periodeElementFieldId}.periodeFom`}
                    defaultValue={null}
                  />
                </FlexColumn>
                <FlexColumn>
                  <DatepickerField
                    label={{ id: 'Registrering.Permisjon.periodeTom' }}
                    name={`${periodeElementFieldId}.periodeTom`}
                    defaultValue={null}
                  />
                </FlexColumn>
                <FlexColumn className={styles.prosentHeader}>
                  <Undertekst>
                    <FormattedMessage id="Registrering.Permisjon.Gradering.Prosentandel" />
                  </Undertekst>
                  <DecimalField
                    name={`${periodeElementFieldId}.prosentandelArbeid`}
                    bredde="S"
                    validate={[hasValidDecimal,
                      maxValue100,
                    ]}
                    normalizeOnBlur={value => (Number.isNaN(value) ? value : parseFloat(value).toFixed(2))}
                  />
                </FlexColumn>
                <FlexColumn>
                  <InputField
                    label={{ id: 'Registrering.Permisjon.Orgnr' }}
                    name={`${periodeElementFieldId}.orgNr`}
                    bredde="S"
                    parse={(value) => {
                      const parsedValue = parseInt(value, 10);
                      return Number.isNaN(parsedValue) ? value : parsedValue;
                    }}
                  />
                </FlexColumn>
              </FlexRow>
              <FlexRow>
                <FlexColumn>
                  <SelectField
                    label={{ id: 'Registrering.Permisjon.ErArbeidstakerLabel' }}
                    name={`${periodeElementFieldId}.erArbeidstaker`}
                    bredde="m"
                    selectValues={[
                      <option
                        value="true"
                        key="true"
                      >
                        {intl.formatMessage({ id: 'Registrering.Permisjon.ErArbeidstaker' })}
                      </option>,
                      <option
                        value="false"
                        key="false"
                      >
                        {intl.formatMessage({ id: 'Registrering.Permisjon.ErIkkeArbeidstaker' })}
                      </option>,
                    ]}
                  />
                </FlexColumn>
                <FlexColumn>
                  <div className={styles.graderesHeader}>
                    <Undertekst>
                      <FormattedMessage id="Registrering.Permisjon.Gradering.SkalGraderes" />
                    </Undertekst>
                  </div>
                  <CheckboxField
                    name={`${periodeElementFieldId}.skalGraderes`}
                    label=" "
                  />
                </FlexColumn>
                <FlexColumn>
                  <div className={styles.smalHeader}>
                    <Undertekst>
                      <FormattedMessage id="Registrering.Permisjon.Flerbarnsdager" />
                    </Undertekst>
                    <CheckboxField
                      readOnly={readOnly}
                      name={`${periodeElementFieldId}.flerbarnsdager`}
                      label=" "
                    />
                  </div>
                </FlexColumn>
                <FlexColumn>
                  <div className={styles.smalHeader}>
                    <Undertekst>
                      <FormattedMessage id="Registrering.Permisjon.HarSamtidigUttak" />
                    </Undertekst>
                  </div>
                  <CheckboxField
                    name={`${periodeElementFieldId}.harSamtidigUttak`}
                    label=""
                  />
                </FlexColumn>
                <FlexColumn>
                  {harSamtidigUttak
                  && (
                    <DecimalField
                      name={`${periodeElementFieldId}.samtidigUttaksprosent`}
                      bredde="S"
                      validate={[hasValidDecimal,
                        maxValue200,
                      ]}
                      label={{ id: 'Registrering.Permisjon.SamtidigUttaksprosent' }}
                      normalizeOnBlur={value => (Number.isNaN(value) ? value : parseFloat(value).toFixed(2))}
                    />
                  )
                  }
                </FlexColumn>
                <FlexColumn className={styles.placeRemoveButton}>
                  {getRemoveButton()}
                </FlexColumn>
              </FlexRow>
              {periodeFomForTidlig
              && (
                <div>
                  <FlexRow wrap>
                    <AlertStripe type="info">
                      <FormattedMessage id="Registrering.Permisjon.PeriodeFomForTidlig" />
                    </AlertStripe>
                  </FlexRow>
                  <VerticalSpacer eightPx />
                </div>
              )
              }
            </FlexContainer>
          </Column>
        </Row>
      );
    }
    }
  </PeriodFieldArray>
);

RenderGraderingPeriodeFieldArray.propTypes = {
  fields: PropTypes.shape().isRequired,
  graderingKvoter: kodeverkPropType.isRequired,
  meta: PropTypes.shape().isRequired,
  intl: intlShape.isRequired,
  readOnly: PropTypes.bool.isRequired,
  graderingValues: PropTypes.arrayOf(PropTypes.shape()).isRequired,
};

const mapStateToProps = (state, ownProps) => {
  const values = getFormValues(ownProps.meta.form)(state);
  const graderingValues = values ? values[ownProps.namePrefix][ownProps.graderingPrefix] : [{}];
  return {
    graderingValues,
  };
};


export default connect(mapStateToProps)(injectIntl(RenderGraderingPeriodeFieldArray));
