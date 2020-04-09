package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.RatingScale;
import com.westernacher.internal.feedback.repository.RatingScaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/ratingScale")
public class RatingScaleController {

    @Autowired
    private RatingScaleRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public List<RatingScale> getAll () {

        List<RatingScale> ratingScales = repository.findAll();

        Collections.sort(ratingScales, new RatingSorter());

        return ratingScales;
    }

    @RequestMapping(method = RequestMethod.POST)
    public void saveRatingScale (@RequestBody List<RatingScale> ratingScales) {
        repository.saveAll(ratingScales);
    }
}

class RatingSorter implements Comparator<RatingScale> {
    @Override
    public int compare(RatingScale o1, RatingScale o2) {
        return extractInt(o1.getRating()) - extractInt(o2.getRating());
    }

    int extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        // return 0 if no digits found
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}


