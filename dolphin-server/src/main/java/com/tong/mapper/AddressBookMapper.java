package com.tong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
