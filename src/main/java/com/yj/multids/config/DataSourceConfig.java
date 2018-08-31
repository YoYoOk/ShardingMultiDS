package com.yj.multids.config;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;
import javax.xml.ws.WebEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;


@Configuration
public class DataSourceConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);
	
	public static String dataSourcesNames = "master,";//master, 加上slave.datasource.names的值  以逗号隔开
	
	@Autowired
    private Environment env;//获取自定义配置
	
	@Value("${spring.datasource.druid.url}")
	private String url;

	@Value("${spring.datasource.druid.username}")
	private String username;

	@Value("${spring.datasource.druid.driver-class-name}")
	private String driverClassName;

	@Value("${spring.datasource.druid.password}")
	private String password;
	
	//通用数据库配置
	@Value("${spring.datasource.druid.initial-size}")
	private String initialSize;

	@Value("${spring.datasource.druid.max-active}")
	private String maxActive;

	@Value("${spring.datasource.druid.min-idle}")
	private String minIdle;

	@Value("${spring.datasource.druid.max-wait}")
	private String maxWait;

	@Value("${spring.datasource.druid.time-between-eviction-runs-millis}")
	private String timeBetweenEvictionRunsMillis;

	@Value("${spring.datasource.druid.min-evictable-idle-time-millis}")
	private String minEvicatableIdleTimeMillis;

	@Value("${spring.datasource.druid.validation-query}")
	private String validationQuery;

	@Value("${spring.datasource.druid.test-while-idle}")
	private String testWhileIdle;
	
	@Value("${spring.datasource.druid.test-on-borrow}")
	private String testOnBorrow;
	
	@Value("${spring.datasource.druid.test-on-return}")
	private String testOnReturn;
	
	@Value("${spring.datasource.druid.remove-abandoned}")
	private String removeAbandoned;
	
	@Value("${spring.datasource.druid.remove-abandoned-timeout-millis}")
	private String removeAbandonedTimeOut;
	
	
	@Primary
	@Bean(name = "shardingDataSource", destroyMethod = "close")
	public DataSource buildDataSource() throws Exception{
		// 设置workID如何生成
		KeyGenerator.initWorkId(KeyGenerator.WORKIDTYPE.IPSECTION_TYPE);//分布式表的主键生成策略
		
		Map<String, DataSource> dsMap =  new HashMap<>();
		dsMap.put("sharding_0", buildShardingDS1());//坑！！！key必须要和数据库名一致
		dsMap.put("sharding_1", buildShardingDS2());
		dsMap.put("master_slave", buildMasterDataSource());
//		dsMap.put("slave_11", buildShardingslaveDS11());
//		dsMap.put("slave_21", buildShardingslaveDS21());
		
		//配置User表规则
		TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration();
		//设置逻辑表
		tableRuleConfiguration.setLogicTable("user");
		//分两个库 12张表
//		String nodeName = String.format("%s.recorddata_${%d..%d}", databaseName, 1, 12);
		tableRuleConfiguration.setActualDataNodes("sharding_${0..1}.user_${1..12}");
		
		//自定义的  分库的规则
		StandardShardingStrategyConfiguration standardDSStrategyConfiguration = new StandardShardingStrategyConfiguration("create_time", 
				DatabasePreciseShardingAlgorithm.class.getName());
		
		//自定义的分片算法实现  分表  第一个参数  根据哪一个字段 做分片  第二个参数 根据哪一种分片策略
		StandardShardingStrategyConfiguration standardTableStrategyConfiguration = new StandardShardingStrategyConfiguration("create_time", 
				TablePreciseShardingAlgorithm.class.getName());
		
		tableRuleConfiguration.setDatabaseShardingStrategyConfig(standardDSStrategyConfiguration);
		tableRuleConfiguration.setTableShardingStrategyConfig(standardTableStrategyConfiguration);
		// 如果设置了@GeneratedValue,则需要设置自增长的主键列
		tableRuleConfiguration.setKeyGeneratorColumnName("id");
		
		// 配置分片规则
		ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
		shardingRuleConfiguration.getTableRuleConfigs().add(tableRuleConfiguration);
		//设置不需要分库的数据源   即 默认的
		shardingRuleConfiguration.setDefaultDatabaseShardingStrategyConfig(null);
		shardingRuleConfiguration.setDefaultDataSourceName("master_slave");
		//TODO 主从库  先暂时搁置 后面研究
//		MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
//		masterSlaveRuleConfig1.setName("ds0");
//		masterSlaveRuleConfig1.setMasterDataSourceName("sharding_0");
//		masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("slave_11"));
//		MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
//		masterSlaveRuleConfig2.setName("ds1");
//		masterSlaveRuleConfig2.setMasterDataSourceName("sharding_1");
//		masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("slave_21"));
//		shardingRuleConfiguration.setMasterSlaveRuleConfigs(Arrays.asList(masterSlaveRuleConfig1,masterSlaveRuleConfig2));
		
		try {
			Properties properties = new Properties();
			return ShardingDataSourceFactory.createDataSource(dsMap, shardingRuleConfiguration,
					new ConcurrentHashMap<String, Object>(), properties);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	//创建分库的两个数据源
//	@Bean(name="shardingDS1")
	public DataSource buildShardingDS1() throws Exception{
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "sharding.datasource.ds1.");
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, propertyResolver.getProperty("url"));//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, propertyResolver.getProperty("username"));//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, propertyResolver.getProperty("password"));//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, propertyResolver.getProperty("driverClassName"));//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
	
//	@Bean(name="shardingDS2")
	public DataSource buildShardingDS2() throws Exception{
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "sharding.datasource.ds2.");
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, propertyResolver.getProperty("url"));//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, propertyResolver.getProperty("username"));//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, propertyResolver.getProperty("password"));//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, propertyResolver.getProperty("driverClassName"));//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
	//创建两个从库的dataSource
	public DataSource buildShardingslaveDS11() throws Exception{
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "slave.datasource.ds1.slave1.");
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, propertyResolver.getProperty("url"));//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, propertyResolver.getProperty("username"));//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, propertyResolver.getProperty("password"));//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, propertyResolver.getProperty("driverClassName"));//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
	public DataSource buildShardingslaveDS21() throws Exception{
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "slave.datasource.ds2.slave1.");
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, propertyResolver.getProperty("url"));//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, propertyResolver.getProperty("username"));//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, propertyResolver.getProperty("password"));//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, propertyResolver.getProperty("driverClassName"));//Driver  数据库驱动
		
		return DruidDataSourceFactory.createDataSource(properties);
	}
	
	private Properties setCustomProperties(){
		Properties properties = new Properties();
		properties.put(DruidDataSourceFactory.PROP_INITIALSIZE, initialSize);//初始化时建立物理连接的个数 默认是0  配置10
		properties.put(DruidDataSourceFactory.PROP_MAXACTIVE, maxActive);//最大连接池数量默认是8  50
		properties.put(DruidDataSourceFactory.PROP_MINIDLE, minIdle);//最小连接池数量 10
		properties.put(DruidDataSourceFactory.PROP_MAXWAIT, maxWait);//获取连接时最大等待时间  如果配置了这个值之后 默认使用公平锁 并发效率会有所下降  可以通过配置userUnfairLock 为true使用非公平锁
		properties.put(DruidDataSourceFactory.PROP_TIMEBETWEENEVICTIONRUNSMILLIS, timeBetweenEvictionRunsMillis);//有两个含义： 1) Destroy线程会检测连接的间隔时间2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
		properties.put(DruidDataSourceFactory.PROP_MINEVICTABLEIDLETIMEMILLIS, minEvicatableIdleTimeMillis);
		properties.put(DruidDataSourceFactory.PROP_VALIDATIONQUERY, validationQuery);//用来检测连接是否有效的sql，要求是一个查询语句  如果此处为null 后面三个不会起作用
		properties.put(DruidDataSourceFactory.PROP_TESTWHILEIDLE, testWhileIdle);
		properties.put(DruidDataSourceFactory.PROP_TESTONBORROW, testOnBorrow);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONED, removeAbandoned);
		properties.put(DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT, removeAbandonedTimeOut);
		properties.put(DruidDataSourceFactory.PROP_TESTONRETURN, testOnReturn);
		return properties;
	}
	
	
