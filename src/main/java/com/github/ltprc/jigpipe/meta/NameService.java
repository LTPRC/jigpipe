package com.github.ltprc.jigpipe.meta;

import java.net.InetSocketAddress;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;

import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.github.ltprc.jigpipe.exception.InvalidParameter;
import com.github.ltprc.jigpipe.exception.NameResolveException;
import com.github.ltprc.jigpipe.exception.StripeOffsetException;
import com.google.gson.Gson;

public class NameService {
    private IRoleSelectStrategy roleStrategy = new DefaultRoleStrategy();
    private String clusterName;

    public NameService(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public IRoleSelectStrategy getRoleStrategy() {
        return roleStrategy;
    }

    public void setRoleStrategy(IRoleSelectStrategy roleStrategy) {
        this.roleStrategy = roleStrategy;
    }

    /**
     * Search url for a specific postion.
     * @throws NameResolveException
     */
    public TopicAddress lookup(String pipelet, long position, int role) throws NameResolveException {
        //Search stripe
        Stripe stripe;
        try {
            stripe = findStripe(pipelet, position);
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE || e.code() == Code.NOAUTH) {
                InvalidParameter error = new InvalidParameter("no access to path " + e.getPath());
                error.initCause(e);
                throw error;
            }
            NameResolveException ne = new NameResolveException(pipelet, position,
                    "get stripe " + e.getPath() + " via zookeeper failed");
            ne.initCause(e);
            throw ne;
        } catch (InterruptedException e) {
            NameResolveException ne = new NameResolveException(pipelet, position,
                    "get stripe interrupted: " + pipelet + " at " + position);
            ne.initCause(e);
            throw ne;
        }
        
        //Search broker group
        BrokerGroup group;
        try {
            group = getGroup(stripe);
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE || e.code() == Code.NOAUTH) {
                RuntimeException error = new RuntimeException(
                        "zookeeper information inconsistency! no access to " + e.getPath());
                error.initCause(e);
                throw error;
            }
            NameResolveException ne = new NameResolveException(pipelet, position,
                    "get group " + stripe.getServingGroup() + " via zookeeper failed");
            ne.initCause(e);
            throw ne;
        } catch (InterruptedException e) {
            NameResolveException ne = new NameResolveException(pipelet, position,
                    "get group interrupted" + stripe.getServingGroup());
            ne.initCause(e);
            throw ne;
        }

        TopicAddress addr = new TopicAddress();
        addr.setStripe(stripe);
        for (Broker broker : group.getBrokers()) {
            if (broker.getRole() == role) {
                addr.setAddress(new InetSocketAddress(broker.getIp(), broker.getPort()));
                return addr;
            }
        }
        throw new RuntimeException(
                "role " + role + "not found in group " + stripe.getServingGroup() + ", meta information is abnormal");
    }

    /**
     * 
     * @throws NameResolveException
     */
    public TopicAddress lookup(String pipelet, long position) throws NameResolveException {
        return lookup(pipelet, position, roleStrategy.getCurrentRole());
    }

    /**
     * 
     * @throws NameResolveException
     */
    public TopicAddress lookupPub(String pipelet) throws NameResolveException {
        return lookup(pipelet, Long.MAX_VALUE, JigpipeConstant.BROKER_MASTER);
    }

    /**
     * Searching the position of a message on its specific pipelet.
     */
    private Stripe findStripe(String pipeletName, long position)
            throws NameResolveException, KeeperException, InterruptedException {
        String pipeletPath = "/" + clusterName + "/" + pipeletName;
        String pipeletinfo = MetaMap.INSTANCE.getInstance().get(clusterName).getMeta(pipeletPath);

        Gson gson = new Gson();
        Pipelet pipelet = gson.fromJson(pipeletinfo, Pipelet.class);

        long minBeginPos = Long.MAX_VALUE;
        Stripe oldestStripe = new Stripe();
        oldestStripe.setBeginPos(0L);
        //Search correct stripe
        for (Stripe stripe : pipelet.getStripes()) {
            if (stripe.getBeginPos() > stripe.getEndPos()) {
                //Invalid stripe, pass.
            }
            if (stripe.getBeginPos() < minBeginPos) {
                minBeginPos = stripe.getBeginPos();
                oldestStripe = stripe;
            }
            if (stripe.getBeginPos() <= position && stripe.getEndPos() >= position) {
                return stripe;
            }
        }
        if (position == 0) {
            return oldestStripe;
        }
        throw new StripeOffsetException(pipeletName, position, oldestStripe.getBeginPos());
    }

    /**
     * Searching the broker group info of a specific stripe.
     */
    private BrokerGroup getGroup(Stripe stripe) throws KeeperException, InterruptedException {
        Gson gson = new Gson();
        String groupPath = String.format("/%s", stripe.getServingGroup());
        String groupInfo = MetaMap.INSTANCE.getInstance().get(clusterName).getMeta(groupPath);
        return gson.fromJson(groupInfo, BrokerGroup.class);
    }
}
