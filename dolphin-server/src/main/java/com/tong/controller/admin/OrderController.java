package com.tong.controller.admin;

import com.tong.dto.OrdersCancelDTO;
import com.tong.dto.OrdersConfirmDTO;
import com.tong.dto.OrdersPageQueryDTO;
import com.tong.dto.OrdersRejectionDTO;
import com.tong.result.PageResult;
import com.tong.result.Result;
import com.tong.service.OrderService;
import com.tong.vo.OrderStatisticsVO;
import com.tong.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController(value = "adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("分页查询订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("分页查询订单，参数：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("根据id查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("根据id查询订单详情，id：{}", id);
        OrderVO orderVO = orderService.getOrderDetailById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单，参数：{}", ordersCancelDTO);
        orderService.cancelWithReason(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单，参数：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("拒单，参数：{}", ordersRejectionDTO);
        orderService.rejectWithReason(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单，id：{}", id);
        orderService.deliveryById(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单，id：{}", id);
        orderService.completeById(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @ApiOperation("统计各状态订单数")
    public Result<OrderStatisticsVO> statistics(){
        log.info("统计各状态订单数");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
}