//	@Bean("targetDataSources")
//	public HashMap<String, DataSource>  buildTargetDataSources() throws Exception{
//		HashMap<String, DataSource> targetDataSources = new HashMap<>();
//		DataSource masterDS = buildMasterDataSource();
//		((DruidDataSource)masterDS).setBreakAfterAcquireFailure(true);
//		targetDataSources.put("master", masterDS);
//		targetDataSources.putAll(buildSlaveDataSources());
//		return targetDataSources;
//	}
//	
//	@Primary
//	@Bean(name="dynamicDataSource")
//	@Qualifier("dyanmicDataSource")
//	public DataSource dynamicDataSource() throws Exception{
//		DynamicDataSource dynamicDataSource = new DynamicDataSource();
//		HashMap<String, DataSource> targetDataSources = buildTargetDataSources();
//		//首先配置默认的数据源
//		dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get("master"));
////		DataSourceContextHolder.setDataSource(ContextConst.DataSourceType.FIRST.name());//首先直接写第一个
//		//配置多数据源   
//		HashMap<Object, Object> dataSourceMap = new HashMap<>();
//		for (String key : targetDataSources.keySet()) {
//			dataSourceMap.put(key, targetDataSources.get(key));
//		}
//		dynamicDataSource.setTargetDataSources(dataSourceMap);
//		return dynamicDataSource;
//	}
//	
//	
	private DataSource buildMasterDataSource() throws Exception{
		Properties properties = setCustomProperties();
		properties.put(DruidDataSourceFactory.PROP_URL, url);//数据库url
		properties.put(DruidDataSourceFactory.PROP_USERNAME, username);//用户名
		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
		properties.put(DruidDataSourceFactory.PROP_PASSWORD, password);//密码
		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, driverClassName);//Driver  数据库驱动

		return DruidDataSourceFactory.createDataSource(properties);
	}
