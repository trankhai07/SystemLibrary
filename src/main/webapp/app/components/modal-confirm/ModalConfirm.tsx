import React, { ReactNode } from 'react';
import './style.scss';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap';

type TypePropsModalConfirm = {
  children: ReactNode;
  title?: ReactNode;
  toggle: () => void;
  isOpen: boolean;
  textSubmit?: string;
  textCancel?: string;
  onSubmit?: () => void;
  onCanncel?: () => void;
};

const ModalConfirm = (props: TypePropsModalConfirm) => {
  const { isOpen, toggle, title, children, textSubmit = 'OK', textCancel = 'Cancel', onCanncel, onSubmit } = props;

  return (
    <Modal className="modal-confirm" isOpen={isOpen} toggle={toggle}>
      <ModalHeader className="modal-confirm-header" toggle={toggle}>
        {title}
      </ModalHeader>
      <ModalBody>{children}</ModalBody>
      <ModalFooter className="modal-confirm-footer">
        <Button outline size="sm" className="btn-cancel" onClick={toggle}>
          {textCancel}
        </Button>
        <Button color="danger" size="sm" onClick={onSubmit}>
          {textSubmit}
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default ModalConfirm;
