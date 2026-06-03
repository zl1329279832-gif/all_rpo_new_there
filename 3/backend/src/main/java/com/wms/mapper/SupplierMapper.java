package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.Supplier;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SupplierMapper {

    int insert(Supplier record);

    int updateById(Supplier record);

    Supplier selectById(Long id);

    @Select("SELECT * FROM wms_supplier WHERE supplier_code = #{supplierCode}")
    Supplier selectByCode(String supplierCode);

    List<Supplier> selectList(@Param("query") PageQuery query,
                               @Param("supplierCode") String supplierCode,
                               @Param("supplierName") String supplierName,
                               @Param("creditLevel") Integer creditLevel,
                               @Param("status") Integer status);

    @Update("UPDATE wms_supplier SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_supplier")
    int selectCount();
}
