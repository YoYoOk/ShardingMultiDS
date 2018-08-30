package com.yj.multids.config;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import io.shardingjdbc.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;

/**
 * 数据库表分片算法
 * @author LiaoYao
 * @date：2018年8月30日
 */
public class TablePreciseShardingAlgorithm implements PreciseShardingAlgorithm<Date> {

	@Override
	public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
		if (shardingValue.getColumnName().compareToIgnoreCase("create_time") == 0) {//shardingValue 有三个值 逻辑表名  字段  value
			Date value = shardingValue.getValue();
			Object[] arrTagetName = availableTargetNames.toArray();//得到表名列表

//			@SuppressWarnings("deprecation")//该方法已废弃 使用Calendar代替
//			int index = value.getMonth();//按照月分   12个月 不同月 放入不同表中
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(value);
			String result =  (String) arrTagetName[calendar.get(Calendar.MONTH)];//按照索引找到这个表名
			System.err.println("pre table:"+result);
			return result;//返回的是表名

		}
		System.err.println("col name :"+shardingValue.getColumnName()+"is not find");;
		return null;
	}


}
