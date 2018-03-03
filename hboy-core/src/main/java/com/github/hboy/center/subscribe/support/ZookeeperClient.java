package com.github.hboy.center.subscribe.support;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.github.hboy.common.util.Constants;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 16:47
 */
public class ZookeeperClient {
	
	
	protected static final Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
	
	private ZkClient client;

	private final InvokerConfig url;
	
	private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();
	
	private final ConcurrentMap<String, ConcurrentMap<ChildListener, IZkChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, IZkChildListener>>();
	
	private final ConcurrentMap<String, ConcurrentMap<DataListener, IZkDataListener>> dataListeners = new ConcurrentHashMap<String, ConcurrentMap<DataListener, IZkDataListener>>();
	
	private volatile boolean closed = false;
	
	private boolean isConnected = false;
	
	public ZookeeperClient(InvokerConfig url) {
		this.url = url;
	}
	
	public ZkClient getClient() {
		return client;
	}

	public synchronized void connect(){
		if(!isConnected){
			client = new ZkClient(url.getSubscribeAddress(),url.getSubscribeSessionTimeOut(),url.getSubscribeConnectionTimeOut());
			//监听zk的连接变化，
			client.subscribeStateChanges(new IZkStateListener() {
				public void handleStateChanged(KeeperState state) throws Exception {
					if (state == KeeperState.Disconnected) {
						stateChanged(StateListener.DISCONNECTED);
					} else if (state == KeeperState.SyncConnected) {
						stateChanged(StateListener.CONNECTED);
					}
				}
				public void handleNewSession() throws Exception {
					stateChanged(StateListener.RECONNECTED);
				}
			});
			isConnected = true;
		}
	}
	
	public InvokerConfig getUrl(){
		return url;
	}
	
	public void create(String path, boolean ephemeral) {
		int i = path.lastIndexOf('/');
		if (i > 0) {
			create(path.substring(0, i), false);
		}
		if (ephemeral) {
			createEphemeral(path);
		} else {
			createPersistent(path);
		}
	}
	
	public void addStateListener(StateListener listener) {
		stateListeners.add(listener);
	}

	public void createPersistent(String path) {
		client.createPersistent(path, true);
	}

	public void createEphemeral(String path) {
		client.createEphemeral(path);
	}

	public void createEphemeral(String path,byte[] data) {
        client.createEphemeral(path, data);
    }
	
	public void delete(String path) {
		client.delete(path);
	}
	
	public boolean exists(String path) {
		return client.exists(path);
	}

	public List<String> getChildren(String path) {
		return client.getChildren(path);
	}
	
	public byte[] readData(String path) {
        return client.readData(path);
    }
	
	public boolean isConnected() {
		return isConnected;
	}

	public void doClose() {
		client.close();
	}
	
	public boolean isClose(){
		return closed;
	}
	
	
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		try {
			doClose();
		} catch (Throwable t) {
			logger.warn(t.getMessage(), t);
		}
	}
 
	
	
	
	public List<String> queryProviderData(String providerPath,List<String> providerNodes) {
        List<String> datas = new ArrayList<String>();
        if(providerNodes == null || providerNodes.size() == 0){
            return datas;
        }
        for(String  providerNode : providerNodes){
            try {
                byte[] data = client.readData(providerPath + Constants.PATH_SPLIT +providerNode,true);
                if(data != null){
                    String str = new String(data,"UTF-8");
                    datas.add(str);
                }else{
                    datas.add(providerNode);
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("re error:" + e.getMessage(), e);
            }
        }
        return datas;
    }
	
	public List<String> queryProviderData(List<String> providerNodePaths) {
        List<String> datas = new ArrayList<String>();
        if(providerNodePaths == null || providerNodePaths.size() == 0){
            return datas;
        }
        for(String  providerPath : providerNodePaths){
            try {
                byte[] data = client.readData(providerPath,true);
                if(data != null){
                    String str = new String(data,"UTF-8");
                    datas.add(str);
                }else{
                    datas.add(providerPath);
                }
            } catch (UnsupportedEncodingException e) {
                logger.error("re error:" + e.getMessage(), e);
            }
        }
        return datas;
    }
	
	/**
	 * 增加zk的节点变化监听
	 * @param path
	 * @param listener
	 * @return
	 */
	public List<String> addChildListener(String path, final ChildListener listener) {
		ConcurrentMap<ChildListener, IZkChildListener> zkListeners = childListeners.get(path);
		if(zkListeners == null){
		    childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, IZkChildListener>());
		    zkListeners = childListeners.get(path);
		}
		IZkChildListener zklistener = zkListeners.get(listener);
		if(zklistener == null){
			zklistener = new IZkChildListener() {
				public void handleChildChange(String parentPath, List<String> currentChilds)
						throws Exception {
					listener.childChanged(parentPath, currentChilds);
				}
			};
			zkListeners.putIfAbsent(listener, zklistener);
		}
		return client.subscribeChildChanges(path, zklistener);
	}
	 
	/**
	 * 删除zk的监听
	 * @param path
	 * @param listener
	 * @return
	 */
	
	public void removeChildListener(String path,final ChildListener listener) {
		ConcurrentMap<ChildListener, IZkChildListener> listeners = childListeners.get(path);
		if (listeners != null) {
			IZkChildListener zkListener = listeners.remove(listener);
			if (zkListener != null) {
				client.unsubscribeChildChanges(path,  zkListener);
			}
		}
	}
	
	
	/**
     * 增加zk的数据变化监听
     * @param path
     * @param listener
     * @return
     */
    public void addDataListener(String path, final DataListener listener) {
        ConcurrentMap<DataListener, IZkDataListener> zkListeners = dataListeners.get(path);
        if(zkListeners == null){
            dataListeners.putIfAbsent(path, new ConcurrentHashMap<DataListener, IZkDataListener>());
            zkListeners = dataListeners.get(path);
        }
        IZkDataListener zklistener = zkListeners.get(listener);
        if(zklistener == null){
            zklistener = new IZkDataListener() {
                
                @Override
                public void handleDataChange(String dataPath, byte[] data) throws Exception {
                    listener.dataChange(dataPath, new String(data,"UTF-8"));
                }
                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    listener.dataDeleted(dataPath);
                }
            };
            zkListeners.putIfAbsent(listener, zklistener);
            client.subscribeDataChanges(path, zklistener);
        }
        
    }
	
    /**
     * 删除zk的监听
     * @param path
     * @param listener
     * @return
     */
    public void removeDataListener(String path,final DataListener listener) {
        ConcurrentMap<DataListener, IZkDataListener> listeners = dataListeners.get(path);
        if (listeners != null) {
            IZkDataListener zkListener = listeners.remove(listener);
            if (zkListener != null) {
                client.unsubscribeDataChanges(path, zkListener);
            }
        }
    }
	
	protected void stateChanged(int state) {
		for (StateListener sessionListener : getSessionListeners()) {
			sessionListener.stateChanged(state);
		}
	}
	
	public Set<StateListener> getSessionListeners() {
		return stateListeners;
	}
}
