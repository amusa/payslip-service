/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.payslip.api.infrastructure.ews;

import microsoft.exchange.webservices.data.notification.StreamingSubscriptionConnection;

/**
 *
 * @author maliska
 */
public interface StreamingSubscriber extends StreamingSubscriptionConnection.INotificationEventDelegate, StreamingSubscriptionConnection.ISubscriptionErrorDelegate{
    void connect() throws Exception;
    void reconnect(StreamingSubscriptionConnection connection) throws Exception;
}
