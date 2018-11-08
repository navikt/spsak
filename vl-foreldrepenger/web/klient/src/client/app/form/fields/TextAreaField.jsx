import React from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';
import { TextareaControlled as NavTextareaControlled } from 'nav-frontend-skjema';
import EtikettFokus from 'nav-frontend-etiketter';
import { injectIntl, intlShape, FormattedMessage } from 'react-intl';

import renderNavField from './renderNavField';
import { labelPropType } from './Label';

import styles from './textAreaField.less';
import ReadOnlyField from './ReadOnlyField';


const TextAreaWithBadge = ({
  badges,
  intl,
  isEdited,
  ...otherProps
}) => (
  <div className={badges ? styles.textAreaFieldWithBadges : null}>
    { badges
    && (
    <div className={styles.etikettWrapper}>
      { badges.map(({ textId, type, title }) => (
        <EtikettFokus key={textId} type={type} title={intl.formatMessage({ id: title })}>
          <FormattedMessage id={textId} />
        </EtikettFokus>))}
    </div>
    )
    }
    <NavTextareaControlled {...otherProps} />
  </div>
);

const renderNavTextArea = renderNavField(injectIntl(TextAreaWithBadge));

const TextAreaField = ({
  name, label, validate, readOnly, ...otherProps
}) => (
  <Field
    name={name}
    validate={validate}
    component={readOnly ? ReadOnlyField : renderNavTextArea}
    label={label}
    {...otherProps}
    readOnly={readOnly}
    readOnlyHideEmpty
    autoComplete="off"
  />
);

TextAreaField.propTypes = {
  name: PropTypes.string.isRequired,
  label: labelPropType.isRequired,
  validate: PropTypes.arrayOf(PropTypes.func),
  readOnly: PropTypes.bool,
};

TextAreaField.defaultProps = {
  validate: null,
  readOnly: false,
};

TextAreaWithBadge.propTypes = {
  intl: intlShape.isRequired,
  badges: PropTypes.arrayOf(PropTypes.shape({
    textId: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
  })),
  isEdited: PropTypes.bool,
};

TextAreaWithBadge.defaultProps = {
  badges: null,
  isEdited: false,
};

export default TextAreaField;
