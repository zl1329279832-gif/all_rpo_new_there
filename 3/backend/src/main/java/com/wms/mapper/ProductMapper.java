package com.wms.mapper;

import com.wms.common.PageQuery;
import com.wms.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProductMapper {

    int insert(Product record);

    int updateById(Product record);

    Product selectById(Long id);

    @Select("SELECT * FROM wms_product WHERE product_code = #{productCode}")
    Product selectByCode(String productCode);

    @Select("SELECT * FROM wms_product WHERE barcode = #{barcode}")
    Product selectByBarcode(String barcode);

    List<Product> selectList(@Param("query") PageQuery query,
                              @Param("productCode") String productCode,
                              @Param("productName") String productName,
                              @Param("category") String category,
                              @Param("brand") String brand,
                              @Param("status") Integer status);

    @Update("UPDATE wms_product SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);

    @Select("SELECT COUNT(*) FROM wms_product")
    int selectCount();
}
