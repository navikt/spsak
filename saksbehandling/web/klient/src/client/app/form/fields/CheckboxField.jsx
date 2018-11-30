import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';
import { Checkbox as NavCheckbox } from 'nav-frontend-skjema';
import { Normaltekst } from 'nav-frontend-typografi';

import renderNavField from './renderNavField';
import { labelPropType } from './Label';

export const RenderCheckboxField = renderNavField(({ onChange, label, ...otherProps }) => (
  <NavCheckbox
    onChange={e => onChange(e.target.checked)}
    checked={otherProps.value}
    label={React.cloneElement(label, { typographyElement: Normaltekst })}
    {...otherProps}
  />
));

const CheckboxField = ({
  name, label, validate, readOnly, ...otherProps
}) => (
  <Field
    name={name}
    validate={validate}
    component={RenderCheckboxField}
    label={label}
    disabled={readOnly}
    readOnly={readOnly}
    readOnlyHideEmpty
    {...otherProps}
  />
);

CheckboxField.propTypes = {
  name: PropTypes.string.isRequired,
  label: labelPropType.isRequired,
  validate: PropTypes.arrayOf(PropTypes.func),
  readOnly: PropTypes.bool,
};

CheckboxField.defaultProps = {
  validate: null,
  readOnly: false,
};

export default CheckboxField;
