import React from 'react';
import { shallow } from 'enzyme';
import { expect } from 'chai';
import sinon from 'sinon';

import DocumentList from './DocumentList';

describe('<DocumentList>', () => {
  it('skal vise to dokumenter i liste', () => {
    const document = {
      journalpostId: '1',
      dokumentId: '1',
      tittel: 'Terminbekreftelse',
      tidspunkt: '0405198632231',
      kommunikasjonsretning: 'INN',
    };

    const anotherDocument = {
      journalpostId: '2',
      dokumentId: '2',
      tittel: 'Førstegangssøknad',
      tidspunkt: '0405198632231',
      kommunikasjonsretning: 'UT',
    };

    const wrapper = shallow(<DocumentList
      documents={[document, anotherDocument]}
      selectDocumentCallback={sinon.spy()}
      behandlingId={1}
    />);

    const label = wrapper.find('Label');
    expect(label).to.have.length(0);

    const table = wrapper.find('Table');
    expect(table).to.have.length(1);
    const tableRows = table.find('TableRow');
    expect(tableRows).to.have.length(2);

    const tableColumnsRow1 = tableRows.first().find('TableColumn');
    expect(tableColumnsRow1.children()).to.have.length(3);
    expect(tableColumnsRow1.at(1).html()).to.eql('<td>Terminbekreftelse</td>');

    const tableColumnsRow2 = tableRows.last().find('TableColumn');
    expect(tableColumnsRow2.children()).to.have.length(3);
    expect(tableColumnsRow2.at(1).html()).to.eql('<td>Førstegangssøknad</td>');
  });

  it('skal vise korrekt tekst om ikke tidspunkt finnes', () => {
    const document = {
      journalpostId: '1',
      dokumentId: '1',
      tittel: 'Terminbekreftelse',
      tidspunkt: null,
      kommunikasjonsretning: 'INN',
    };

    const wrapper = shallow(<DocumentList
      documents={[document]}
      selectDocumentCallback={sinon.spy()}
      behandlingId={1}
    />);

    const formattedMessage = wrapper.find('FormattedMessage');
    expect(formattedMessage).to.have.length(1);
    expect(formattedMessage.prop('id')).to.eql('DocumentList.IProduksjon');
  });

  it('skal ikke vise tabell når det ikke finnes dokumenter', () => {
    const wrapper = shallow(<DocumentList
      documents={[]}
      selectDocumentCallback={sinon.spy()}
      behandlingId={1}
    />);

    const label = wrapper.find('Normaltekst');
    expect(label).to.have.length(1);
    const table = wrapper.find('Table');
    expect(table).to.have.length(0);
  });
});
