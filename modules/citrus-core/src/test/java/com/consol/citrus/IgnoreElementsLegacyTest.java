/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus;

import static org.easymock.EasyMock.*;

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.context.ValidationContextBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContextBuilder;

/**
 * @author Christoph Deppisch
 */
public class IgnoreElementsLegacyTest extends AbstractBaseTest {
    @Autowired
    MessageValidator<ValidationContext> validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setup() {
        super.setup();
        
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);

        receiveMessageBean.setValidator(validator);
    }

    @Test
    public void testIgnoreElements() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA");
        ignoreMessageElements.add("sub-elementB");
        contextBuilder.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContextBuilder<? extends ValidationContext>> validationContextBuilders = 
            new ArrayList<ValidationContextBuilder<? extends ValidationContext>>();
        validationContextBuilders.add(contextBuilder);
        receiveMessageBean.setValidationContextBuilders(validationContextBuilders);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributes() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                + "<sub-elementB attribute='no validation'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA.attribute");
        ignoreMessageElements.add("sub-elementB.attribute");
        contextBuilder.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContextBuilder<? extends ValidationContext>> validationContextBuilders = 
            new ArrayList<ValidationContextBuilder<? extends ValidationContext>>();
        validationContextBuilders.add(contextBuilder);
        receiveMessageBean.setValidationContextBuilders(validationContextBuilders);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testIgnoreRootElement() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element>Text</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element additonal-attribute='some'>Wrong text</element>" 
                        + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root");
        contextBuilder.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContextBuilder<? extends ValidationContext>> validationContextBuilders = 
            new ArrayList<ValidationContextBuilder<? extends ValidationContext>>();
        validationContextBuilders.add(contextBuilder);
        receiveMessageBean.setValidationContextBuilders(validationContextBuilders);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreElementsAndValidate() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA");
        ignoreMessageElements.add("sub-elementB");
        contextBuilder.setIgnoreExpressions(ignoreMessageElements);
        
        List<ValidationContextBuilder<? extends ValidationContext>> validationContextBuilders = 
            new ArrayList<ValidationContextBuilder<? extends ValidationContext>>();
        validationContextBuilders.add(contextBuilder);
        receiveMessageBean.setValidationContextBuilders(validationContextBuilders);
        
        Map<String, String> validateElements = new HashMap<String, String>();
        validateElements.put("root.element.sub-elementA", "wrong value");
        validateElements.put("sub-elementB", "wrong value");
        contextBuilder.setPathValidationExpressions(validateElements);
        
        receiveMessageBean.execute(context);
    }
}
