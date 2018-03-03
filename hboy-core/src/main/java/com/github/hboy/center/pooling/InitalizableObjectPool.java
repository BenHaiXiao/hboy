package com.github.hboy.center.pooling;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GenericObjectPool的改进版。
 * 增加了以下功能：
 * 1.如果minIdle > 0, 那么在创建池的时候，会在池里面保存minIdle个初始对象。
 * 2.实现了 Inspectable， 可以用于诊断池是否工作正常，适用于连接池。
 * @author xiaobenhai
 * Date: 2016/3/16
 * Time: 19:12
 */
public class InitalizableObjectPool extends GenericObjectPool<Object> implements Inspectable {
    private static Logger log = LoggerFactory.getLogger(InitalizableObjectPool.class);

    public InitalizableObjectPool() {
	this(null, DEFAULT_MAX_ACTIVE, DEFAULT_WHEN_EXHAUSTED_ACTION, DEFAULT_MAX_WAIT, DEFAULT_MAX_IDLE,
		DEFAULT_MIN_IDLE, DEFAULT_TEST_ON_BORROW, DEFAULT_TEST_ON_RETURN,
		DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
		DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>AutoInitObjectPool</tt> using the specified factory.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory) {
	this(factory, DEFAULT_MAX_ACTIVE, DEFAULT_WHEN_EXHAUSTED_ACTION, DEFAULT_MAX_WAIT, DEFAULT_MAX_IDLE,
		DEFAULT_MIN_IDLE, DEFAULT_TEST_ON_BORROW, DEFAULT_TEST_ON_RETURN,
		DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
		DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param config
     *            a non-<tt>null</tt> {@link InitalizableObjectPool.Config}
     *            describing my configuration
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, Config config) {
	this(factory, config.maxActive, config.whenExhaustedAction, config.maxWait, config.maxIdle, config.minIdle,
		config.testOnBorrow, config.testOnReturn, config.timeBetweenEvictionRunsMillis,
		config.numTestsPerEvictionRun, config.minEvictableIdleTimeMillis, config.testWhileIdle,
		config.softMinEvictableIdleTimeMillis, config.lifo);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed from me at
     *            one time (see {@link #setMaxActive})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive) {
	this(factory, maxActive, DEFAULT_WHEN_EXHAUSTED_ACTION, DEFAULT_MAX_WAIT, DEFAULT_MAX_IDLE, DEFAULT_MIN_IDLE,
		DEFAULT_TEST_ON_BORROW, DEFAULT_TEST_ON_RETURN, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
		DEFAULT_NUM_TESTS_PER_EVICTION_RUN, DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed from me at
     *            one time (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #getWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted an and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #getMaxWait})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait) {
	this(factory, maxActive, whenExhaustedAction, maxWait, DEFAULT_MAX_IDLE, DEFAULT_MIN_IDLE,
		DEFAULT_TEST_ON_BORROW, DEFAULT_TEST_ON_RETURN, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
		DEFAULT_NUM_TESTS_PER_EVICTION_RUN, DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #getWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted an and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #getMaxWait})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #getTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #getTestOnReturn}
     *            )
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    boolean testOnBorrow, boolean testOnReturn) {
	this(factory, maxActive, whenExhaustedAction, maxWait, DEFAULT_MAX_IDLE, DEFAULT_MIN_IDLE, testOnBorrow,
		testOnReturn, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
		DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #getWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #getMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #getMaxIdle})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle) {
	this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, DEFAULT_MIN_IDLE, DEFAULT_TEST_ON_BORROW,
		DEFAULT_TEST_ON_RETURN, DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
		DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #getWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #getMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #getMaxIdle})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #getTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #getTestOnReturn}
     *            )
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle, boolean testOnBorrow, boolean testOnReturn) {
	this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, DEFAULT_MIN_IDLE, testOnBorrow, testOnReturn,
		DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS, DEFAULT_NUM_TESTS_PER_EVICTION_RUN,
		DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS, DEFAULT_TEST_WHILE_IDLE);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #setWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #setMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #setMaxIdle})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #setTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #setTestOnReturn}
     *            )
     * @param timeBetweenEvictionRunsMillis
     *            the amount of time (in milliseconds) to sleep between
     *            examining idle objects for eviction (see
     *            {@link #setTimeBetweenEvictionRunsMillis})
     * @param numTestsPerEvictionRun
     *            the number of idle objects to examine per run within the idle
     *            object eviction thread (if any) (see
     *            {@link #setNumTestsPerEvictionRun})
     * @param minEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction (see
     *            {@link #setMinEvictableIdleTimeMillis})
     * @param testWhileIdle
     *            whether or not to validate objects in the idle object eviction
     *            thread, if any (see {@link #setTestWhileIdle})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis,
	    int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle) {
	this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, DEFAULT_MIN_IDLE, testOnBorrow, testOnReturn,
		timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #setWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #setMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #setMaxIdle})
     * @param minIdle
     *            the minimum number of idle objects in my pool (see
     *            {@link #setMinIdle})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #setTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #setTestOnReturn}
     *            )
     * @param timeBetweenEvictionRunsMillis
     *            the amount of time (in milliseconds) to sleep between
     *            examining idle objects for eviction (see
     *            {@link #setTimeBetweenEvictionRunsMillis})
     * @param numTestsPerEvictionRun
     *            the number of idle objects to examine per run within the idle
     *            object eviction thread (if any) (see
     *            {@link #setNumTestsPerEvictionRun})
     * @param minEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction (see
     *            {@link #setMinEvictableIdleTimeMillis})
     * @param testWhileIdle
     *            whether or not to validate objects in the idle object eviction
     *            thread, if any (see {@link #setTestWhileIdle})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle, int minIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis,
	    int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle) {
	this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, minIdle, testOnBorrow, testOnReturn,
		timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle,
		DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #setWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #setMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #setMaxIdle})
     * @param minIdle
     *            the minimum number of idle objects in my pool (see
     *            {@link #setMinIdle})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #setTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #setTestOnReturn}
     *            )
     * @param timeBetweenEvictionRunsMillis
     *            the amount of time (in milliseconds) to sleep between
     *            examining idle objects for eviction (see
     *            {@link #setTimeBetweenEvictionRunsMillis})
     * @param numTestsPerEvictionRun
     *            the number of idle objects to examine per run within the idle
     *            object eviction thread (if any) (see
     *            {@link #setNumTestsPerEvictionRun})
     * @param minEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction (see
     *            {@link #setMinEvictableIdleTimeMillis})
     * @param testWhileIdle
     *            whether or not to validate objects in the idle object eviction
     *            thread, if any (see {@link #setTestWhileIdle})
     * @param softMinEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction with the extra
     *            condition that at least "minIdle" amount of object remain in
     *            the pool. (see {@link #setSoftMinEvictableIdleTimeMillis})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle, int minIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis,
	    int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle,
	    long softMinEvictableIdleTimeMillis) {
	this(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, minIdle, testOnBorrow, testOnReturn,
		timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle,
		softMinEvictableIdleTimeMillis, DEFAULT_LIFO);
    }

    /**
     * Create a new <tt>InitalizableObjectPool</tt> using the specified values.
     * 
     * @param factory
     *            the (possibly <tt>null</tt>)PoolableObjectFactory to use to
     *            create, validate and destroy objects
     * @param maxActive
     *            the maximum number of objects that can be borrowed at one time
     *            (see {@link #setMaxActive})
     * @param whenExhaustedAction
     *            the action to take when the pool is exhausted (see
     *            {@link #setWhenExhaustedAction})
     * @param maxWait
     *            the maximum amount of time to wait for an idle object when the
     *            pool is exhausted and <i>whenExhaustedAction</i> is
     *            {@link #WHEN_EXHAUSTED_BLOCK} (otherwise ignored) (see
     *            {@link #setMaxWait})
     * @param maxIdle
     *            the maximum number of idle objects in my pool (see
     *            {@link #setMaxIdle})
     * @param minIdle
     *            the minimum number of idle objects in my pool (see
     *            {@link #setMinIdle})
     * @param testOnBorrow
     *            whether or not to validate objects before they are returned by
     *            the {@link #borrowObject} method (see {@link #setTestOnBorrow}
     *            )
     * @param testOnReturn
     *            whether or not to validate objects after they are returned to
     *            the {@link #returnObject} method (see {@link #setTestOnReturn}
     *            )
     * @param timeBetweenEvictionRunsMillis
     *            the amount of time (in milliseconds) to sleep between
     *            examining idle objects for eviction (see
     *            {@link #setTimeBetweenEvictionRunsMillis})
     * @param numTestsPerEvictionRun
     *            the number of idle objects to examine per run within the idle
     *            object eviction thread (if any) (see
     *            {@link #setNumTestsPerEvictionRun})
     * @param minEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction (see
     *            {@link #setMinEvictableIdleTimeMillis})
     * @param testWhileIdle
     *            whether or not to validate objects in the idle object eviction
     *            thread, if any (see {@link #setTestWhileIdle})
     * @param softMinEvictableIdleTimeMillis
     *            the minimum number of milliseconds an object can sit idle in
     *            the pool before it is eligible for eviction with the extra
     *            condition that at least "minIdle" amount of object remain in
     *            the pool. (see {@link #setSoftMinEvictableIdleTimeMillis})
     * @param lifo
     *            whether or not objects are returned in last-in-first-out order
     *            from the idle object pool (see {@link #setLifo})
     */
    public InitalizableObjectPool(PoolableObjectFactory<Object> factory, int maxActive, byte whenExhaustedAction, long maxWait,
	    int maxIdle, int minIdle, boolean testOnBorrow, boolean testOnReturn, long timeBetweenEvictionRunsMillis,
	    int numTestsPerEvictionRun, long minEvictableIdleTimeMillis, boolean testWhileIdle,
	    long softMinEvictableIdleTimeMillis, boolean lifo) {
	super(factory, maxActive, whenExhaustedAction, maxWait, maxIdle, minIdle, testOnBorrow, testOnReturn,
		timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, minEvictableIdleTimeMillis, testWhileIdle,
		softMinEvictableIdleTimeMillis, lifo);

	initPool();
    }

    /**
     * 初始化池。 如果池中设置了minIdle, 那么初始化池的时候，需要把这些idle object创建起来。 Common Pooling
     * 的Generic Object Pool 虽然有 minIdle但是并不起效（或者说初始化的时候不起效）。
     */
    protected void initPool() {
	int toCreate = getMinIdle() - getNumIdle();
	if (toCreate > 0) {
	    log.debug("trying to initialize " + toCreate + " idel objects");
	}

	while (toCreate > 0) {
	    toCreate--;

	    try {
		Object obj = borrowObject();
		returnObject(obj);
	    } catch (Exception e) {
		log.error("error occurs while initialize default idle object", e);
	    }
	}
    }

    /**
     * 检测对象池是否工作正常。判断方法：
     * 如果NumActive> 0 或者NumIdle>0 那么对象池正常。
     * 如果没有Active 和idle, 那么能borrow出对象，那么池也算正常。
     */
    public boolean isAlive() {
	if (this.getNumActive() > 0 || this.getNumIdle() > 0 ){
	    return true;
	} else {
	    try {
		Object obj = this.borrowObject();
		this.returnObject(obj);
		
		return true;
	    } catch (Exception e) {
		log.debug("pool is not alive", e);
		return false;
	    }
	}
    }

}