//	
//	private HashMap<String, DataSource> buildSlaveDataSources() throws Exception{
//		 // 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
//        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "slave.datasource.");
//        String dsPrefixs = propertyResolver.getProperty("names");
//        dataSourcesNames += dsPrefixs;
//        LOGGER.info("dataSourcesNames: " + dataSourcesNames);
//        HashMap<String, DataSource> slaveDataSources = new HashMap<>();
//        for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
//            Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
//            DataSource slaveDataSource = buildDataSource(dsMap);
//            slaveDataSources.put(dsPrefix, slaveDataSource);
//        }
//        return slaveDataSources;
//	}
//
//	
//	public DataSource buildDataSource(Map<String, Object> dsMap) throws Exception {
//		Properties properties = setCustomProperties();
//		properties.put(DruidDataSourceFactory.PROP_URL, dsMap.get("url").toString());//数据库url
//		properties.put(DruidDataSourceFactory.PROP_USERNAME, dsMap.get("username").toString());//用户名
//		// properties.put(DruidDataSourceFactory.PROP_PASSWORD,
//		// ConfigTools.decrypt(publicKey,mysqlUserPwd));
//		properties.put(DruidDataSourceFactory.PROP_PASSWORD, dsMap.get("password").toString());//密码
//		properties.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, dsMap.get("driverClassName").toString());//Driver  数据库驱动
//
//		return DruidDataSourceFactory.createDataSource(properties);
//	}
	
}
