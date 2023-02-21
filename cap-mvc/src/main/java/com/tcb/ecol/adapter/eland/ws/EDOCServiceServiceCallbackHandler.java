/**
 * EDOCServiceServiceCallbackHandler.java
 *
 * <p>This file was auto-generated from WSDL by the Apache Axis2 version: 1.8.0 Built on : Aug 01,
 * 2021 (07:27:19 HST)
 */
package com.tcb.ecol.adapter.eland.ws;

/**
 * EDOCServiceServiceCallbackHandler Callback class, Users can extend this class and implement their
 * own receiveResult and receiveError methods.
 */
public abstract class EDOCServiceServiceCallbackHandler {

  protected Object clientData;

  /**
   * User can pass in any object that needs to be accessed once the NonBlocking Web service call is
   * finished and appropriate method of this CallBack is called.
   *
   * @param clientData Object mechanism by which the user can pass in user data that will be
   *     avilable at the time this callback is called.
   */
  public EDOCServiceServiceCallbackHandler(Object clientData) {
    this.clientData = clientData;
  }

  /** Please use this constructor if you don't want to set any clientData */
  public EDOCServiceServiceCallbackHandler() {
    this.clientData = null;
  }

  /** Get the client data */
  public Object getClientData() {
    return clientData;
  }

  /**
   * auto generated Axis2 call back method for applyQuery method override this method for handling
   * normal response from applyQuery operation
   */
  public void receiveResultapplyQuery(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.ApplyQueryResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * applyQuery operation
   */
  public void receiveErrorapplyQuery(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for applyEDOC method override this method for handling
   * normal response from applyEDOC operation
   */
  public void receiveResultapplyEDOC(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.ApplyEDOCResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * applyEDOC operation
   */
  public void receiveErrorapplyEDOC(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for getSection method override this method for handling
   * normal response from getSection operation
   */
  public void receiveResultgetSection(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.GetSectionResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * getSection operation
   */
  public void receiveErrorgetSection(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for exportEDOCData method override this method for
   * handling normal response from exportEDOCData operation
   */
  public void receiveResultexportEDOCData(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.ExportEDOCDataResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * exportEDOCData operation
   */
  public void receiveErrorexportEDOCData(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for getCity method override this method for handling
   * normal response from getCity operation
   */
  public void receiveResultgetCity(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.GetCityResponse result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * getCity operation
   */
  public void receiveErrorgetCity(java.lang.Exception e) {}

  /**
   * auto generated Axis2 call back method for applyQuery2 method override this method for handling
   * normal response from applyQuery2 operation
   */
  public void receiveResultapplyQuery2(
      com.tcb.ecol.adapter.eland.ws.EDOCServiceServiceStub.ApplyQuery2Response result) {}

  /**
   * auto generated Axis2 Error handler override this method for handling error response from
   * applyQuery2 operation
   */
  public void receiveErrorapplyQuery2(java.lang.Exception e) {}
}
