package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.context.BaseContext;
import com.tong.entity.AddressBook;
import com.tong.mapper.AddressBookMapper;
import com.tong.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Override
    public void addAddressBook(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        save(addressBook);
    }

    @Override
    public void deleteAddressBookById(Long id) {
        removeById(id);
    }

    @Override
    public void updateAddressBookById(AddressBook addressBook) {
        updateById(addressBook);
    }

    @Override
    public AddressBook getAddressBookById(Long id) {
        AddressBook addressBook = getById(id);
        return addressBook;
    }

    @Override
    public List<AddressBook> listAddressBook() {
        List<AddressBook> addressBookList = lambdaQuery()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .list();
        return addressBookList;
    }

    @Override
    public AddressBook getDefaultAddressBook() {
        AddressBook addressBook = lambdaQuery()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1)
                .one();
        return addressBook;
    }

    @Override
    @Transactional
    public void setDefaultAddressBook(AddressBook addressBook) {
        // 将当前用户所有地址改为非默认地址
        lambdaUpdate()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .set(AddressBook::getIsDefault, 0)
                .update();
        // 根据id将指定地址改为默认地址
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        updateById(addressBook);
    }
}
