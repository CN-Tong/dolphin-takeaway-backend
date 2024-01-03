package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {

    void addAddressBook(AddressBook addressBook);

    void deleteAddressBookById(Long id);

    void updateAddressBookById(AddressBook addressBook);

    AddressBook getAddressBookById(Long id);

    List<AddressBook> listAddressBook();

    AddressBook getDefaultAddressBook();

    void setDefaultAddressBook(AddressBook addressBook);
}
