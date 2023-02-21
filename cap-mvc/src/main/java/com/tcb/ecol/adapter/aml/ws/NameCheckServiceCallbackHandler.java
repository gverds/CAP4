/**
 * NameCheckServiceCallbackHandler.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis2 version: 1.8.0 Built on : Aug 01,
 * 2021 (07:27:19 HST)
 */
package com.tcb.ecol.adapter.aml.ws;

/**
 * NameCheckServiceCallbackHandler Callback class, Users can extend this class and implement their
 * own receiveResult and receiveError methods.
 */
public abstract class NameCheckServiceCallbackHandler {

  protected Object clientData;

  /**
   * User can pass in any object that needs to be accessed once the NonBlocking Web service call is
   * finished and appropriate method of this CallBack is called.
   *
   * @param clientData Object mechanism by which the user can pass in user data that will be
   *     avilable at the time this callback is called.
   */
  public NameCheckServiceCallbackHandler(Object clientData) {
    this.clientData = clientData;
  }

  /** Please use this constructor if you don't want to set any clientData */
  public NameCheckServiceCallbackHandler() {
    this.clientData = null;
  }

  /** Get the client data */
  public Object getClientData() {
    return clientData;
  }

  /**
   * auto generated Axis2 call back method for submitNameCheck method override this method for
   * handling normal response from submitNameCheck operation
   */
  public void receiveResultsubmitNameCheck(
      com.tcb.ecol.adapter.aml.ws.NameCheckServiceStub.SubmitNameCheckResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * submitNameCheck operation
   */
  public void receiveErrorsubmitNameCheck(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for submitNameCheckByModule method override this method
   * for handling normal response from submitNameCheckByModule operation
   */
  public void receiveResultsubmitNameCheckByModule(
      com.tcb.ecol.adapter.aml.ws.NameCheckServiceStub.SubmitNameCheckByModuleResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * submitNameCheckByModule operation
   */
  public void receiveErrorsubmitNameCheckByModule(java.lang.Exception e) {}
}
