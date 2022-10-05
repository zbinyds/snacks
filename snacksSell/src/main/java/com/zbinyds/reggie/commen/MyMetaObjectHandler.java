package com.zbinyds.reggie.commen;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author zbinyds
 * @time 2022/08/17 10:21
 *
 * 自定义元数据处理器。实现字段自动填充（Mybatis-plus）
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 进行插入操作时，进行字段自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("插入数据时字段自动填充");
        // 获取当前登录用户id
        Long currentId = BaseContext.getCurrentId();

        metaObject.setValue("createTime",new Date());
        metaObject.setValue("updateTime",new Date());
        metaObject.setValue("createUser",currentId);
        metaObject.setValue("updateUser",currentId);
    }

    /**
     * 进行修改操作时，进行字段自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("修改数据时字段自动填充");
        // 获取当前登录用户id
        Long currentId = BaseContext.getCurrentId();

        metaObject.setValue("updateTime",new Date());
        metaObject.setValue("updateUser",currentId);
    }
}
