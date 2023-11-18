package com.simon.service.price;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.simon.common.model.price.dto.BaseDTO;
import com.simon.common.model.price.vo.PriceDetailVO;
import com.simon.common.model.price.vo.PriceDiffVO;
import com.simon.common.model.price.vo.PriceVO;
import com.simon.common.model.search.PageResult;

/**
 * @Author yzy
 * @Date 2023/11/14
 */
public interface SpotElecFcstService {
    public List<PriceVO> getPriceCurve(BaseDTO baseDTO) throws IOException;

    public List<PriceDiffVO> getPriceDiffCurve(BaseDTO baseDTO) throws IOException;

    public PageResult<PriceDetailVO> getPriceDetail(BaseDTO baseDTO) throws IOException;

    public String downloadPriceDetail(BaseDTO baseDTO);

    public void downloadPriceDetail(HttpServletResponse response, BaseDTO baseDTO) throws IOException;

    public List<String> getStationList() throws IOException;
}
