import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Element } from 'nav-frontend-typografi';
import { FieldArray, formValueSelector } from 'redux-form';

import { CheckboxField } from 'form/Fields';
import VerticalSpacer from 'sharedComponents/VerticalSpacer';
import { getKodeverk } from 'kodeverk/duck';
import kodeverkTyper from 'kodeverk/kodeverkTyper';
import kodeverkPropType from 'kodeverk/kodeverkPropType';
import {
  hasValidInteger,
  hasValidPeriodIncludingOtherErrors,
  maxLength,
  required,
  validateProsentandel,
} from 'utils/validation/validators';
import { isRequiredMessage } from 'utils/validation/messages';
import RenderGraderingPeriodeFieldArray from './RenderGraderingPeriodeFieldArray';

export const graderingPeriodeFieldArrayName = 'graderingPeriode';

const maxLength9 = maxLength(9);

/**
 *  PermisjonGraderingPanel
 *
 * Presentasjonskomponent: Viser panel for gradering
 * Komponenten har inputfelter og må derfor rendres som etterkommer av komponent dekorert med reduxForm.
 */
export const PermisjonGraderingPanel = ({
  graderingKvoter,
  form,
  namePrefix,
  skalGradere,
  readOnly,
}) => (
  <div>
    <Element><FormattedMessage id="Registrering.Permisjon.Gradering.Title" /></Element>
    <VerticalSpacer sixteenPx />
    <CheckboxField
      readOnly={readOnly}
      name="skalGradere"
      label={<FormattedMessage id="Registrering.Permisjon.Gradering.GraderUttaket" />}
    />
    { skalGradere
    && (
    <FieldArray
      name={graderingPeriodeFieldArrayName}
      component={RenderGraderingPeriodeFieldArray}
      graderingKvoter={graderingKvoter}
      form={form}
      namePrefix={namePrefix}
      graderingPrefix={graderingPeriodeFieldArrayName}
      readOnly={readOnly}
    />
    )
    }
  </div>
);

export const validateOtherErrors = values => values.map(({
  periodeForGradering, prosentandelArbeid, orgNr, erArbeidstaker, samtidigUttaksprosent, harSamtidigUttak,
}) => {
  const periodeForGraderingError = required(periodeForGradering);
  const prosentandelArbeidError = validateProsentandel(prosentandelArbeid);
  const orgNrShouldBeRequired = erArbeidstaker === 'true';
  const orgNrError = (orgNrShouldBeRequired && required(orgNr)) || hasValidInteger(orgNr) || maxLength9(orgNr);
  const samtidigUttaksprosentError = harSamtidigUttak === true && required(samtidigUttaksprosent);
  if (prosentandelArbeidError || periodeForGraderingError || orgNrError || samtidigUttaksprosentError) {
    return {
      periodeForGradering: periodeForGraderingError,
      orgNr: orgNrError,
      prosentandelArbeid: prosentandelArbeidError,
      samtidigUttaksprosent: samtidigUttaksprosentError,
    };
  }
  return null;
});

PermisjonGraderingPanel.validate = (values) => {
  if (!values || !values.length) {
    return { _error: isRequiredMessage() };
  }
  const otherErrors = validateOtherErrors(values);

  return hasValidPeriodIncludingOtherErrors(values, otherErrors);
};

PermisjonGraderingPanel.propTypes = {
  graderingKvoter: kodeverkPropType.isRequired,
  form: PropTypes.string.isRequired,
  namePrefix: PropTypes.string.isRequired,
  skalGradere: PropTypes.bool.isRequired,
  readOnly: PropTypes.bool.isRequired,
};

PermisjonGraderingPanel.initialValues = {
  [graderingPeriodeFieldArrayName]: [{}],
  skalGradere: false,
};


const mapStateToProps = (state, ownProps) => ({
  graderingKvoter: getKodeverk(kodeverkTyper.UTSETTELSE_GRADERING_KVOTE)(state),
  skalGradere: formValueSelector(ownProps.form)(state, ownProps.namePrefix).skalGradere,
});

export default connect(mapStateToProps)(PermisjonGraderingPanel);
