package com.yj.multids.config;

import java.util.Collection;

import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.standard.RangeShardingAlgorithm;

public class MyRangeShardingAlgorithm implements RangeShardingAlgorithm<String> {

	@Override
	public Collection<String> doSharding(Collection<String> availableTargetNames,
			RangeShardingValue<String> shardingValue) {
		
		return null;
	}

}
