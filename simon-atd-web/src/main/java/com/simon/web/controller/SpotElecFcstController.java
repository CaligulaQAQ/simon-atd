package com.simon.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.simon.common.model.price.dto.BaseDTO;
import com.simon.common.model.price.vo.PriceDetailVO;
import com.simon.common.model.price.vo.PriceDiffVO;
import com.simon.common.model.price.vo.PriceVO;
import com.simon.common.model.search.PageResult;
import com.simon.common.result.Result;
import com.simon.service.price.SpotElecFcstService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author yzy
 * @Date 2023/11/15
 */
@RestController
@RequestMapping("/elec")
public class SpotElecFcstController {

    @Autowired
    private SpotElecFcstService spotElecFcstService;

    @PostMapping("price")
    public Result getPriceCurve(@RequestBody BaseDTO baseDTO) throws IOException {
        List<PriceVO> priceCurve = spotElecFcstService.getPriceCurve(baseDTO);
        return Result.success(priceCurve);
    }

    @PostMapping("price_diff")
    public Result getPriceDiffCurve(@RequestBody BaseDTO baseDTO) throws IOException {
        List<PriceDiffVO> priceDiffCurve = spotElecFcstService.getPriceDiffCurve(baseDTO);
        return Result.success(priceDiffCurve);
    }

    @PostMapping("price_detail")
    public Result getPriceDetail(@RequestBody BaseDTO baseDTO) throws IOException {
        PageResult<PriceDetailVO> priceDetail = spotElecFcstService.getPriceDetail(baseDTO);
        return Result.success(priceDetail);
    }

    @PostMapping("price_download")
    public void downloadPriceDetail(HttpServletResponse response, @RequestBody BaseDTO baseDTO) throws IOException {
        spotElecFcstService.downloadPriceDetail(response, baseDTO);
    }

    @GetMapping("station_list")
    public Result getStationList() throws IOException {
        List<String> stationList = spotElecFcstService.getStationList();
        return Result.success(stationList);
    }
}
