package com.web.review;

import com.domain.biz.ReviewsReq;
import com.service.ReviewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by tangcheng on 2017/2/18.
 */
@RestController
@RequestMapping(value = "v1")
public class ReviewsController {

    @Autowired
    private ReviewsService reviewsService;

    @RequestMapping(value = "getVersions", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getVersions(@RequestParam(value = "app") Long app) {
        List<String> versions = reviewsService.getVersions(app);
        return ResponseEntity.ok(versions);
    }

    @Transactional
    @RequestMapping(value = "getReviews", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getReviews(@ModelAttribute ReviewsReq vo) {
        Map<String, Object> reviews = reviewsService.getReviews(vo);
        return ResponseEntity.ok(reviews);
    }

}
