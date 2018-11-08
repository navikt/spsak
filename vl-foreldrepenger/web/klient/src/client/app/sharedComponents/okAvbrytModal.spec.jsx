import React from 'react';
import { expect } from 'chai';
import sinon from 'sinon';
import { FormattedMessage } from 'react-intl';

import { shallowWithIntl, intlMock } from 'testHelpers/intl-enzyme-test-helper';
import Modal from 'sharedComponents/Modal';
import Image from 'sharedComponents/Image';

import OkAvbrytModal from './OkAvbrytModal';

describe('<OkAvbrytModal>', () => {
  it('skal rendre modal', () => {
    const wrapper = shallowWithIntl(
      <OkAvbrytModal.WrappedComponent
        intl={intlMock}
        textCode="OpenBehandlingForChangesMenuItem.OpenBehandling"
        showModal
        cancel={sinon.spy()}
        submit={sinon.spy()}
      />,
    );

    const modal = wrapper.find(Modal);
    expect(modal).to.have.length(1);
    expect(modal.prop('isOpen')).is.true;
    expect(modal.prop('contentLabel')).is.eql('Åpne behandling for endringer?');

    const image = wrapper.find(Image);
    expect(image).to.have.length(1);
    expect(image.prop('altCode')).is.eql('OpenBehandlingForChangesMenuItem.OpenBehandling');

    const message = wrapper.find(FormattedMessage);
    expect(message).to.have.length(1);
    expect(message.prop('id')).is.eql('OpenBehandlingForChangesMenuItem.OpenBehandling');
  });
});
