package com.service;

import com.domain.biz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by tangcheng on 2017/2/18.
 */
@Service
public class ReviewsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewsService.class);

    @Autowired
    private ReviewsRepository reviewsRepository;

    @Autowired
    private AppsRepository appsRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<String> getVersions(Long app) {
        List<Reviews> reviewsList = reviewsRepository.findByApp(app);
        List<String> list = new ArrayList<>(reviewsList.size());
        for (Reviews reviews : reviewsList) {
            if (list.contains(reviews.getVersion())) {
                continue;
            }
            list.add(reviews.getVersion());
        }
        return list;
    }

    public Map<String, Object> getReviews(ReviewsReq vo) {
        Map<String, Object> map = new HashMap<>();
        map.put("current", vo.getCurrent());
        map.put("rowCount", vo.getRowCount());
        Page<Reviews> page;
        List<Reviews> content = newArrayList();
        String searchPhrase = vo.getSearchPhrase();
        if (StringUtils.hasText(searchPhrase)) {
            long app;
            try {
                app = Long.parseLong(searchPhrase);
            } catch (Exception e) {
                LOGGER.info("searchPhrase:{}", searchPhrase);
                throw new IllegalArgumentException("invalid appId");
            }
            Pageable pageable = new PageRequest(vo.getPageId(), vo.getRowCount());
            page = reviewsRepository.findByAppOrderByRetrievedDateDesc(app, pageable);
            if (vo.getPageId() == 0 && !page.hasContent()) {
                Apps apps = new Apps();
                apps.setId(app);
                long count = appsRepository.count(Example.of(apps));
                if (count == 0) {
                    apps.setEnabled(false);
                    apps.setName("");
                    apps.setIphone(false);
                    apps.setIpad(false);
                    apps.setOsx(false);
                    appsRepository.saveAndFlush(apps);
                } else {
                    LOGGER.info("do nothing.appId:{} already exists.", app);
                }

                //触发调用爬虫
//            http://127.0.0.1:9000/crawler?appid=963065779
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = "http://127.0.0.1:9000/crawler?appid=" + apps.getId();
                            String forObject = restTemplate.getForObject(URI.create(url), String.class);
                            LOGGER.info("invoke spider result:{},appId:{}", forObject, apps.getId());
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            map.put("total", 0);
        } else {
            Pageable all = new PageRequest(vo.getPageId(), vo.getRowCount(), new Sort(Sort.Direction.DESC, "retrievedDate"));
            page = reviewsRepository.findAll(all);
            content = page.getContent();
            map.put("total", page.getTotalElements());
        }

        List<ReviewsRes> resList = new ArrayList<>(content.size());
        for (Reviews reviews : content) {
            ReviewsRes res = new ReviewsRes();
            BeanUtils.copyProperties(reviews, res);
            res.setRetrievedDate(reviews.getRetrievedDate().getTime());
            resList.add(res);
        }

        /**
         * {
         "current": 1,
         "rowCount": 10,
         "rows": [
         {
         "id": 19,
         "sender": "123@test.de",
         "received": "2014-05-30T22:15:00"
         },
         {
         "id": 14,
         "sender": "123@test.de",
         "received": "2014-05-30T20:15:00"
         },
         ...
         ],
         "total": 1123
         }
         */

        map.put("rows", resList);

        return map;
    }
}
