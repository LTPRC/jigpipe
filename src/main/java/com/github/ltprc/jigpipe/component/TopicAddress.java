package com.github.ltprc.jigpipe.component;

import java.net.InetSocketAddress;

public class TopicAddress {
    private InetSocketAddress address;
    
    private Stripe stripe;
    
    public InetSocketAddress getAddress() {
        return address;
    }
    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }
    public Stripe getStripe() {
        return stripe;
    }
    public void setStripe(Stripe stripe) {
        this.stripe = stripe;
    }
}
