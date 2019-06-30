package org.maples.serinus.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.maples.serinus.model.SerinusStrategy;
import org.maples.serinus.repository.SerinusStrategyMapper;
import org.maples.serinus.utility.SerinusHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StrategyService {

    @Autowired
    private SerinusStrategyMapper strategyMapper;

    @Transactional
    public SerinusStrategy getStrategyByUUID(String uuid) {
        SerinusStrategy strategy = strategyMapper.selectByPrimaryKey(uuid);

        if (strategy != null) {
            strategy.setFilter(SerinusHelper.base64Decode(strategy.getFilter()));
            strategy.setContent(SerinusHelper.base64Decode(strategy.getContent()));
        }

        return strategy;
    }

    @Transactional
    public void saveStrategy(SerinusStrategy strategy) {
        if (StringUtils.isAnyBlank(strategy.getProduct(), strategy.getTitle())) {
            return;
        }

        JSONObject filter = JSON.parseObject(strategy.getFilter());
        JSONObject content = JSON.parseObject(strategy.getContent());

        log.info("Save strategy\n{}", JSON.toJSONString(strategy, true));
        log.info("Filter = \n{}", JSON.toJSONString(filter, true));
        log.info("Content = \n{}", JSON.toJSONString(content, true));

        strategy.setFilter(SerinusHelper.base64Encode(filter.toJSONString()));
        strategy.setContent(SerinusHelper.base64Encode(content.toJSONString()));

        if (!StringUtils.isEmpty(strategy.getUuid())) {
            // Confirm that an object with same id existing in database;
            SerinusStrategy dbObject = strategyMapper.selectByPrimaryKey(strategy.getUuid());
            if (dbObject != null) {
                // Confirm that two object has same title and product
                boolean identity = dbObject.getProduct().equals(strategy.getProduct());
                identity = identity && dbObject.getTitle().equals(strategy.getTitle());
                if (identity) {
                    log.info("Update strategy, id = {}", strategy.getUuid());
                    BeanUtils.copyProperties(strategy, dbObject);
                    strategy.setEnabled(false);
                    strategyMapper.updateByPrimaryKey(dbObject);
                    return;
                }
            }
        }

        strategy.setUuid(SerinusHelper.generateUUID());
        int order = strategyMapper.selectCountByProduct(strategy.getProduct());
        strategy.setOrderInProduct(order);
        strategy.setEnabled(false);

        log.info("Insert new strategy, id = {}", strategy.getUuid());
        strategyMapper.insert(strategy);
    }

    @Transactional
    public Map<String, Map<String, List<SerinusStrategy>>> getSerinusStrategyMap() {
        Map<String, Map<String, List<SerinusStrategy>>> result = new HashMap<>();
        List<SerinusStrategy> serinusStrategies = strategyMapper.selectAll();

        for (SerinusStrategy strategy : serinusStrategies) {
            result.putIfAbsent(strategy.getProduct(), new HashMap<>());
            Map<String, List<SerinusStrategy>> group = result.get(strategy.getProduct());

            group.putIfAbsent(strategy.getTitle(), new ArrayList<>());
            List<SerinusStrategy> strategies = group.get(strategy.getTitle());

            strategy.setFilter(SerinusHelper.base64Decode(strategy.getFilter()));
            strategy.setContent(SerinusHelper.base64Decode(strategy.getContent()));
            strategies.add(strategy);
        }

        return result;
    }

    @Transactional
    public Map<String, List<SerinusStrategy>> getSerinusStrategyList() {
        Map<String, List<SerinusStrategy>> result = new HashMap<>();
        List<SerinusStrategy> serinusStrategies = strategyMapper.selectAll();

        for (SerinusStrategy strategy : serinusStrategies) {
            result.putIfAbsent(strategy.getProduct(), new ArrayList<>());
            List<SerinusStrategy> strategies = result.get(strategy.getProduct());

            strategy.setFilter(SerinusHelper.base64Decode(strategy.getFilter()));
            strategy.setContent(SerinusHelper.base64Decode(strategy.getContent()));
            strategies.add(strategy);
        }

        return result;
    }

    public List<SerinusStrategy> getSerinusStrategiesByProduct(String product) {
        return strategyMapper.selectAllEnabledByProduct(product);
    }
}
