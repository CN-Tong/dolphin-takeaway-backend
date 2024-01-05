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
import com.tong.service.*;
import com.tong.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkSpaceService workSpaceService;

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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库，获取近30日营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin, end);
        // 获取模板文件的输入流
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("template/BusinessDataTemplate.xlsx");
        // 通过POI将数据写出到Excel文件中
        try {
            // 填充概览数据
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            // 填充第2行第2列-时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            // 填充第4行第3列-营业额
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            // 填充第4行第5列-订单完成率
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            // 填充第4行第7列-新增用户数
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            // 填充第5行第3列-有效订单数
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            // 填充第5行第5列-平均客单价
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());
            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                // 填充时间
                LocalDate date = dateBegin.plusDays(i);
                XSSFRow row = sheet.getRow(i + 7);
                row.getCell(1).setCellValue(date.toString());
                // 查询某一天的运营数据
                BusinessDataVO businessData = workSpaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                if (businessData == null) {
                    continue;
                }
                Double turnover = businessData.getTurnover();
                Integer validOrderCount = businessData.getValidOrderCount();
                Double orderCompletionRate = businessData.getOrderCompletionRate();
                Double unitPrice = businessData.getUnitPrice();
                Integer newUsers = businessData.getNewUsers();
                row.getCell(2).setCellValue(turnover == null ? 0 : turnover);
                row.getCell(3).setCellValue(validOrderCount == null ? 0 : validOrderCount);
                row.getCell(4).setCellValue(orderCompletionRate == null ? 0 : orderCompletionRate);
                row.getCell(5).setCellValue(unitPrice == null ? 0 : unitPrice);
                row.getCell(6).setCellValue(newUsers == null ? 0 : newUsers);
            }
            // 通过输出流将Excel文件下载到浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            // 关闭资源
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
