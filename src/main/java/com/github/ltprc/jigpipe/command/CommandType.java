package com.github.ltprc.jigpipe.command;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;

/**
 * Command type enumerator implemented from Google protobuf.
 * @author tuoli
 *
 */
public enum CommandType implements com.google.protobuf.ProtocolMessageEnum {

     BMQ_UNDEFINED,
     /**
     * <code>BMQ_SEND = 1;</code>
     *
     * <pre>
     * this uses MessageCommand
     * </pre>
     */
     BMQ_SEND,
     /**
     * <code>BMQ_MESSAGE = 2;</code>
     */
     BMQ_MESSAGE,
     /**
     * <code>BMQ_MESSAGEPACK = 3;</code>
     */
     BMQ_MESSAGEPACK,
     /**
     * <code>BMQ_CONNECT = 4;</code>
     */
     BMQ_CONNECT,
     /**
     * <code>BMQ_DISCONNECT = 5;</code>
     */
     BMQ_DISCONNECT,
     /**
     * <code>BMQ_SUBSCRIBE = 6;</code>
     */
     BMQ_SUBSCRIBE,
     /**
     * <code>BMQ_SUBSCRIBEALL = 7;</code>
     */
     BMQ_SUBSCRIBEALL,
     /**
     * <code>BMQ_UNSUBSCRIBE = 8;</code>
     */
     BMQ_UNSUBSCRIBE,
     /**
     * <code>BMQ_MIGRATE_PIPELET = 9;</code>
     */
     BMQ_MIGRATE_PIPELET,
     /**
     * <code>BMQ_CMDSEND = 10;</code>
     */
     BMQ_CMDSEND,
     /**
     * <code>BMQ_CMDMESSAGE = 11;</code>
     */
     BMQ_CMDMESSAGE,
     /**
     * <code>BMQ_GETTICKET = 12;</code>
     */
     BMQ_GETTICKET,
     /**
     * <code>BMQ_TICKET = 13;</code>
     */
     BMQ_TICKET,
     /**
     * <code>BMQ_ACK = 21;</code>
     *
     * <pre>
     * acks
     * </pre>
     */
     BMQ_ACK,
     /**
     * <code>BMQ_ERROR = 22;</code>
     */
     BMQ_ERROR,
     /**
     * <code>BMQ_CONNECTED = 23;</code>
     */
     BMQ_CONNECTED,
     /**
     * <code>BMQ_RECEIPT = 24;</code>
     */
     BMQ_RECEIPT,
     ;

     @Override
     public int getNumber() {
         // TODO Auto-generated method stub
         return 0;
     }

     @Override
     public EnumValueDescriptor getValueDescriptor() {
         // TODO Auto-generated method stub
         return null;
     }

     @Override
     public EnumDescriptor getDescriptorForType() {
         // TODO Auto-generated method stub
         return null;
     }
}
