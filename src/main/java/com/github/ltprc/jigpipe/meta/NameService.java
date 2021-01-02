package org.lituo.jigpipe.meta;

import java.net.InetSocketAddress;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import com.google.gson.Gson;

import org.lituo.jigpipe.component.Broker;
import org.lituo.jigpipe.component.BrokerGroup;
import org.lituo.jigpipe.component.Pipelet;
import org.lituo.jigpipe.component.Stripe;
import org.lituo.jigpipe.component.TopicAddress;
import org.lituo.jigpipe.exception.InvalidParameter;
import org.lituo.jigpipe.exception.NameResolveException;
import org.lituo.jigpipe.exception.StripeOffsetException;

public class NameService {
    private IRoleSelectStrategy roleStrategy = new DefaultRoleStrategy();
    private String clusterName;

    public NameService(String topDomainName) {
        clusterName = topDomainName;
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
        for (Broker b : group.getBrokers()) {
            if (b.getRole() == role) {
                addr.setAddress(new InetSocketAddress(b.getIp(), b.getPort()));
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
        return lookup(pipelet, Long.MAX_VALUE, 1);
    }

    /**
     * Searching the position of a message on its specific pipelet.
     */
    private Stripe findStripe(String pipelet, long position)
            throws NameResolveException, KeeperException, InterruptedException {
        String pipeletPath = "/" + pipelet; // Mock pipelet path.
        String pipeletinfo = ZooKeeperUtil.getInstance(clusterName).getMeta(pipeletPath);

        Gson gson = new Gson();
        Pipelet p = gson.fromJson(pipeletinfo, Pipelet.class);

        long minBeginPos = Long.MAX_VALUE;
        Stripe oldestStripe = new Stripe();
        oldestStripe.setBeginPos(0L);
        //Search correct stripe
        for (Stripe s : p.getStripes()) {
            if (s.getBeginPos() > 0 && s.getBeginPos() <= s.getEndPos()) {
                if (s.getBeginPos() < minBeginPos) {
                    minBeginPos = s.getBeginPos();
                    oldestStripe = s;
                }
                if (s.getBeginPos() <= position && s.getBeginPos() >= position) {
                    return s;
                }
            }
        }
        if (position == 0) {
            return oldestStripe;
        }
        throw new StripeOffsetException(pipelet, position, oldestStripe.getBeginPos());
    }

    /**
     * Searching the broker group info of a specific stripe.
     */
    private BrokerGroup getGroup(Stripe s) throws KeeperException, InterruptedException {
        Gson gson = new Gson();
        String groupPath = String.format("/%s", s.getServingGroup()); // Mock group path.
        String groupInfo = ZooKeeperUtil.getInstance(clusterName).getMeta(groupPath);
        return gson.fromJson(groupInfo, BrokerGroup.class);
    }
}