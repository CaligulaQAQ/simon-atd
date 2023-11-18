package com.simon.service.price.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.simon.common.model.price.dto.BaseDTO;
import com.simon.common.model.price.vo.PriceDetailVO;
import com.simon.common.model.price.vo.PriceDiffVO;
import com.simon.common.model.price.vo.PriceVO;
import com.simon.common.model.search.PageResult;
import com.simon.common.util.DateUtils;
import com.simon.common.util.RouteUtils;
import com.simon.service.manager.ReadManager;
import com.simon.service.price.SpotElecFcstService;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

/**
 * @Author yzy
 * @Date 2023/11/14
 */
@Service
public class SpotElecFcstServiceImpl implements SpotElecFcstService {
    // todo 把15分钟的因素考虑进去
    @Override
    public List<PriceVO> getPriceCurve(BaseDTO baseDTO) throws IOException {
        // todo 考虑下缺数据的情况
        List<String> dateList = DateUtils.getDateList(baseDTO.getStartDate(), baseDTO.getEndDate());
        List<PriceVO> priceVOLists = Lists.newArrayList();
        for (String date : dateList) {
            List<Long> dateTimeList = DateUtils.getDateTimeList(date);
            List<BigDecimal> list = ReadManager.readActualRealPrice(date, baseDTO.getStation());
            List<BigDecimal> list1 = ReadManager.readActualAheadPrice(date, baseDTO.getStation());
            List<BigDecimal> list2 = ReadManager.readPredictRealPrice(date);
            List<BigDecimal> list3 = ReadManager.readPredictAheadPrice(date);
            List<PriceVO> priceVOList = Lists.newArrayList();
            int k = 0;
            int j = 1;
            if (baseDTO.getIsFifteen() == 1) {
                k = 3;
                j = 4;
            }
            for (int i = k; i < 96; i = i + j) {
                PriceVO priceVO = PriceVO.builder()
                    .timestamp(dateTimeList.get(i))
                    .actualAheadPrice(list1.get(i))
                    .actualRealPrice(list.get(i))
                    .predictAheadPrice(list3.get(i))
                    .predictRealPrice(list2.get(i)).build();
                priceVOList.add(priceVO);
            }
            priceVOLists.addAll(priceVOList);
        }
        return priceVOLists;
    }

    @Override
    public List<PriceDiffVO> getPriceDiffCurve(BaseDTO baseDTO) throws IOException {
        List<PriceVO> curve = getPriceCurve(baseDTO);
        List<PriceDiffVO> priceDiffVOS = Lists.newArrayList();
        for (PriceVO priceVO : curve) {
            BigDecimal predictDiff = priceVO.getPredictAheadPrice().subtract(priceVO.getPredictRealPrice()).abs();
            BigDecimal actualDiff = priceVO.getActualAheadPrice().subtract(priceVO.getActualRealPrice()).abs();
            // todo 不清楚置信度公式
            BigDecimal confidence = predictDiff.multiply(new BigDecimal(100))
                .divide(priceVO.getPredictAheadPrice().abs(), 2, RoundingMode.HALF_UP);
            priceDiffVOS.add(new PriceDiffVO(priceVO.getTimestamp(), predictDiff, actualDiff, confidence));
        }
        return priceDiffVOS;
    }

    @Override
    public PageResult<PriceDetailVO> getPriceDetail(BaseDTO baseDTO) throws IOException {
        List<PriceVO> curve = getPriceCurve(baseDTO);
        List<PriceDiffVO> priceDiffCurve = getPriceDiffCurve(baseDTO);
        int startSize = (baseDTO.getPageNo() - 1) * baseDTO.getPageSize();
        int endSize = startSize + baseDTO.getPageSize();
        List<PriceDetailVO> priceDetailVOList = Lists.newArrayList();
        for (int i = startSize; i < endSize; i++) {
            PriceDetailVO build = PriceDetailVO.builder()
                .timestamp(curve.get(i).getTimestamp())
                .actualAheadPrice(curve.get(i).getActualAheadPrice())
                .actualRealPrice(curve.get(i).getActualRealPrice())
                .predictAheadPrice(curve.get(i).getPredictAheadPrice())
                .predictRealPrice(curve.get(i).getPredictRealPrice())
                .actualPriceDiff(priceDiffCurve.get(i).getActualPriceDiff())
                .predictPriceDiff(priceDiffCurve.get(i).getPredictPriceDiff())
                .confidence(priceDiffCurve.get(i).getConfidence()).build();
            priceDetailVOList.add(build);
        }
        return new PageResult<>(priceDetailVOList, baseDTO.getPageSize(), baseDTO.getPageNo(),
            curve.size());
    }

    @Override
    public String downloadPriceDetail(BaseDTO baseDTO) {
        return null;
    }

    @Override
    public void downloadPriceDetail(HttpServletResponse response, BaseDTO baseDTO) throws IOException {
        List<String> header = CollUtil.newArrayList("数据时间", "预测日前价格（元/MWh）", "预测实时价格（元/MWh）",
            "预测价差（元/MWh）", "置信度（%）", "实际日前价格（元/MWh）", "实际实时价格（元/MWh）", "实际价差（元/MWh）");
        List<PriceVO> curve = getPriceCurve(baseDTO);
        List<PriceDiffVO> priceDiffCurve = getPriceDiffCurve(baseDTO);
        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        for (int i = 0; i < curve.size(); i++) {
            List<String> row = CollUtil.newArrayList(
                DateUtils.format3(curve.get(i).getTimestamp()), curve.get(i).getPredictAheadPrice().toString(),
                curve.get(i).getPredictRealPrice().toString(), priceDiffCurve.get(i).getPredictPriceDiff().toString(),
                priceDiffCurve.get(i).getConfidence().toString(),
                curve.get(i).getActualAheadPrice().toString(), curve.get(i).getActualRealPrice().toString(),
                priceDiffCurve.get(i).getActualPriceDiff().toString());
            rows.add(row);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=price.xls");
        ExcelWriter writer = ExcelUtil.getWriter();
        writer.write(rows, true);
        writer.flush(response.getOutputStream());
        writer.close();
    }

    @Override
    public List<String> getStationList() throws IOException {
        return FileUtils.readLines(new File(RouteUtils.getWholePath("场站列表.txt")),
            StandardCharsets.UTF_8);
    }
}
