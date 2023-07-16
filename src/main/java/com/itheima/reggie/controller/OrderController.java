package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return R.success("支付成功？");
    }

    /**
     * 查询订单信息
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize){
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> dtoPage = new Page<>();

        //封装Orders数据
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, currentId);
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        orderService.page(pageInfo, queryWrapper);

        //将数据拷贝到dtoPage
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        //将Orders封装为OrdersDto
        List<Orders> orders = pageInfo.getRecords();
        List<OrdersDto> dtoList = orders.stream().map(order -> {
            OrdersDto ordersDto = new OrdersDto();
            //拷贝基础信息
            BeanUtils.copyProperties(order, ordersDto);
            AddressBook addressBook = addressBookService.getById(order.getAddressBookId());
            ordersDto.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                    + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                    + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                    + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, order.getNumber());
            List<OrderDetail> list = orderDetailService.list(wrapper);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());

        //封装dtoPage
        dtoPage.setRecords(dtoList);

        return R.success(dtoPage);
    }

}
