import React, { useEffect, useReducer, useRef, useState } from 'react';
import { Translate, translate, ValidatedField } from 'react-jhipster';
import { Button, Modal, ModalHeader, ModalBody, ModalFooter, Alert, Row, Col, Form } from 'reactstrap';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEye, faEyeSlash } from '@fortawesome/free-solid-svg-icons';
import { InputType } from 'reactstrap/types/lib/Input';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

type TypePassword = {
  type: 'text' | 'password';
  showPass: boolean;
};

const showPassWordReducer = (state: TypePassword, action: string) => {
  switch (action) {
    case 'SHOW_PASSWORD':
      return { ...state, type: 'text', showPass: true };
    case 'HIDE_PASSWORD':
      return { ...state, type: 'password', showPass: false };
    default:
      return { ...state, type: 'password', showPass: false };
  }
};

const LoginModal = (props: ILoginModalProps) => {
  const [showPassWord, showPassWordDispatch] = useReducer(showPassWordReducer, { type: 'password', showPass: false });

  const login = ({ username, password, rememberMe }) => {
    props.handleLogin(username, password, rememberMe);
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const { loginError, handleClose } = props;

  const changeShowPassWord = () => {
    showPassWordDispatch(!showPassWord.showPass ? 'SHOW_PASSWORD' : 'HIDE_PASSWORD');
  };

  const handleLoginSubmit = e => {
    handleSubmit(login)(e);
  };

  return (
    <Modal isOpen={props.showModal} toggle={handleClose} backdrop="static" id="login-page" autoFocus={false}>
      <Form onSubmit={handleLoginSubmit}>
        <ModalHeader id="login-title" data-cy="loginTitle" toggle={handleClose}>
          <Translate contentKey="login.title">Sign in</Translate>
        </ModalHeader>
        <ModalBody>
          <Row>
            <Col md="12">
              {loginError ? (
                <Alert color="danger" data-cy="loginError">
                  <Translate contentKey="login.messages.error.authentication">
                    <strong>Failed to sign in!</strong> Please check your credentials and try again.
                  </Translate>
                </Alert>
              ) : null}
            </Col>
            <Col md="12">
              <ValidatedField
                name="username"
                label={translate('global.form.username.label')}
                placeholder={translate('global.form.username.placeholder')}
                required
                autoFocus
                data-cy="username"
                validate={{ required: 'Username cannot be empty!' }}
                register={register}
                error={errors.username}
                isTouched={touchedFields.username}
              />
              <div className="d-flex flex-row align-items-center position-relative">
                <ValidatedField
                  name="password"
                  type={showPassWord.type as InputType}
                  label={translate('login.form.password')}
                  placeholder={translate('login.form.password.placeholder')}
                  required
                  data-cy="password"
                  validate={{ required: 'Password cannot be empty!' }}
                  register={register}
                  error={errors.password}
                  isTouched={touchedFields.password}
                  autoComplete="none"
                  className="w-100"
                  style={{
                    paddingRight: '75px',
                  }}
                />
                <FontAwesomeIcon
                  className="cursor-pointer position-absolute"
                  style={{ right: '40px', top: '43px' }}
                  icon={showPassWord.showPass ? faEye : faEyeSlash}
                  onClick={changeShowPassWord}
                />
              </div>
              <ValidatedField
                name="rememberMe"
                type="checkbox"
                check
                label={translate('login.form.rememberme')}
                value={true}
                register={register}
              />
            </Col>
          </Row>
          <div className="mt-1">&nbsp;</div>
          <Alert color="warning">
            <span>
              <Translate contentKey="global.messages.info.register.noaccount">You don&apos;t have an account yet?</Translate>
            </span>{' '}
            <Link to="/account/register">
              <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
            </Link>
          </Alert>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={handleClose} tabIndex={1}>
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>{' '}
          <Button color="primary" type="submit" data-cy="submit">
            <Translate contentKey="login.form.button">Sign in</Translate>
          </Button>
        </ModalFooter>
      </Form>
    </Modal>
  );
};

export default LoginModal;
