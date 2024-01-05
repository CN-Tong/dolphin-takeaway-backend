package com.tong.service;

import com.tong.vo.BusinessDataVO;
import com.tong.vo.DishOverViewVO;
import com.tong.vo.OrderOverViewVO;
import com.tong.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {

    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    OrderOverViewVO getOverviewOrders();

    DishOverViewVO getOverviewDishes();

    SetmealOverViewVO getOverviewSetmeals();
}
