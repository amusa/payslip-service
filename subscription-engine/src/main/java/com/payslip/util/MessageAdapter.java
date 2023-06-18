package com.payslip.util;

import com.microsoft.graph.models.Message;
import com.payslip.subscription.NewMessageNotification;

public class MessageAdapter {

    public static NewMessageNotification convert(Message message){
       
        return new NewMessageNotification(
            message.id, 
            message.subject,
            message.sender.emailAddress.address,
            message.sentDateTime.toLocalDateTime());        
    }
    
}
