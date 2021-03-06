package com.github.ltprc.jigpipe.meta;

import java.net.InetSocketAddress;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;

import com.github.ltprc.jigpipe.constant.ErrorConstant;
import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.google.gson.Gson;

/**
 * Broker name resolver.
 * @author tuoli
 *
 */
public class NameService {
    private IRoleStrategy roleStrategy = new DefaultRoleStrategy();
    private String clusterName;

    public NameService(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public IRoleStrategy getRoleStrategy() {
        return roleStrategy;
    }

    public void setRoleStrategy(IRoleStrategy roleStrategy) {
        this.roleStrategy = roleStrategy;
    }

    /**
     * Resolve address of the specific message
     */
    public TopicAddress lookup(String pipelet, long offset, int role) {
        //Search stripe
        Stripe stripe;
        try {
            stripe = findStripe(pipelet, offset);
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE || e.code() == Code.NOAUTH) {
                throw new RuntimeException(ErrorConstant.ERR_INVALID_PARAMETER, e);
            }
            throw new RuntimeException(ErrorConstant.ERR_NO_STRIPE, e);
        } catch (InterruptedException e) {
            throw new RuntimeException(ErrorConstant.ERR_NO_STRIPE, e);
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
            throw new RuntimeException(ErrorConstant.ERR_NO_BROKER_GROUP, e);
        } catch (InterruptedException e) {
            throw new RuntimeException(ErrorConstant.ERR_NO_BROKER_GROUP, e);
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

    public TopicAddress lookup(String pipelet, long position) {
        return lookup(pipelet, position, roleStrategy.getCurrentRole());
    }

    public TopicAddress lookupPub(String pipelet) {
        return lookup(pipelet, Long.MAX_VALUE, JigpipeConstant.BROKER_MASTER);
    }

    /**
     * Searching the position of a message on its specific pipelet.
     */
    private Stripe findStripe(String pipeletName, long position) throws KeeperException, InterruptedException {
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
        throw new RuntimeException(ErrorConstant.ERR_NO_OFFSET);
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
