package com.ms.user.producers;

import com.ms.user.dtos.EmailDto;
import com.ms.user.models.UserModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value ="${broker.queue.email.name}")
    private String routingKey;

    public void publishMessageEmail(UserModel userModel){
        var emailDto = new EmailDto();
        emailDto.setUserId(userModel.getId());
        emailDto.setEmailTo(userModel.getEmail());
        emailDto.setSubject("E-mail enviado com sucesso");
        emailDto.setText("Caro " + userModel.getName() + ", \n " +
                "O teste de envio de e-mail utilizando microsservices e RabbitMQ deu certo. \n\n " +
                "Tenha uma ótima semana. \n");
        rabbitTemplate.convertAndSend("",routingKey, emailDto);
    }

}
