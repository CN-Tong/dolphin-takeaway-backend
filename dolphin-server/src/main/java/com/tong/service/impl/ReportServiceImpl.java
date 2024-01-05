package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.dto.GoodsSalesDTO;
import com.tong.entity.OrderDetail;
import com.tong.entity.Orders;
import com.tong.entity.User;
import com.tong.service.OrderDetailService;
import com.tong.service.OrderService;
import com.tong.service.ReportService;
import com.tong.service.UserService;
import com.tong.vo.OrderReportVO;
import com.tong.vo.SalesTop10ReportVO;
import com.tong.vo.TurnoverReportVO;
import com.tong.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        // 获取begin到end之间的日期集合
        List<LocalDate> dateList = getdateList(begin, end);
        // List转String
        String dateStr = StringUtils.join(dateList, ',');
        // 放入turnoverReportVO
        turnoverReportVO.setDateList(dateStr);
        // 定义集合存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        // 查询date对应的营业额数据，即状态为已完成的订单数
        dateList.forEach(localDate -> {
            LocalDateTime localDateBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            List<Orders> ordersList = orderService.lambdaQuery()
                    .eq(Orders::getStatus, Orders.COMPLETED)
                    .ge(Orders::getOrderTime, localDateBeginTime)
                    .le(Orders::getOrderTime, localDateEndTime)
                    .list();
            if (CollUtil.isEmpty(ordersList)) {
                turnoverList.add(0.0);
            } else {
                Double turnover = ordersList.stream().mapToDouble(orders -> orders.getAmount().doubleValue()).sum();
                turnoverList.add(turnover);
            }
        });
        // List转String
        String turnoverStr = StringUtils.join(turnoverList, ',');
        // 放入turnoverReportVO
        turnoverReportVO.setTurnoverList(turnoverStr);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        // 获取begin到end之间的日期集合
        List<LocalDate> dateList = getdateList(begin, end);
        // List转String
        String dateStr = StringUtils.join(dateList, ',');
        // 放入userReportVO
        userReportVO.setDateList(dateStr);
        List<Long> totalUserCountList = new ArrayList<>();
        List<Long> newUserCountList = new ArrayList<>();
        dateList.forEach(localDate -> {
            LocalDateTime localDateBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            // 查询date对应的新增用户量
            Long newUserCount = userService.lambdaQuery()
                    .ge(User::getCreateTime, localDateBeginTime)
                    .le(User::getCreateTime, localDateEndTime)
                    .count();
            newUserCount = newUserCount == null ? 0 : newUserCount;
            newUserCountList.add(newUserCount);
            // 查询date对应的用户总量
            Long totalUserCount = userService.lambdaQuery()
                    .le(User::getCreateTime, localDateEndTime)
                    .count();
            totalUserCount = totalUserCount == null ? 0 : totalUserCount;
            totalUserCountList.add(totalUserCount);
        });
        // List转String
        String totalUserCountStr = StringUtils.join(totalUserCountList, ',');
        String newUserCountStr = StringUtils.join(newUserCountList, ',');
        // 封装UserReportVO
        userReportVO.setNewUserList(newUserCountStr);
        userReportVO.setTotalUserList(totalUserCountStr);
        return userReportVO;
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        // 获取begin到end之间的日期集合
        List<LocalDate> dateList = getdateList(begin, end);
        // List转String
        String dateStr = StringUtils.join(dateList, ',');
        orderReportVO.setDateList(dateStr);
        // 统计每天订单总数和有效订单数
        List<Long> totalOrdersList = new ArrayList<>();
        List<Long> validOrdersList = new ArrayList<>();
        dateList.forEach(localDate -> {
            LocalDateTime localDateBeginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime localDateEndTime = LocalDateTime.of(localDate, LocalTime.MAX);
            // 统计每天订单总数
            Long totalOrdersCount = orderService.lambdaQuery()
                    .ge(Orders::getOrderTime, localDateBeginTime)
                    .le(Orders::getOrderTime, localDateEndTime)
                    .count();
            totalOrdersCount = totalOrdersCount == null ? 0 : totalOrdersCount;
            totalOrdersList.add(totalOrdersCount);
            Long validOrdersCount = orderService.lambdaQuery()
                    .eq(Orders::getStatus, Orders.COMPLETED)
                    .ge(Orders::getOrderTime, localDateBeginTime)
                    .le(Orders::getOrderTime, localDateEndTime)
                    .count();
            validOrdersCount = validOrdersCount == null ? 0 : validOrdersCount;
            validOrdersList.add(validOrdersCount);
        });
        // List转String
        String totalOrdersCountStr = StringUtils.join(totalOrdersList, ',');
        String validOrdersCountStr = StringUtils.join(validOrdersList, ',');
        // 计算订单总数和有效订单总数
        Long totalOrdersCount = totalOrdersList.stream().reduce(Long::sum).get();
        Long validOrdersCount = validOrdersList.stream().reduce(Long::sum).get();
        if (totalOrdersCount != 0) {
            // 计算订单完成率
            Double orderCompletionRate = validOrdersCount.doubleValue() / totalOrdersCount;
            orderReportVO.setOrderCompletionRate(orderCompletionRate);
        }
        // 封装orderReportVO
        orderReportVO.setOrderCountList(totalOrdersCountStr);
        orderReportVO.setValidOrderCountList(validOrdersCountStr);
        orderReportVO.setTotalOrderCount(totalOrdersCount.intValue());
        orderReportVO.setValidOrderCount(validOrdersCount.intValue());
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);
        // 查询order表中状态为已完成，在指定时间内的订单id
        List<Orders> ordersList = orderService.lambdaQuery()
                .eq(Orders::getStatus, Orders.COMPLETED)
                .ge(Orders::getOrderTime, beginDateTime)
                .le(Orders::getOrderTime, endDateTime)
                .list();
        List<Long> orderIds = new ArrayList<>();
        ordersList.forEach(orders -> orderIds.add(orders.getId()));
        // 查询order_detail表
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<>();
        Page<OrderDetail> page = Page.of(0, 10);
        wrapper.select("name", "count(number) as number")
                .in("order_id", orderIds)
                .groupBy("name");
        Page<OrderDetail> p = orderDetailService.page(page, wrapper);
        List<OrderDetail> orderDetailList = p.getRecords();
        // 封装GoodsSalesDTO
        List<GoodsSalesDTO> goodsSalesDTOList = BeanUtil.copyToList(orderDetailList, GoodsSalesDTO.class);
        // 封装SalesTop10ReportVO
        StringJoiner nameSJ = new StringJoiner(",");
        StringJoiner numberSj = new StringJoiner(",");
        goodsSalesDTOList.forEach(goodsSalesDTO -> {
            String name = goodsSalesDTO.getName();
            String number = goodsSalesDTO.getNumber().toString();
            nameSJ.add(name);
            numberSj.add(number);
        });
        String nameStr = nameSJ.toString();
        String numberStr = numberSj.toString();
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(nameStr);
        salesTop10ReportVO.setNumberList(numberStr);
        return salesTop10ReportVO;
    }

    /**
     * 返回begin到end之间的日期集合
     */
    private List<LocalDate> getdateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
