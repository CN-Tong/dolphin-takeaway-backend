package com.tong.controller.user;

import com.tong.entity.AddressBook;
import com.tong.result.Result;
import com.tong.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "userAddressBookController")
@RequestMapping("/user/addressBook")
@Api(tags = "地址簿相关接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址，参数：{}", addressBook);
        addressBookService.addAddressBook(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result delete(Long id){
        log.info("根据id删除地址，参数：{}", id);
        addressBookService.deleteAddressBookById(id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook){
        log.info("根据id修改地址，参数：{}", addressBook);
        addressBookService.updateAddressBookById(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("根据id查询地址，id：{}", id);
        AddressBook addressBook = addressBookService.getAddressBookById(id);
        return Result.success(addressBook);
    }

    @GetMapping("/list")
    @ApiOperation("查询当前登录用户所有地址")
    public Result<List<AddressBook>> list(){
        log.info("查询当前登录用户所有地址");
        List<AddressBook> addressBookList = addressBookService.listAddressBook();
        return Result.success(addressBookList);
    }

    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddressBook(){
        log.info("查询默认地址");
        AddressBook addressBook = addressBookService.getDefaultAddressBook();
        return Result.success(addressBook);
    }

    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddressBook(@RequestBody AddressBook addressBook){
        log.info("设置默认地址，参数：{}", addressBook);
        addressBookService.setDefaultAddressBook(addressBook);
        return Result.success();
    }
}
